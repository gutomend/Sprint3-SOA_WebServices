package br.com.fiap.autospec_api;

import br.com.fiap.autospec_api.model.Perfil;
import br.com.fiap.autospec_api.model.Usuario;
import br.com.fiap.autospec_api.repository.UsuarioRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @BeforeEach
    void prepararUsuario() {

        if (usuarioRepository
                .findByEmail("teste@autospec.com")
                .isEmpty()) {

            Usuario usuario = new Usuario();

            usuario.setNome("Usuário Teste");
            usuario.setEmail("teste@autospec.com");
            usuario.setSenha(
                    passwordEncoder.encode("Teste123")
            );
            usuario.setPerfil(Perfil.ADMIN);

            usuarioRepository.save(usuario);
        }
    }

    @Test
    void deveRealizarLoginComSucesso()
            throws Exception {

        String json = """
                {
                    "email": "teste@autospec.com",
                    "senha": "Teste123"
                }
                """;

        mockMvc.perform(
                        post("/api/auth/login")
                                .contentType(
                                        MediaType.APPLICATION_JSON
                                )
                                .content(json)
                )
                .andExpect(status().isOk())
                .andExpect(
                        jsonPath("$.token").exists()
                )
                .andExpect(
                        jsonPath("$.tipo")
                                .value("Bearer")
                )
                .andExpect(
                        jsonPath("$.expiraEm")
                                .value(3600000)
                );
    }

    @Test
    void deveRetornar401QuandoSenhaForIncorreta()
            throws Exception {

        String json = """
                {
                    "email": "teste@autospec.com",
                    "senha": "senhaerrada"
                }
                """;

        mockMvc.perform(
                        post("/api/auth/login")
                                .contentType(
                                        MediaType.APPLICATION_JSON
                                )
                                .content(json)
                )
                .andExpect(
                        status().isUnauthorized()
                );
    }
}