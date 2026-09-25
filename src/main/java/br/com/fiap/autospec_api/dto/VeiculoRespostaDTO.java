package br.com.fiap.autospec_api.dto;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@JsonPropertyOrder({
        "marca",
        "modelo",
        "versao",
        "especificacoes"
})
public class VeiculoRespostaDTO {

    private String marca;

    private String modelo;

    private String versao;

    private List<EspecificacaoDTO> especificacoes;
}