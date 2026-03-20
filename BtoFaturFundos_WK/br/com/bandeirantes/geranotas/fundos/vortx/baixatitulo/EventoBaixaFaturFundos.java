package br.com.bandeirantes.geranotas.fundos.vortx.baixatitulo;

import br.com.bandeirantes.geranotas.fundos.vortx.model.Baixa;
import br.com.sankhya.extensions.eventoprogramavel.EventoProgramavelJava;
import br.com.sankhya.jape.event.PersistenceEvent;
import br.com.sankhya.jape.event.TransactionContext;
import br.com.sankhya.jape.vo.DynamicVO;
import br.com.sankhya.jape.wrapper.JapeFactory;
import br.com.sankhya.jape.wrapper.JapeWrapper;
import br.com.sankhya.modelcore.financeiro.helper.BaixaHelper;
import br.com.sankhya.modelcore.financeiro.util.DadosBaixa;
import br.com.sankhya.modelcore.financeiro.util.DadosBaixa.ValoresBaixa;
import br.com.sankhya.modelcore.util.DynamicEntityNames;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collection;

public class EventoBaixaFaturFundos implements EventoProgramavelJava {

    JapeWrapper faturFundosDAO = JapeFactory.dao("AD_FATURFUNDOS");
    JapeWrapper financeirosDAO = JapeFactory.dao(DynamicEntityNames.FINANCEIRO);
    JapeWrapper parametroSistemaDAO = JapeFactory.dao("ParametroSistema");

    @Override
    public void afterInsert(PersistenceEvent persistenceEvent) throws Exception {
        DynamicVO baixaVO = (DynamicVO) persistenceEvent.getVo();
        realizarBaixa(baixaVO);
    }

    private void realizarBaixa(DynamicVO baixaVO) throws Exception {
        BigDecimal idIntegracao = baixaVO.asBigDecimal("IDINTEGRACAO");
        BigDecimal vlrBaixa = baixaVO.asBigDecimal("VLRBAIXA");
        Timestamp dhIntegracao = baixaVO.asTimestamp("DHINTEGRACAO");

        DynamicVO faturVO = faturFundosDAO.findOne("IDINTEGRACAO = ?", idIntegracao);
        BigDecimal nuNota = faturVO.asBigDecimal("NUNOTA");

        if (nuNota == null) {
            throw new RuntimeException("Nota ainda não gerada para o faturamento IDINTEGRACAO: " + idIntegracao);
        }

        DynamicVO financeiroVO = financeirosDAO.findOne("NUNOTA = ? AND RECDESP = 1", nuNota);
        if (financeiroVO == null) {
            throw new RuntimeException("Título financeiro não encontrado para NUNOTA: " + nuNota);
        }

        Baixa baixa = montaBaixa(vlrBaixa, dhIntegracao, financeiroVO);
        efetuarBaixa(baixa);
    }

    private Baixa montaBaixa(BigDecimal vlrBaixa, Timestamp dtBaixa, DynamicVO financeiroVO) throws Exception {
        DynamicVO paramSistema = parametroSistemaDAO.findOne("CHAVE = 'TOPBAIXA'");
        BigDecimal topBaixa = paramSistema.asBigDecimal("INTEIRO");

        return new Baixa(
                financeiroVO.asBigDecimal("NUFIN"),
                vlrBaixa,
                financeiroVO.asBigDecimal("VLRDESDOB"),
                dtBaixa,
                financeiroVO.asTimestamp("DTVENC"),
                financeiroVO.asBigDecimal("NUMNOTA"),
                financeiroVO.asBigDecimal("CODEMP"),
                financeiroVO.asBigDecimal("CODCTABCOINT"),
                topBaixa
        );
    }

    private void efetuarBaixa(Baixa baixa) throws Exception {
        boolean isBaixaTotal = baixa.getVlrBaixa().compareTo(baixa.getVlrDesdob()) >= 0;
        BigDecimal vlrEfetivo = isBaixaTotal ? baixa.getVlrDesdob() : baixa.getVlrBaixa();

        Collection<DadosBaixa> dadosbaixas = new ArrayList<DadosBaixa>();

        BaixaHelper baixaHelper = new BaixaHelper(baixa.getNufin(), BigDecimal.ZERO, baixa.getDtBaixa(), true);
        DadosBaixa dadosBaixa = baixaHelper.montaDadosBaixa(baixa.getDtBaixa(), true, false);

        DadosBaixa.DadosBancarios dadosBancarios = dadosBaixa.getDadosBancarios();
        DadosBaixa.DadosAdicionais dadosAdicionais = dadosBaixa.getDadosAdicionais();
        DadosBaixa.DadosPendencia dadosPendencia = dadosBaixa.getDadosPendencia();
        ValoresBaixa vlrBaixaObj = dadosBaixa.getValoresBaixa();

        dadosBaixa.setDataBaixa(baixa.getDtBaixa());
        dadosBaixa.getDescisaoBaixa().setDescisao(4);

        vlrBaixaObj.setVlrBaixa(vlrEfetivo.doubleValue());
        vlrBaixaObj.setVlrDesdob(baixa.getVlrDesdob().doubleValue());
        vlrBaixaObj.setVlrTotal(vlrEfetivo.doubleValue());

        if (!isBaixaTotal) {
            dadosPendencia.setDtVencimento(baixa.getDtVenc());
            dadosPendencia.setVlrTotal(baixa.getVlrDesdob().subtract(baixa.getVlrBaixa()).doubleValue());
            dadosPendencia.setVlrDesconto(0);
            dadosPendencia.setVlrDespesaCartorio(0);
            dadosPendencia.setVlrInss(0);
            dadosPendencia.setVlrIrf(0);
            dadosPendencia.setVlrIss(0);
            dadosPendencia.setVlrJuroIncluso(0);
            dadosPendencia.setVlrMultaInclusa(0);
            dadosPendencia.setVlrOutrosImpostos(0);
            dadosPendencia.setVlrOutrosImpostosMensais(0);
            dadosPendencia.setVlrTaxaAdm(0);
            dadosPendencia.setVlrVendor(0);
            dadosPendencia.setVlrJuroNeg(0);
        }

        dadosAdicionais.setCodTipoOperacao(baixa.getTopBaixa());
        dadosAdicionais.setNumNota(baixa.getNumNota());
        dadosAdicionais.setCodEmpresa(baixa.getCodEmp());

        dadosBancarios.setCodConta(baixa.getCodConta());
        dadosBancarios.setCodLancamento(BigDecimal.ONE);
        dadosBancarios.setHistorico("");
        dadosBancarios.setNumDocumento(BigDecimal.ZERO);

        dadosbaixas.add(dadosBaixa);
        baixaHelper.baixaAgrupada(dadosbaixas, vlrEfetivo.doubleValue());
    }

    @Override
    public void beforeInsert(PersistenceEvent persistenceEvent) throws Exception {}

    @Override
    public void beforeUpdate(PersistenceEvent persistenceEvent) throws Exception {}

    @Override
    public void beforeDelete(PersistenceEvent persistenceEvent) throws Exception {}

    @Override
    public void afterUpdate(PersistenceEvent persistenceEvent) throws Exception {}

    @Override
    public void afterDelete(PersistenceEvent persistenceEvent) throws Exception {}

    @Override
    public void beforeCommit(TransactionContext transactionContext) throws Exception {}
}