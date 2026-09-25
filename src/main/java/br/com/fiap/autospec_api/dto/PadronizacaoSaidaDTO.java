package br.com.fiap.autospec_api.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class PadronizacaoSaidaDTO {

    private String atributoOriginal;

    private String atributoPadronizado;
}