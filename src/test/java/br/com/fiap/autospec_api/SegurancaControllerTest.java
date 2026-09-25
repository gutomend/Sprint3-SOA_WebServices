package br.com.fiap.autospec_api;

import br.com.fiap.autospec_api.model.Perfil;
import br.com.fiap.autospec_api.model.Usuario;
import br.com.fiap.autospec_api.repository.UsuarioRepository;
import br.com.fiap.autospec_api.service.JwtService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class SegurancaControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtService jwtService;

    private String tokenAdmin;
    private String tokenAnalista;

    @BeforeEach
    void prepararUsuarios() {

        Usuario admin = usuarioRepository
                .findByEmail("admin.teste@autospec.com")
                .orElseGet(() -> {

                    Usuario usuario = new Usuario();

                    usuario.setNome("Admin Teste");
                    usuario.setEmail("admin.teste@autospec.com");
                    usuario.setSenha(
                            passwordEncoder.encode("Admin123")
                    );
                    usuario.setPerfil(Perfil.ADMIN);

                    return usuarioRepository.save(usuario);
                });

        Usuario analista = usuarioRepository
                .findByEmail("analista.teste@autospec.com")
                .orElseGet(() -> {

                    Usuario usuario = new Usuario();

                    usuario.setNome("Analista Teste");
                    usuario.setEmail("analista.teste@autospec.com");
                    usuario.setSenha(
                            passwordEncoder.encode("Analista123")
                    );
                    usuario.setPerfil(Perfil.ANALISTA);

                    return usuarioRepository.save(usuario);
                });

        tokenAdmin = jwtService.gerarToken(admin);
        tokenAnalista = jwtService.gerarToken(analista);
    }

    @Test
    void deveRetornar401AoAcessarSemToken()
            throws Exception {

        mockMvc.perform(
                        get("/api/veiculos")
                )
                .andExpect(
                        status().isUnauthorized()
                );
    }

    @Test
    void analistaDeveConsultarVeiculos()
            throws Exception {

        mockMvc.perform(
                        get("/api/veiculos")
                                .header(
                                        "Authorization",
                                        "Bearer " + tokenAnalista
                                )
                )
                .andExpect(
                        status().isOk()
                );
    }

    @Test
    void analistaNaoDeveExcluirVeiculo()
            throws Exception {

        mockMvc.perform(
                        delete("/api/veiculos/999")
                                .header(
                                        "Authorization",
                                        "Bearer " + tokenAnalista
                                )
                )
                .andExpect(
                        status().isForbidden()
                );
    }

    @Test
    void adminDeveReceber404ParaVeiculoInexistente()
            throws Exception {

        mockMvc.perform(
                        get("/api/veiculos/999999")
                                .header(
                                        "Authorization",
                                        "Bearer " + tokenAdmin
                                )
                )
                .andExpect(
                        status().isNotFound()
                );
    }
}