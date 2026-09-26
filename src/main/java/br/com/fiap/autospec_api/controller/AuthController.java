package br.com.fiap.autospec_api.controller;

import br.com.fiap.autospec_api.dto.LoginDTO;
import br.com.fiap.autospec_api.dto.TokenDTO;
import br.com.fiap.autospec_api.dto.RefreshRequest;
import br.com.fiap.autospec_api.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/api/auth")
@Tag(
        name = "Autenticação",
        description = "Operações de autenticação e geração de token JWT"
)
public class AuthController {

    private final AuthService authService;

    public AuthController(
            AuthService authService) {

        this.authService = authService;
    }

    @Operation(
            summary = "Realizar login",
            description = "Autentica o usuário e retorna um token JWT"
    )
    @PostMapping("/login")
    public ResponseEntity<TokenDTO> login(
            @RequestBody @NotNull @Valid LoginDTO login) {

        return ResponseEntity.ok(
                authService.login(login)
        );
    }

        @Operation(summary = "Trocar refresh token por novo access token")
        @PostMapping("/refresh")
        public ResponseEntity<TokenDTO> refresh(
                        @RequestBody @NotNull @Valid RefreshRequest request) {

                return ResponseEntity.ok(
                                authService.refresh(request.getRefreshToken())
                );
        }
}