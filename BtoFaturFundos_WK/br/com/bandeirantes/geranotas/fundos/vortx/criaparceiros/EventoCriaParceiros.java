package br.com.bandeirantes.geranotas.fundos.vortx.criaparceiros;

import br.com.bandeirantes.geranotas.fundos.vortx.model.Parceiro;
import br.com.sankhya.extensions.eventoprogramavel.EventoProgramavelJava;
import br.com.sankhya.jape.event.PersistenceEvent;
import br.com.sankhya.jape.event.TransactionContext;
import br.com.sankhya.jape.vo.DynamicVO;
import br.com.sankhya.jape.wrapper.JapeFactory;
import br.com.sankhya.jape.wrapper.JapeWrapper;
import br.com.sankhya.modelcore.util.DynamicEntityNames;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.net.HttpURLConnection;
import java.net.URL;

public class EventoCriaParceiros implements EventoProgramavelJava {

    JapeWrapper parceiroDAO = JapeFactory.dao("Parceiro");
    JapeWrapper contatoDAO = JapeFactory.dao("Contato");
    JapeWrapper cidadeDAO = JapeFactory.dao(DynamicEntityNames.CIDADE);
    JapeWrapper ufDAO = JapeFactory.dao(DynamicEntityNames.UNIDADE_FEDERATIVA);
    JapeWrapper faturamentosCorpDAO = JapeFactory.dao("AD_FATURAMENTOSCORP");

    String uf = "";
    BigDecimal codParc = null;
    @Override
    public void afterInsert(PersistenceEvent persistenceEvent) throws Exception {
        DynamicVO faturamentoVO = (DynamicVO) persistenceEvent.getVo();
        if(faturamentoVO.asString("CNPJCLIENTE") != null && faturamentoVO.asString("EMAILCLIENTE") != null){
            conexaoApi(faturamentoVO.asString("CNPJCLIENTE"), faturamentoVO.asString("EMAILCLIENTE"));
        }
    }

    private void conexaoApi(String cnpj, String email) throws Exception {

        String url = "https://receitaws.com.br/v1/cnpj/"+cnpj;

        URL apiUrl = new URL(url);
        HttpURLConnection connection = (HttpURLConnection) apiUrl.openConnection();
        connection.setRequestMethod("GET");
        connection.setDoOutput(true);

        int responseCode = connection.getResponseCode();
        do {
            if (responseCode == HttpURLConnection.HTTP_OK) {
                BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                StringBuilder response = new StringBuilder();
                String line;

                while ((line = reader.readLine()) != null) {
                    response.append(line);
                }
                reader.close();

                String jsonResponse = response.toString();

                JSONObject jsonAT = new JSONObject(jsonResponse);

                String tipo = "J";
                String nomeParc = jsonAT.getString("nome");
                if (nomeParc.length() > 40) {
                    nomeParc = nomeParc.substring(0, 40);
                }
                String municipio = jsonAT.getString("municipio");
                String cep = jsonAT.getString("cep").replaceAll("[^0-9]", "");
                uf = jsonAT.getString("uf");
                Parceiro dadosParceiro = new Parceiro(tipo, cnpj, nomeParc, municipio, cep);
                cadastroParceiro(dadosParceiro, email);
            }else {
                if(responseCode == 429) {
                    Thread.sleep(10000);
                }
                if(responseCode == HttpURLConnection.HTTP_NOT_FOUND) {
                    return;
                }
            }
        }while(responseCode == 429);

    }

    private void cadastroParceiro(Parceiro parceiro, String email) throws Exception {

        BigDecimal codCidade = buscaCidade(parceiro.getMunicipio());

        DynamicVO parceiroVO = parceiroDAO.create()
                .set("CGC_CPF", parceiro.getCnpj())
                .set("NOMEPARC", parceiro.getNomeParceiro())
                .set("TIPPESSOA", parceiro.getTipo())
                .set("CLIENTE", "S")
                .set("FORNECEDOR", "S")
                .set("IDENTINSCESTAD", "ISENTO")
                .set("CODCID", codCidade)
                .set("CEP", parceiro.getCep())
                .save();

        codParc = parceiroVO.asBigDecimal("CODPARC");

        if(email != null) {
            DynamicVO contatoVO = contatoDAO.create()
                    .set("CODCONTATO", BigDecimal.ONE)
                    .set("APELIDO", "CONTATO 1")
                    .set("ATIVO", "S")
                    .set("CODPARC", codParc)
                    .set("EMAIL", email)
                    .set("RESPCOBRANCA", "S")
                    .set("RECEBEBOLETOEMAIL", "S")
                    .set("RECEBENOTAEMAIL", "S")
                    .save();
        }
    }

    public BigDecimal buscaCidade(String municipio) throws Exception {
        BigDecimal codCidade = null;
        DynamicVO cidadeCorreioVO = cidadeDAO.findOne("DESCRICAOCORREIO = ?",municipio);
        DynamicVO cidadeVO = cidadeDAO.findOne("NOMECID = ?",municipio);
        if(cidadeCorreioVO != null) {
            codCidade = cidadeCorreioVO.asBigDecimal("CODCID");
        }
        if(cidadeVO != null) {
            codCidade = cidadeVO.asBigDecimal("CODCID");
        }
        return codCidade == null ? criaCidade(municipio) : codCidade;
    }

    public BigDecimal criaCidade(String municipio) throws Exception{
        BigDecimal ufNovo = ufDAO.findOne("UF = ?",uf).asBigDecimal("CODUF");

        return cidadeDAO.create()
                .set("NOMECID", municipio)
                .set("DESCRICAOCORREIO", municipio)
                .set("UF", ufNovo)
                .save().asBigDecimal("CODCID");
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
