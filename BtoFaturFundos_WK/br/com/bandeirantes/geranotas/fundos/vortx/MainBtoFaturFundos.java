package br.com.bandeirantes.geranotas.fundos.vortx;

import br.com.bandeirantes.geranotas.fundos.vortx.model.Nota;
import br.com.sankhya.extensions.actionbutton.AcaoRotinaJava;
import br.com.sankhya.extensions.actionbutton.ContextoAcao;
import br.com.sankhya.extensions.actionbutton.Registro;
import br.com.sankhya.jape.EntityFacade;
import br.com.sankhya.jape.vo.DynamicVO;
import br.com.sankhya.jape.vo.PrePersistEntityState;
import br.com.sankhya.jape.wrapper.JapeFactory;
import br.com.sankhya.jape.wrapper.JapeWrapper;
import br.com.sankhya.modelcore.auth.AuthenticationInfo;
import br.com.sankhya.modelcore.comercial.BarramentoRegra;
import br.com.sankhya.modelcore.comercial.CentralFaturamento;
import br.com.sankhya.modelcore.comercial.ComercialUtils;
import br.com.sankhya.modelcore.comercial.ConfirmacaoNotaHelper;
import br.com.sankhya.modelcore.comercial.centrais.CACHelper;
import br.com.sankhya.modelcore.comercial.impostos.ImpostosHelpper;
import br.com.sankhya.modelcore.util.DynamicEntityNames;
import br.com.sankhya.modelcore.util.EntityFacadeFactory;
import br.com.sankhya.ws.ServiceContext;
import com.sankhya.util.TimeUtils;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collection;

public class MainBtoFaturFundos implements AcaoRotinaJava {

    static JapeWrapper usuDAO = JapeFactory.dao(DynamicEntityNames.USUARIO);
    static JapeWrapper parceiroDAO = JapeFactory.dao(DynamicEntityNames.PARCEIRO);

    @Override
    public void doAction(ContextoAcao ctx) throws Exception {
        Registro[] linhas = ctx.getLinhas();
        BigDecimal codUsu = usuDAO.findByPK(ctx.getUsuarioLogado()).asBigDecimal("CODUSU");

        for (Registro linha : linhas) {
            if (linha.getCampo("NUNOTA") == null) {
                validaNotas(linha);
                gerarNota(linha, codUsu);
            }
        }
        ctx.setMensagemRetorno("Nota(s) gerada(s) com sucesso!");
    }

    private static void validaNotas(Registro linha) throws Exception {
        if (linha.getCampo("NUNOTA") != null) {
            throw new Exception("Nota já foi gerada para este faturamento!");
        }
        if (linha.getCampo("CNPJCLIENTE") == null) {
            throw new Exception("CNPJ do cliente está em branco!");
        }
        if (linha.getCampo("CODEMP") == null) {
            throw new Exception("Unidade de Negócio está em branco!");
        }
        if (linha.getCampo("CODTIPOPER") == null) {
            throw new Exception("Tipo de Operação está em branco!");
        }
        if (linha.getCampo("TIPNEG") == null) {
            throw new Exception("Tipo de Negociação está em branco!");
        }
        if (linha.getCampo("CODPROD") == null) {
            throw new Exception("Serviço (Produto) está em branco!");
        }
        if (linha.getCampo("VLRUNIT") == null) {
            throw new Exception("Valor Unitário está em branco!");
        }
        if (linha.getCampo("DTREMESSA") == null) {
            throw new Exception("Data de Remessa está em branco!");
        }
        if (linha.getCampo("CODOPERFIN") == null) {
            throw new Exception("Operação Financeira está em branco!");
        }
        if (retornaParceiro((String) linha.getCampo("CNPJCLIENTE")) == null) {
            throw new Exception("Parceiro não encontrado para o CNPJ: " + linha.getCampo("CNPJCLIENTE") + ". Verifique o cadastro de parceiros!");
        }
    }

    private void gerarNota(Registro linha, BigDecimal codUsu) throws Exception {
        try {
            EntityFacade dwfFacade = EntityFacadeFactory.getDWFFacade();
            ServiceContext sctx = new ServiceContext(null);
            sctx.setAutentication(AuthenticationInfo.getCurrent());
            sctx.makeCurrent();

            CACHelper cacHelper = new CACHelper();
            BigDecimal codOperFin = (BigDecimal) linha.getCampo("CODOPERFIN");

            Nota nota = montaNota(linha);

            DynamicVO newcabVO = gerarCabecalho(nota, codOperFin, dwfFacade, sctx, cacHelper);
            BigDecimal nuNota = newcabVO.asBigDecimal("NUNOTA");

            incluirItem(nota, nuNota, dwfFacade, sctx, cacHelper);

            calcularImpostos(nuNota);

            confirmarNota(nuNota);

            setCamposFaturamento(linha, newcabVO, codUsu);

        } catch (Exception e) {
            System.out.println("Erro na geração da nota de faturamento fundos: " + e.getMessage());
            throw e;
        }
    }

