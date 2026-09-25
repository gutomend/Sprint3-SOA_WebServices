package br.com.fiap.autospec_api.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class VeiculoDTO {

    @NotBlank(message = "A marca é obrigatória")
    private String marca;

    @NotBlank(message = "O modelo é obrigatório")
    private String modelo;

    @NotBlank(message = "A versão é obrigatória")
    private String versao;

    @Valid
    private List<EspecificacaoDTO> especificacoes;
}