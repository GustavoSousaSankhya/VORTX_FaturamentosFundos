package br.com.bandeirantes.geranotas.fundos.vortx;

import br.com.sankhya.extensions.actionbutton.AcaoRotinaJava;
import br.com.sankhya.extensions.actionbutton.ContextoAcao;
import br.com.sankhya.extensions.actionbutton.Registro;
import br.com.sankhya.jape.wrapper.JapeFactory;
import br.com.sankhya.jape.wrapper.JapeWrapper;
import br.com.sankhya.modelcore.util.DynamicEntityNames;

import java.math.BigDecimal;

public class MainBtoFaturFundos implements AcaoRotinaJava {

    static JapeWrapper usuDAO = JapeFactory.dao(DynamicEntityNames.USUARIO);

    @Override
    public void doAction(ContextoAcao ctx) throws Exception {
        Registro[] linhas = ctx.getLinhas();
        BigDecimal codUsu = usuDAO.findByPK(ctx.getUsuarioLogado()).asBigDecimal("CODUSU");

        for (Registro linha : linhas) {
            //validaNotas(linha);
        }
    }
}
