package br.com.bandeirantes.geranotas.fundos.vortx.criaoperfin;

import br.com.bandeirantes.geranotas.fundos.vortx.model.OperacaoFinanceira;
import br.com.sankhya.extensions.eventoprogramavel.EventoProgramavelJava;
import br.com.sankhya.jape.EntityFacade;
import br.com.sankhya.jape.event.PersistenceEvent;
import br.com.sankhya.jape.event.TransactionContext;
import br.com.sankhya.jape.sql.NativeSql;
import br.com.sankhya.jape.vo.DynamicVO;
import br.com.sankhya.jape.wrapper.JapeFactory;
import br.com.sankhya.jape.wrapper.JapeWrapper;
import br.com.sankhya.modelcore.util.DynamicEntityNames;
import br.com.sankhya.modelcore.util.EntityFacadeFactory;

import java.math.BigDecimal;
import java.sql.ResultSet;

public class EventoCriaOperacaoFinan implements EventoProgramavelJava {

    JapeWrapper faturamentoDAO = JapeFactory.dao("AD_FATURFUNDOS");
    JapeWrapper emissorDAO = JapeFactory.dao("AD_EMISSOPER");
    JapeWrapper serieDAO = JapeFactory.dao("AD_SERIEOPER");
    JapeWrapper operFinDAO = JapeFactory.dao("AD_OPERFIN");

    OperacaoFinanceira operacaoFinanceira = null;


    @Override
    public void afterInsert(PersistenceEvent persistenceEvent) throws Exception {
        DynamicVO faturamentoVO = (DynamicVO) persistenceEvent.getVo();
        setaOperFin(faturamentoVO);
    }

    @Override
    public void afterUpdate(PersistenceEvent persistenceEvent) throws Exception {

    }

    public void setaOperFin(DynamicVO faturamentoVO) throws Exception {
        if(faturamentoVO != null && faturamentoVO.asBigDecimal("CODOPERFIN") == null){
            if(podeCriarOperacaoFin(faturamentoVO)) {
                String tipoOperacao = faturamentoVO.asString("TIPOPER");
                BigDecimal serie = validaCriaSerie(faturamentoVO.asString("SERIE"));
                BigDecimal emissao = validaCriaEmissor(faturamentoVO.asString("EMISSAO"));
                String operacao = faturamentoVO.asString("OPERACAO");
                String descrIf = faturamentoVO.asString("DESCR_IF");
                BigDecimal controleInterno = faturamentoVO.asBigDecimal("CONTROLEINTERNO");
                String periocididade = faturamentoVO.asString("PERIODICIDADE");

                String operacaoEscapada = operacao != null ? operacao.replace("'", "''") : null;
                String descrIfEscapada = descrIf != null ? descrIf.replace("'", "''") : null;

                operacaoFinanceira = new OperacaoFinanceira(periocididade, tipoOperacao, controleInterno, descrIfEscapada, operacaoEscapada, emissao, serie);

                BigDecimal codOperFin = existeOperFin(operacaoFinanceira);

                if(codOperFin == null) {
                    codOperFin = cadastraOperFin(operacaoFinanceira);
                }

                faturamentoDAO.prepareToUpdate(faturamentoVO)
                        .set("CODOPERFIN", codOperFin)
                        .update();
            }
        }
    }

    public BigDecimal existeOperFin(OperacaoFinanceira operFin) throws Exception {
        EntityFacade dwf = EntityFacadeFactory.getDWFFacade();
        NativeSql nativeSql = new NativeSql(dwf.getJdbcWrapper());
        ResultSet queryOperFin = null;

        if(operFin.getDescrIf() != null) {
            nativeSql.setNamedParameter("TIPOOPERACAO", operFin.getTipoOperacao());
            nativeSql.setNamedParameter("SERIE", operFin.getSerie());
            nativeSql.setNamedParameter("EMISSAO", operFin.getEmissao());
            nativeSql.setNamedParameter("OPERACAO", operFin.getApelidoOperacao());
            nativeSql.setNamedParameter("DESCR_IF", operFin.getDescrIf());
            nativeSql.setNamedParameter("CONTROLEINTERNO", operFin.getControleInterno());
            queryOperFin = nativeSql.executeQuery("SELECT TOP 1 CODOPERFIN FROM AD_OPERFIN \r\n" +
                    "\r\n" +
                    "WHERE TIPOPER = :TIPOOPERACAO\r\n" +
                    "AND (CODOPER = :SERIE OR (CODOPER IS NULL AND :SERIE IS NULL))\r\n" +
                    "AND (CODEMISOP = :EMISSAO OR (CODEMISOP IS NULL AND :EMISSAO IS NULL))\r\n" +
                    "AND (DESCRICAO = :OPERACAO OR (DESCRICAO IS NULL AND :OPERACAO IS NULL))\r\n" +
                    "AND (CONTROLEINTERNO = :CONTROLEINTERNO OR (CONTROLEINTERNO IS NULL AND :CONTROLEINTERNO IS NULL))\r\n" +
                    "AND (DESCR_IF = :DESCR_IF)");
        } else {
            nativeSql.setNamedParameter("TIPOOPERACAO", operFin.getTipoOperacao());
            nativeSql.setNamedParameter("SERIE", operFin.getSerie());
            nativeSql.setNamedParameter("EMISSAO", operFin.getEmissao());
            nativeSql.setNamedParameter("OPERACAO", operFin.getApelidoOperacao());
            nativeSql.setNamedParameter("CONTROLEINTERNO", operFin.getControleInterno());
            queryOperFin = nativeSql.executeQuery("SELECT TOP 1 CODOPERFIN FROM AD_OPERFIN \r\n" +
                    "\r\n" +
                    "WHERE TIPOPER = :TIPOOPERACAO\r\n" +
                    "AND (CODOPER = :SERIE OR (CODOPER IS NULL AND :SERIE IS NULL))\r\n" +
                    "AND (CODEMISOP = :EMISSAO OR (CODEMISOP IS NULL AND :EMISSAO IS NULL))\r\n" +
                    "AND (DESCRICAO = :OPERACAO OR (DESCRICAO IS NULL AND :OPERACAO IS NULL))\r\n" +
                    "AND (CONTROLEINTERNO = :CONTROLEINTERNO OR (CONTROLEINTERNO IS NULL AND :CONTROLEINTERNO IS NULL))");
        }

        if(queryOperFin.next()) {
            return queryOperFin.getBigDecimal("CODOPERFIN");
        }
        return null;
    }

