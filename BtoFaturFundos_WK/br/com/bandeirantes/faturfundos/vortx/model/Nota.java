package br.com.bandeirantes.faturfundos.vortx.model;

import java.math.BigDecimal;
import java.sql.Timestamp;

public class Nota {

    BigDecimal codEmp;
    BigDecimal codParc;
    BigDecimal codTipOper;
    BigDecimal codTipNeg;
    BigDecimal codNat;
    BigDecimal codCenCus;
    BigDecimal idFundos;
    String observacao;
    BigDecimal codProd;
    BigDecimal qtdNeg;
    BigDecimal vlrUnit;
    BigDecimal nuNota;
    Timestamp dtNeg;

    public Nota(BigDecimal codEmp, BigDecimal codParc, BigDecimal codTipOper, BigDecimal codTipNeg, BigDecimal codNat, BigDecimal codCenCus, BigDecimal idFundos, String observacao, BigDecimal codProd, BigDecimal qtdNeg, BigDecimal vlrUnit, Timestamp dtNeg) {
        this.codEmp = codEmp;
        this.codParc = codParc;
        this.codTipOper = codTipOper;
        this.codTipNeg = codTipNeg;
        this.codNat = codNat;
        this.codCenCus = codCenCus;
        this.idFundos = idFundos;
        this.observacao = observacao;
        this.codProd = codProd;
        this.qtdNeg = qtdNeg;
        this.vlrUnit = vlrUnit;
        this.dtNeg = dtNeg;
    }

    @Override
    public String toString() {
        return "Nota{" +
                "codEmp=" + codEmp +
                ", codParc=" + codParc +
                ", codTipOper=" + codTipOper +
                ", codTipNeg=" + codTipNeg +
                ", codNat=" + codNat +
                ", codCenCus=" + codCenCus +
                ", idFundos=" + idFundos +
                ", observacao='" + observacao + '\'' +
                ", codProd=" + codProd +
                ", qtdNeg=" + qtdNeg +
                ", vlrUnit=" + vlrUnit +
                ", nuNota=" + nuNota +
                ", dtNeg=" + dtNeg +
                '}';
    }

    public BigDecimal getCodEmp() {
        return codEmp;
    }

    public void setCodEmp(BigDecimal codEmp) {
        this.codEmp = codEmp;
    }

    public BigDecimal getCodParc() {
        return codParc;
    }

    public void setCodParc(BigDecimal codParc) {
        this.codParc = codParc;
    }

    public BigDecimal getCodTipOper() {
        return codTipOper;
    }

    public void setCodTipOper(BigDecimal codTipOper) {
        this.codTipOper = codTipOper;
    }

    public BigDecimal getCodTipNeg() {
        return codTipNeg;
    }

    public void setCodTipNeg(BigDecimal codTipNeg) {
        this.codTipNeg = codTipNeg;
    }

    public BigDecimal getCodNat() {
        return codNat;
    }

    public void setCodNat(BigDecimal codNat) {
        this.codNat = codNat;
    }

    public BigDecimal getCodCenCus() {
        return codCenCus;
    }

    public void setCodCenCus(BigDecimal codCenCus) {
        this.codCenCus = codCenCus;
    }

    public BigDecimal getIdFundos() {
        return idFundos;
    }

    public void setIdFundos(BigDecimal idFundos) {
        this.idFundos = idFundos;
    }

    public String getObservacao() {
        return observacao;
    }

    public void setObservacao(String observacao) {
        this.observacao = observacao;
    }

    public BigDecimal getCodProd() {
        return codProd;
    }

    public void setCodProd(BigDecimal codProd) {
        this.codProd = codProd;
    }

    public BigDecimal getQtdNeg() {
        return qtdNeg;
    }

    public void setQtdNeg(BigDecimal qtdNeg) {
        this.qtdNeg = qtdNeg;
    }

    public BigDecimal getVlrUnit() {
        return vlrUnit;
    }

    public void setVlrUnit(BigDecimal vlrUnit) {
        this.vlrUnit = vlrUnit;
    }

    public BigDecimal getNuNota() {
        return nuNota;
    }

    public void setNuNota(BigDecimal nuNota) {
        this.nuNota = nuNota;
    }

    public Timestamp getDtNeg() {
        return dtNeg;
    }

    public void setDtNeg(Timestamp dtNeg) {
        this.dtNeg = dtNeg;
    }

}
