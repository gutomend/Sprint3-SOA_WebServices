package br.com.fiap.autospec_api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PadronizacaoEntradaDTO {

    @NotBlank(message = "O atributo é obrigatório")
    @Size(max = 200, message = "O atributo deve ter no máximo 200 caracteres")
    private String atributo;
}