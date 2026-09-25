package br.com.fiap.autospec_api.exception;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class ErroResposta {

    private int status;

    private List<String> mensagens;

    public ErroResposta(int status, List<String> mensagens) {
        this.status = status;
        this.mensagens = mensagens;
    }
}