package br.com.fiap.autospec_api.service;

import br.com.fiap.autospec_api.dto.LoginDTO;
import br.com.fiap.autospec_api.dto.TokenDTO;
import br.com.fiap.autospec_api.model.Usuario;
import br.com.fiap.autospec_api.repository.UsuarioRepository;
import br.com.fiap.autospec_api.model.RefreshToken;
import br.com.fiap.autospec_api.repository.RefreshTokenRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import br.com.fiap.autospec_api.exception.CredenciaisInvalidasException;

@Service
public class AuthService {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
        private final RefreshTokenRepository refreshTokenRepository;

        public AuthService(
                        UsuarioRepository usuarioRepository,
                        PasswordEncoder passwordEncoder,
                        JwtService jwtService,
                        RefreshTokenRepository refreshTokenRepository) {

                this.usuarioRepository = usuarioRepository;
                this.passwordEncoder = passwordEncoder;
                this.jwtService = jwtService;
                this.refreshTokenRepository = refreshTokenRepository;
        }

    public TokenDTO login(LoginDTO login) {

        Usuario usuario = usuarioRepository
                .findByEmail(login.getEmail())
                .orElseThrow(() ->
                        new CredenciaisInvalidasException(
                                "E-mail ou senha inválidos"
                        )
                );

        boolean senhaCorreta =
                passwordEncoder.matches(
                        login.getSenha(),
                        usuario.getSenha()
                );

        if (!senhaCorreta) {
            throw new CredenciaisInvalidasException(
                    "E-mail ou senha inválidos"
            );
        }

        String token = jwtService.gerarToken(usuario);
        String refresh = jwtService.gerarRefreshToken(usuario);

        // persist refresh token
        RefreshToken refreshTokenEntity = new RefreshToken(
                refresh,
                usuario,
                java.time.Instant.now().plusMillis(jwtService.getRefreshExpiration())
        );

        refreshTokenRepository.save(refreshTokenEntity);

        return new TokenDTO(
                token,
                "Bearer",
                jwtService.getExpiration(),
                refresh,
                jwtService.getRefreshExpiration()
        );
    }

    public TokenDTO refresh(String refreshToken) {
        RefreshToken tokenEntity = refreshTokenRepository.findByToken(refreshToken)
                .orElseThrow(() -> new CredenciaisInvalidasException("Refresh token inválido"));

        if (tokenEntity.getExpiracao().isBefore(java.time.Instant.now())) {
            refreshTokenRepository.delete(tokenEntity);
            throw new CredenciaisInvalidasException("Refresh token expirado");
        }

        Usuario usuario = tokenEntity.getUsuario();
        String newAccess = jwtService.gerarToken(usuario);
        String newRefresh = jwtService.gerarRefreshToken(usuario);

        // rotate refresh token
        tokenEntity.setToken(newRefresh);
        tokenEntity.setExpiracao(java.time.Instant.now().plusMillis(jwtService.getRefreshExpiration()));
        refreshTokenRepository.save(tokenEntity);

        return new TokenDTO(
                newAccess,
                "Bearer",
                jwtService.getExpiration(),
                newRefresh,
                jwtService.getRefreshExpiration()
        );
    }
}