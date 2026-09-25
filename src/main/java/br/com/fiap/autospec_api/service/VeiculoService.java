package br.com.fiap.autospec_api.service;

import br.com.fiap.autospec_api.exception.RecursoNaoEncontradoException;
import br.com.fiap.autospec_api.model.Veiculo;
import br.com.fiap.autospec_api.repository.VeiculoRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class VeiculoService {

    private final VeiculoRepository repository;

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