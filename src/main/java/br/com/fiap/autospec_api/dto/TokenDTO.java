package br.com.fiap.autospec_api.dto;

public class TokenDTO {

    private String token;
    private String tipo;
    private Long expiraEm;

    public TokenDTO(
            String token,
            String tipo,
            Long expiraEm) {

        this.token = token;
        this.tipo = tipo;
        this.expiraEm = expiraEm;
    }

    public String getToken() {
        return token;
    }

    public String getTipo() {
        return tipo;
    }

    public Long getExpiraEm() {
        return expiraEm;
    }
}