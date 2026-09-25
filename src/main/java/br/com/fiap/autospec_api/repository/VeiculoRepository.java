package br.com.fiap.autospec_api.repository;

import br.com.fiap.autospec_api.model.Veiculo;
import org.springframework.data.jpa.repository.JpaRepository;

public interface VeiculoRepository extends JpaRepository<Veiculo, Long> {
}