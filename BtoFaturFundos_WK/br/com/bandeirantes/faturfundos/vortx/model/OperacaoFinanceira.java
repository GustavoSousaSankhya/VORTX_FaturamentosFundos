package br.com.bandeirantes.faturfundos.vortx.model;

import java.math.BigDecimal;

public class OperacaoFinanceira {
    String periodicidade;
    BigDecimal codOperFin;
    String tipoOperacao;
    BigDecimal controleInterno;
    String descrIf;
    String apelidoOperacao;
    String emissao;
    String serie;

    public OperacaoFinanceira(String periodicidade,String tipoOperacao, BigDecimal controleInterno, String descrIf, String apelidoOperacao, String emissao, String serie) {
        this.periodicidade = periodicidade;
        this.tipoOperacao = tipoOperacao;
        this.controleInterno = controleInterno;
        this.descrIf = descrIf;
        this.apelidoOperacao = apelidoOperacao;
        this.emissao = emissao;
        this.serie = serie;
    }

    @Override
    public String toString() {
        return "OperacaoFinanceira{" +
                "periodicidade='" + periodicidade + '\'' +
                ", codOperFin=" + codOperFin +
                ", tipoOperacao='" + tipoOperacao + '\'' +
                ", controleInterno=" + controleInterno +
                ", descrIf='" + descrIf + '\'' +
                ", apelidoOperacao='" + apelidoOperacao + '\'' +
                ", emissao='" + emissao + '\'' +
                ", serie='" + serie + '\'' +
                '}';
    }

    public String getPeriodicidade() {
        return periodicidade;
    }

    public void setPeriodicidade(String periodicidade) {
        this.periodicidade = periodicidade;
    }

    public BigDecimal getCodOperFin() {
        return codOperFin;
    }

    public void setCodOperFin(BigDecimal codOperFin) {
        this.codOperFin = codOperFin;
    }

    public String getTipoOperacao() {
        return tipoOperacao;
    }

    public void setTipoOperacao(String tipoOperacao) {
        this.tipoOperacao = tipoOperacao;
    }

    public BigDecimal getControleInterno() {
        return controleInterno;
    }

    public void setControleInterno(BigDecimal controleInterno) {
        this.controleInterno = controleInterno;
    }

    public String getDescrIf() {
        return descrIf;
    }

    public void setDescrIf(String descrIf) {
        this.descrIf = descrIf;
    }

    public String getApelidoOperacao() {
        return apelidoOperacao;
    }

    public void setApelidoOperacao(String apelidoOperacao) {
        this.apelidoOperacao = apelidoOperacao;
    }

    public String getEmissao() {
        return emissao;
    }

    public void setEmissao(String emissao) {
        this.emissao = emissao;
    }

    public String getSerie() {
        return serie;
    }

    public void setSerie(String serie) {
        this.serie = serie;
    }
}
