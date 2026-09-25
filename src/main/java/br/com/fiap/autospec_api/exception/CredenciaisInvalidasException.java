package br.com.fiap.autospec_api.exception;

public class CredenciaisInvalidasException
        extends RuntimeException {

    public CredenciaisInvalidasException(String mensagem) {
        super(mensagem);
    }
}