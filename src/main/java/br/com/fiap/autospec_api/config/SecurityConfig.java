package br.com.fiap.autospec_api.config;

import br.com.fiap.autospec_api.security.JwtAuthenticationFilter;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    public SecurityConfig(
            JwtAuthenticationFilter jwtAuthenticationFilter) {

        this.jwtAuthenticationFilter =
                jwtAuthenticationFilter;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(
            HttpSecurity http) throws Exception {

        http
                .csrf(csrf ->
                        csrf.disable()
                )

                .sessionManagement(session ->
                        session.sessionCreationPolicy(
                                SessionCreationPolicy.STATELESS
                        )
                )

                .exceptionHandling(exception ->
                        exception

                                .authenticationEntryPoint(
                                        (request,
                                         response,
                                         authException) -> {

                                            response.setStatus(
                                                    HttpServletResponse
                                                            .SC_UNAUTHORIZED
                                            );

                                            response.setContentType(
                                                    "application/json"
                                            );

                                            response.getWriter().write(
                                                    """
                                                    {
                                                      "status": 401,
                                                      "erros": [
                                                        "Autenticação necessária"
                                                      ]
                                                    }
                                                    """
                                            );
                                        }
                                )

                                .accessDeniedHandler(
                                        (request,
                                         response,
                                         accessDeniedException) -> {

                                            response.setStatus(
                                                    HttpServletResponse
                                                            .SC_FORBIDDEN
                                            );

                                            response.setContentType(
                                                    "application/json"
                                            );

                                            response.getWriter().write(
                                                    """
                                                    {
                                                      "status": 403,
                                                      "erros": [
                                                        "Acesso negado"
                                                      ]
                                                    }
                                                    """
                                            );
                                        }
                                )
                )

                .authorizeHttpRequests(auth ->
                        auth

                                // Públicos
                                .requestMatchers(
                                        "/api/auth/**",
                                        "/swagger-ui/**",
                                        "/swagger-ui.html",
                                        "/v3/api-docs/**"
                                )
                                .permitAll()

                                // Consultas
                                .requestMatchers(
                                        HttpMethod.GET,
                                        "/api/veiculos/**"
                                )
                                .hasAnyRole(
                                        "ADMIN",
                                        "ANALISTA"
                                )

                                // Criação e alteração
                                .requestMatchers(
                                        HttpMethod.POST,
                                        "/api/veiculos/**"
                                )
                                .hasRole("ADMIN")

                                .requestMatchers(
                                        HttpMethod.PUT,
                                        "/api/veiculos/**"
                                )
                                .hasRole("ADMIN")

                                // Exclusão
                                .requestMatchers(
                                        HttpMethod.DELETE,
                                        "/api/veiculos/**"
                                )
                                .hasRole("ADMIN")

                                // Todo o restante exige login
                                .anyRequest()
                                .authenticated()
                )

                .addFilterBefore(
                        jwtAuthenticationFilter,
                        UsernamePasswordAuthenticationFilter.class
                );

        return http.build();
    }
}