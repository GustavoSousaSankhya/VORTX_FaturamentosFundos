package br.com.bandeirantes.geranotas.fundos.vortx.model;

public class Parceiro {
    private String tipo;
    private String cnpj;
    private String nomeParceiro;
    private String municipio;
    private String cep;


    public Parceiro(String tipo, String cnpj, String nomeParceiro, String municipio, String cep) {
        super();
        this.tipo = tipo;
        this.cnpj = cnpj;
        this.nomeParceiro = nomeParceiro;
        this.municipio = municipio;
        this.cep = cep;
    }
    @Override
    public String toString() {
        return "Parceiro [tipo=" + tipo + ", cnpj=" + cnpj + ", nomeParceiro=" + nomeParceiro + ", municipio="
                + municipio + ", cep=" + cep + "]";
    }

    public String getTipo() {
        return tipo;
    }
    public void setTipo(String tipo) {
        this.tipo = tipo;
    }

    public String getCnpj() {
        return cnpj;
    }
    public void setCnpj(String cnpj) {
        this.cnpj = cnpj;
    }
    public String getNomeParceiro() {
        return nomeParceiro;
    }
    public void setNomeParceiro(String nomeParceiro) {
        this.nomeParceiro = nomeParceiro;
    }

    public String getMunicipio() {
        return municipio;
    }

    public void setMunicipio(String municipio) {
        this.municipio = municipio;
    }
    public String getCep() {
        return cep;
    }
    public void setCep(String cep) {
        this.cep = cep;
    }

}