    public BigDecimal cadastraOperFin (OperacaoFinanceira operFin) throws Exception {
        operFin.setCodOperFin(maxOperFin());

        DynamicVO operacaoFinVO = operFinDAO.create()
                .set("CODOPERFIN", operFin.getCodOperFin())
                .set("TIPOPER", operFin.getTipoOperacao())
                .set("CODOPER", operFin.getSerie())
                .set("CODEMISOP", operFin.getEmissao())
                .set("DESCRICAO", operFin.getApelidoOperacao())
                .set("DESCR_IF", operFin.getDescrIf())
                .set("ATIVO", "S")
                .set("CONTROLEINTERNO", operFin.getControleInterno())
                //.set("CODPARC", new BigDecimal(4893))
                .save();

       return  operacaoFinVO.asBigDecimal("CODOPERFIN");
    }

    public BigDecimal validaCriaEmissor (String descrEmissor) throws Exception{
        DynamicVO emissorVO = emissorDAO.findOne("DESCROP = ?",descrEmissor);

        if(emissorVO != null) {
            return emissorVO.asBigDecimal("CODEMISOP");
        }else {

            DynamicVO emissorNovo = emissorDAO.create()
                    .set("DESCROP", descrEmissor)
                    .save();

            return emissorNovo.asBigDecimal("CODEMISOP");

        }
    }

    public BigDecimal validaCriaSerie (String descrSerie) throws Exception{
        DynamicVO serieVO = serieDAO.findOne("DESCROPER = ?",descrSerie);

        if(serieVO != null) {
            return serieVO.asBigDecimal("CODOPER");
        }else {

            DynamicVO novoEmissorVO = serieDAO.create()
                    .set("DESCROPER", descrSerie)
                    .save();

            return novoEmissorVO.asBigDecimal("CODOPER");
        }

    }

    private boolean podeCriarOperacaoFin(DynamicVO vo) {

        String[] camposObrigatorios = {
                "PERIODICIDADE",
                "TIPOPER",
                "CONTROLEINTERNO",
                "OPERACAO",
                "EMISSAO",
                "SERIE",
        };

        for (String campo : camposObrigatorios) {
            Object valor = vo.getProperty(campo);
            if (valor == null || (valor instanceof String && ((String)valor).trim().isEmpty())) {
                return false;
            }
        }

        return true;
    }

    public BigDecimal maxOperFin() throws Exception {
        EntityFacade dwf = EntityFacadeFactory.getDWFFacade();
        NativeSql nativeSql = new NativeSql(dwf.getJdbcWrapper());

        nativeSql.cleanParameters();
        ResultSet queryOperFin = nativeSql.executeQuery("SELECT MAX(CODOPERFIN) AS CODOPERFIN FROM AD_OPERFIN");
        if(queryOperFin.next()) {
            return queryOperFin.getBigDecimal("CODOPERFIN").add(BigDecimal.ONE);
        }
        return null;
    }

    @Override
    public void afterDelete(PersistenceEvent persistenceEvent) throws Exception {}

    @Override
    public void beforeCommit(TransactionContext transactionContext) throws Exception {}
    @Override
    public void beforeInsert(PersistenceEvent persistenceEvent) throws Exception {}

    @Override
    public void beforeUpdate(PersistenceEvent persistenceEvent) throws Exception {}

    @Override
    public void beforeDelete(PersistenceEvent persistenceEvent) throws Exception {}
}
