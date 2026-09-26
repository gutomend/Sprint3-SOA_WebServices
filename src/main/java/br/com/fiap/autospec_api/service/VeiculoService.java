package br.com.fiap.autospec_api.service;

import br.com.fiap.autospec_api.exception.RecursoNaoEncontradoException;
import br.com.fiap.autospec_api.model.Veiculo;
import br.com.fiap.autospec_api.repository.VeiculoRepository;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.List;

@Service
public class VeiculoService {

    private final VeiculoRepository repository;
    private static final Logger logger = LoggerFactory.getLogger(VeiculoService.class);

    public VeiculoService(VeiculoRepository repository) {
        this.repository = repository;
    }

    public Veiculo salvar(Veiculo veiculo) {

        associarEspecificacoes(veiculo);

        return repository.save(veiculo);
    }

    public List<Veiculo> listarTodos() {
        return repository.findAll();
    }

    public Veiculo buscarPorId(Long id) {
        return repository.findById(id)
                .orElseThrow(() ->
                        new RecursoNaoEncontradoException(
                                "Veículo não encontrado com o ID: " + id
                        )
                );
    }

    public Veiculo atualizar(Long id, Veiculo veiculoAtualizado) {

        Veiculo veiculoExistente = buscarPorId(id);

        veiculoExistente.setMarca(veiculoAtualizado.getMarca());
        veiculoExistente.setModelo(veiculoAtualizado.getModelo());
        veiculoExistente.setVersao(veiculoAtualizado.getVersao());

        veiculoExistente.setEspecificacoes(
                veiculoAtualizado.getEspecificacoes()
        );

        associarEspecificacoes(veiculoExistente);

        return repository.save(veiculoExistente);
    }

    public void excluir(Long id) {

        Veiculo veiculo = buscarPorId(id);

        String actor = SecurityContextHolder.getContext().getAuthentication() != null
            ? SecurityContextHolder.getContext().getAuthentication().getName()
            : "system";

        logger.warn("event=delete_vehicle vehicleId={} marca={} modelo={} actor={}",
            veiculo.getId(), veiculo.getMarca(), veiculo.getModelo(), actor);

        repository.delete(veiculo);
    }

    private void associarEspecificacoes(Veiculo veiculo) {

        if (veiculo.getEspecificacoes() != null) {

            veiculo.getEspecificacoes().forEach(
                    especificacao ->
                            especificacao.setVeiculo(veiculo)
            );
        }
    }
}