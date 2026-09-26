package br.com.fiap.autospec_api.config;

import br.com.fiap.autospec_api.model.Perfil;
import br.com.fiap.autospec_api.model.Usuario;
import br.com.fiap.autospec_api.repository.UsuarioRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;
import br.com.fiap.autospec_api.security.CryptoService;

@Configuration
public class CargaInicial {

    @Bean
        CommandLineRunner criarUsuarios(
            UsuarioRepository repository,
            PasswordEncoder passwordEncoder,
            CryptoService cryptoService) {

        return args -> {

            if (repository.findByEmail(
                    "admin@autospec.com").isEmpty()) {

                Usuario admin = new Usuario();

                admin.setNome(cryptoService.encrypt("Administrador"));
                admin.setEmail("admin@autospec.com");
                admin.setSenha(
                    passwordEncoder.encode("Admin123")
                );
                admin.setPerfil(Perfil.ADMIN);

                repository.save(admin);
            }

            if (repository.findByEmail(
                    "analista@autospec.com").isEmpty()) {

                Usuario analista = new Usuario();

                analista.setNome(cryptoService.encrypt("Analista"));
                analista.setEmail(
                    "analista@autospec.com"
                );
                analista.setSenha(
                    passwordEncoder.encode("Analista123")
                );
                analista.setPerfil(Perfil.ANALISTA);

                repository.save(analista);
            }
        };
    }
}