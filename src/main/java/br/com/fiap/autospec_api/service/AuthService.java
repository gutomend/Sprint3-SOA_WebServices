package br.com.fiap.autospec_api.service;

import br.com.fiap.autospec_api.dto.LoginDTO;
import br.com.fiap.autospec_api.dto.TokenDTO;
import br.com.fiap.autospec_api.model.Usuario;
import br.com.fiap.autospec_api.repository.UsuarioRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.context.SecurityContextHolder;

import br.com.fiap.autospec_api.exception.CredenciaisInvalidasException;

@Service
public class AuthService {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;
        private final JwtService jwtService;
        private static final Logger logger = LoggerFactory.getLogger(AuthService.class);

    public AuthService(
            UsuarioRepository usuarioRepository,
            PasswordEncoder passwordEncoder,
            JwtService jwtService) {

        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
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
            logger.warn("event=login_failed email={} reason=invalid_credentials", login.getEmail());
            throw new CredenciaisInvalidasException(
                    "E-mail ou senha inválidos"
            );
        }

        String token = jwtService.gerarToken(usuario);

        // log successful login (do not log sensitive data)
        String actor = (SecurityContextHolder.getContext().getAuthentication() != null)
                ? SecurityContextHolder.getContext().getAuthentication().getName()
                : login.getEmail();
        logger.info("event=login_success actor={} email={} profile={}", actor, usuario.getEmail(), usuario.getPerfil());

        return new TokenDTO(
                token,
                "Bearer",
                jwtService.getExpiration()
        );
    }
}