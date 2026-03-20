package br.com.bandeirantes.geranotas.fundos.vortx.model;

import java.math.BigDecimal;
import java.sql.Timestamp;

public class Baixa {

    BigDecimal nufin;
    BigDecimal vlrBaixa;
    BigDecimal vlrDesdob;
    Timestamp dtBaixa;
    Timestamp dtVenc;
    BigDecimal numNota;
    BigDecimal codEmp;
    BigDecimal codConta;
    BigDecimal topBaixa;

    public Baixa(BigDecimal nufin, BigDecimal vlrBaixa, BigDecimal vlrDesdob, Timestamp dtBaixa, Timestamp dtVenc, BigDecimal numNota, BigDecimal codEmp, BigDecimal codConta, BigDecimal topBaixa) {
        this.nufin = nufin;
        this.vlrBaixa = vlrBaixa;
        this.vlrDesdob = vlrDesdob;
        this.dtBaixa = dtBaixa;
        this.dtVenc = dtVenc;
        this.numNota = numNota;
        this.codEmp = codEmp;
        this.codConta = codConta;
        this.topBaixa = topBaixa;
    }

    @Override
    public String toString() {
        return "Baixa{" +
                "nufin=" + nufin +
                ", vlrBaixa=" + vlrBaixa +
                ", vlrDesdob=" + vlrDesdob +
                ", dtBaixa=" + dtBaixa +
                ", dtVenc=" + dtVenc +
                ", numNota=" + numNota +
                ", codEmp=" + codEmp +
                ", codConta=" + codConta +
                ", topBaixa=" + topBaixa +
                '}';
    }

    public BigDecimal getNufin() {
        return nufin;
    }

    public void setNufin(BigDecimal nufin) {
        this.nufin = nufin;
    }

    public BigDecimal getVlrBaixa() {
        return vlrBaixa;
    }

    public void setVlrBaixa(BigDecimal vlrBaixa) {
        this.vlrBaixa = vlrBaixa;
    }

    public BigDecimal getVlrDesdob() {
        return vlrDesdob;
    }

    public void setVlrDesdob(BigDecimal vlrDesdob) {
        this.vlrDesdob = vlrDesdob;
    }

    public Timestamp getDtBaixa() {
        return dtBaixa;
    }

    public void setDtBaixa(Timestamp dtBaixa) {
        this.dtBaixa = dtBaixa;
    }

    public Timestamp getDtVenc() {
        return dtVenc;
    }

    public void setDtVenc(Timestamp dtVenc) {
        this.dtVenc = dtVenc;
    }

    public BigDecimal getNumNota() {
        return numNota;
    }

    public void setNumNota(BigDecimal numNota) {
        this.numNota = numNota;
    }

    public BigDecimal getCodEmp() {
        return codEmp;
    }

    public void setCodEmp(BigDecimal codEmp) {
        this.codEmp = codEmp;
    }

    public BigDecimal getCodConta() {
        return codConta;
    }

    public void setCodConta(BigDecimal codConta) {
        this.codConta = codConta;
    }

    public BigDecimal getTopBaixa() {
        return topBaixa;
    }

    public void setTopBaixa(BigDecimal topBaixa) {
        this.topBaixa = topBaixa;
    }
}