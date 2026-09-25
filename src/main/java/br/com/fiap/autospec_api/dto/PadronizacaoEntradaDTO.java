package br.com.fiap.autospec_api.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PadronizacaoEntradaDTO {

    @NotBlank(message = "O atributo é obrigatório")
    private String atributo;
}