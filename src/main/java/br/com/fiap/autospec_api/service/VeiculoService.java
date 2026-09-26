package br.com.fiap.autospec_api.service;

import br.com.fiap.autospec_api.exception.RecursoNaoEncontradoException;
import br.com.fiap.autospec_api.model.Veiculo;
import br.com.fiap.autospec_api.repository.VeiculoRepository;
import br.com.fiap.autospec_api.security.CryptoService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class VeiculoService {

    private final VeiculoRepository repository;
    private final CryptoService cryptoService;

    public VeiculoService(VeiculoRepository repository, CryptoService cryptoService) {
        this.repository = repository;
        this.cryptoService = cryptoService;
    }

    public Veiculo salvar(Veiculo veiculo) {

        // Encrypt specification values before saving
        if (veiculo.getEspecificacoes() != null) {
            veiculo.getEspecificacoes().forEach(e -> {
                if (e.getValor() != null) {
                    e.setValor(cryptoService.encrypt(e.getValor()));
                }
            });
        }

        associarEspecificacoes(veiculo);

        return repository.save(veiculo);
    }

    public List<Veiculo> listarTodos() {
        List<Veiculo> veiculos = repository.findAll();

        // Decrypt especificacao values before returning
        veiculos.forEach(v -> {
            if (v.getEspecificacoes() != null) {
                v.getEspecificacoes().forEach(e -> {
                    if (e.getValor() != null) {
                        e.setValor(cryptoService.decrypt(e.getValor()));
                    }
                });
            }
        });

        return veiculos;
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

        // Encrypt specification values before saving the updated entity
        if (veiculoExistente.getEspecificacoes() != null) {
            veiculoExistente.getEspecificacoes().forEach(e -> {
                if (e.getValor() != null) {
                    e.setValor(cryptoService.encrypt(e.getValor()));
                }
            });
        }

        Veiculo saved = repository.save(veiculoExistente);

        // Decrypt before returning
        if (saved.getEspecificacoes() != null) {
            saved.getEspecificacoes().forEach(e -> {
                if (e.getValor() != null) {
                    e.setValor(cryptoService.decrypt(e.getValor()));
                }
            });
        }

        return saved;
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