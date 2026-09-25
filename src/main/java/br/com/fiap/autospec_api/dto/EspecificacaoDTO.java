package br.com.fiap.autospec_api.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class EspecificacaoDTO {

    @NotBlank(message = "O atributo é obrigatório")
    private String atributo;

    @NotBlank(message = "O valor é obrigatório")
    private String valor;
}