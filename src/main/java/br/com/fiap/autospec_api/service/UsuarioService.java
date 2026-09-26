package br.com.fiap.autospec_api.service;

import br.com.fiap.autospec_api.model.Usuario;
import br.com.fiap.autospec_api.repository.UsuarioRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.context.SecurityContextHolder;
import java.util.Optional;

@Service
public class UsuarioService {

    private final UsuarioRepository repository;
    private final PasswordEncoder passwordEncoder;
    private static final Logger logger = LoggerFactory.getLogger(UsuarioService.class);

    public UsuarioService(
            UsuarioRepository repository,
            PasswordEncoder passwordEncoder) {

        this.repository = repository;
        this.passwordEncoder = passwordEncoder;
    }

    public Usuario salvar(Usuario usuario) {

        boolean isUpdate = usuario.getId() != null;

        Optional<Usuario> existente = Optional.empty();
        if (isUpdate) {
            existente = repository.findById(usuario.getId());
        }

        usuario.setSenha(
                passwordEncoder.encode(usuario.getSenha())
        );

        Usuario saved = repository.save(usuario);

        // Log profile change if applicable
        if (existente.isPresent()) {
            Usuario old = existente.get();
            if (old.getPerfil() != null && usuario.getPerfil() != null && !old.getPerfil().equals(usuario.getPerfil())) {
                String actor = SecurityContextHolder.getContext().getAuthentication() != null
                        ? SecurityContextHolder.getContext().getAuthentication().getName()
                        : "system";
                logger.info("event=profile_change userId={} email={} oldProfile={} newProfile={} actor={}",
                        saved.getId(), saved.getEmail(), old.getPerfil(), saved.getPerfil(), actor);
            }
        } else {
            logger.info("event=user_created userId={} email={} profile={}", saved.getId(), saved.getEmail(), saved.getPerfil());
        }

        return saved;
    }

}