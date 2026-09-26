package br.com.fiap.autospec_api.service;

import br.com.fiap.autospec_api.model.Usuario;
import br.com.fiap.autospec_api.repository.UsuarioRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import br.com.fiap.autospec_api.security.CryptoService;
import org.springframework.stereotype.Service;

@Service
public class UsuarioService {

    private final UsuarioRepository repository;
    private final PasswordEncoder passwordEncoder;
    private final CryptoService cryptoService;

    public UsuarioService(
            UsuarioRepository repository,
            PasswordEncoder passwordEncoder,
            CryptoService cryptoService) {

        this.repository = repository;
        this.passwordEncoder = passwordEncoder;
        this.cryptoService = cryptoService;
    }

    public Usuario salvar(Usuario usuario) {

        // Hash password with BCrypt
        usuario.setSenha(
                passwordEncoder.encode(usuario.getSenha())
        );

        // Encrypt sensitive fields before persisting (nome)
        if (usuario.getNome() != null) {
            usuario.setNome(cryptoService.encrypt(usuario.getNome()));
        }

        return repository.save(usuario);
    }

}