    private static Nota montaNota(Registro linha) throws Exception {
        BigDecimal codParc = retornaParceiro((String) linha.getCampo("CNPJCLIENTE"));
        return new Nota(
                (BigDecimal) linha.getCampo("CODEMP"),
                codParc,
                (BigDecimal) linha.getCampo("CODTIPOPER"),
                (BigDecimal) linha.getCampo("TIPNEG"),
                null,
                null,
                (BigDecimal) linha.getCampo("IDFUNDOS"),
                (String) linha.getCampo("OBSERVACAO"),
                (BigDecimal) linha.getCampo("CODPROD"),
                BigDecimal.ONE,
                (BigDecimal) linha.getCampo("VLRUNIT"),
                (Timestamp) linha.getCampo("DTREMESSA")
        );
    }

    private DynamicVO gerarCabecalho(Nota nota, BigDecimal codOperFin, EntityFacade dwfFacade, ServiceContext sctx, CACHelper cacHelper) throws Exception {
        DynamicVO tipnegVO = ComercialUtils.getTipoNegociacao(nota.getCodTipNeg());
        DynamicVO topVO = ComercialUtils.getTipoOperacao(nota.getCodTipOper());

        DynamicVO nunotaModeloVO = (DynamicVO) dwfFacade.getDefaultValueObjectInstance(DynamicEntityNames.CABECALHO_NOTA);

        nunotaModeloVO.setProperty("CODTIPVENDA", tipnegVO.asBigDecimal("CODTIPVENDA"));
        nunotaModeloVO.setProperty("DHTIPVENDA", tipnegVO.asTimestamp("DHALTER"));
        nunotaModeloVO.setProperty("CODTIPOPER", topVO.asBigDecimal("CODTIPOPER"));
        nunotaModeloVO.setProperty("DHTIPOPER", topVO.asTimestamp("DHALTER"));
        nunotaModeloVO.setProperty("TIPMOV", topVO.asString("TIPMOV"));
        nunotaModeloVO.setProperty("DTNEG", nota.getDtNeg());
        nunotaModeloVO.setProperty("DTMOV", nota.getDtNeg());
        nunotaModeloVO.setProperty("CODEMP", nota.getCodEmp());
        nunotaModeloVO.setProperty("CODPARC", nota.getCodParc());
        nunotaModeloVO.setProperty("AD_CODOPERFIN", codOperFin);
        nunotaModeloVO.setProperty("OBSERVACAO", nota.getObservacao());

        PrePersistEntityState cabPreState = PrePersistEntityState.build(dwfFacade, DynamicEntityNames.CABECALHO_NOTA, nunotaModeloVO);
        cabPreState.getNewVO();

        BarramentoRegra bregra = cacHelper.incluirAlterarCabecalho(sctx, cabPreState);
        return bregra.getState().getNewVO();
    }

    private void incluirItem(Nota nota, BigDecimal nuNota, EntityFacade dwfFacade, ServiceContext sctx, CACHelper cacHelper) throws Exception {
        Collection<PrePersistEntityState> itensNota = new ArrayList<PrePersistEntityState>();
        DynamicVO itemVO = (DynamicVO) dwfFacade.getDefaultValueObjectInstance("ItemNota");

        itemVO.setProperty("NUNOTA", nuNota);
        itemVO.setProperty("CODPROD", nota.getCodProd());
        itemVO.setProperty("QTDNEG", nota.getQtdNeg());
        itemVO.setProperty("VLRUNIT", nota.getVlrUnit());

        PrePersistEntityState itePreState = PrePersistEntityState.build(dwfFacade, "ItemNota", itemVO);
        itePreState.getNewVO();
        itensNota.add(itePreState);

        cacHelper.incluirAlterarItem(nuNota, sctx, itensNota, true);
    }

    private void calcularImpostos(BigDecimal nuNota) throws Exception {
        ImpostosHelpper imposto = new ImpostosHelpper();
        imposto.forcaRecalculoBaseISS(true);
        imposto.calcularISS(nuNota);
        imposto.calcularIRF(nuNota);
        imposto.calcularImpostos(nuNota);
    }

    private void confirmarNota(BigDecimal nuNota) throws Exception {
        BarramentoRegra barramentoConfirmacao = BarramentoRegra.build(CentralFaturamento.class, "regrasConfirmacaoSilenciosa.xml", AuthenticationInfo.getCurrent());
        ConfirmacaoNotaHelper.confirmarNota(nuNota, barramentoConfirmacao);
    }

    private void setCamposFaturamento(Registro linha, DynamicVO cabVO, BigDecimal codUsu) throws Exception {
        linha.setCampo("NUNOTA", cabVO.asBigDecimal("NUNOTA"));
        linha.setCampo("NUMNOTA", cabVO.asBigDecimal("NUMNOTA"));


        linha.setCampo("CODUSUFATUR", codUsu);
        linha.setCampo("DHFATUR", TimeUtils.getNow());
    }

    private static BigDecimal retornaParceiro(String cnpj) throws Exception {
        DynamicVO parceiroVO = parceiroDAO.findOne("CGC_CPF = ?", cnpj);
        return parceiroVO != null ? parceiroVO.asBigDecimal("CODPARC") : null;
    }
}