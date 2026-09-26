package br.com.fiap.autospec_api.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class VeiculoDTO {

    @NotBlank(message = "A marca é obrigatória")
    @Size(max = 100, message = "A marca deve ter no máximo 100 caracteres")
    private String marca;

    @NotBlank(message = "O modelo é obrigatório")
    @Size(max = 100, message = "O modelo deve ter no máximo 100 caracteres")
    private String modelo;

    @NotBlank(message = "A versão é obrigatória")
    @Size(max = 100, message = "A versão deve ter no máximo 100 caracteres")
    private String versao;

    @Valid
    private List<EspecificacaoDTO> especificacoes;
}