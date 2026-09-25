package br.com.fiap.autospec_api.service;

import br.com.fiap.autospec_api.dto.LoginDTO;
import br.com.fiap.autospec_api.dto.TokenDTO;
import br.com.fiap.autospec_api.model.Usuario;
import br.com.fiap.autospec_api.repository.UsuarioRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import br.com.fiap.autospec_api.exception.CredenciaisInvalidasException;

@Service
public class AuthService {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

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
            throw new CredenciaisInvalidasException(
                    "E-mail ou senha inválidos"
            );
        }

        String token =
                jwtService.gerarToken(usuario);

        return new TokenDTO(
                token,
                "Bearer",
                jwtService.getExpiration()
        );
    }
}