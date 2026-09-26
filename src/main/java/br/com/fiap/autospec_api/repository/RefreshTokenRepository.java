package br.com.fiap.autospec_api.repository;

import br.com.fiap.autospec_api.model.RefreshToken;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {

    Optional<RefreshToken> findByToken(String token);

    void deleteByUsuarioId(Long usuarioId);
}
