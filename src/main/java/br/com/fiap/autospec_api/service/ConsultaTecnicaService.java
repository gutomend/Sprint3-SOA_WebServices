package br.com.fiap.autospec_api.service;

import br.com.fiap.autospec_api.dto.EspecificacaoDTO;
import br.com.fiap.autospec_api.dto.VeiculoRespostaDTO;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class ConsultaTecnicaService {

    private final PadronizacaoService padronizacaoService;

    public ConsultaTecnicaService(
            PadronizacaoService padronizacaoService) {

        this.padronizacaoService = padronizacaoService;
    }

    public VeiculoRespostaDTO consultar(
            String marca,
            String modelo,
            String versao) {

        VeiculoRespostaDTO resposta =
                new VeiculoRespostaDTO();

        resposta.setMarca(marca);
        resposta.setModelo(modelo);
        resposta.setVersao(versao);

        List<EspecificacaoDTO> especificacoes =
                new ArrayList<>();

        adicionarEspecificacao(
                especificacoes,
                "Horse Power",
                "397 cv"
        );

        adicionarEspecificacao(
                especificacoes,
                "Engine",
                "3.0 V6 Bi-Turbo"
        );

        adicionarEspecificacao(
                especificacoes,
                "Torque Máximo",
                "59 kgfm"
        );

        resposta.setEspecificacoes(especificacoes);

        return resposta;
    }

    private void adicionarEspecificacao(
            List<EspecificacaoDTO> lista,
            String atributoOriginal,
            String valor) {

        String atributoPadronizado =
                padronizacaoService
                        .padronizar(atributoOriginal);

        EspecificacaoDTO dto =
                new EspecificacaoDTO();

        dto.setAtributo(atributoPadronizado);
        dto.setValor(valor);

        lista.add(dto);
    }
}