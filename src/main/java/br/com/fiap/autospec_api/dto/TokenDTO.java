package br.com.fiap.autospec_api.dto;

public class TokenDTO {

    private String token;
    private String tipo;
    private Long expiraEm;
    private String refreshToken;
    private Long refreshExpiraEm;

    public TokenDTO(
            String token,
            String tipo,
            Long expiraEm,
            String refreshToken,
            Long refreshExpiraEm) {

        this.token = token;
        this.tipo = tipo;
        this.expiraEm = expiraEm;
        this.refreshToken = refreshToken;
        this.refreshExpiraEm = refreshExpiraEm;
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

    public String getRefreshToken() {
        return refreshToken;
    }

    public Long getRefreshExpiraEm() {
        return refreshExpiraEm;
    }
}