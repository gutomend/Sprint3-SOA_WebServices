package br.com.fiap.autospec_api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class EspecificacaoDTO {

    @NotBlank(message = "O atributo é obrigatório")
    @Size(max = 200, message = "O atributo deve ter no máximo 200 caracteres")
    private String atributo;

    @NotBlank(message = "O valor é obrigatório")
    @Size(max = 200, message = "O valor deve ter no máximo 200 caracteres")
    private String valor;
}