package br.com.fiap.autospec_api.controller;

import br.com.fiap.autospec_api.dto.PadronizacaoEntradaDTO;
import br.com.fiap.autospec_api.dto.PadronizacaoSaidaDTO;
import br.com.fiap.autospec_api.service.PadronizacaoService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/padronizacao")
public class PadronizacaoController {

    private final PadronizacaoService service;

    public PadronizacaoController(PadronizacaoService service) {
        this.service = service;
    }

    @Operation(summary = "Padronizar atributo técnico")
    @PostMapping
        @PreAuthorize("hasAnyRole('ADMIN','ANALISTA','ADMINISTRADOR','BRIGADISTA','GESTOR')")
        public PadronizacaoSaidaDTO padronizar(
            @RequestBody @NotNull @Valid PadronizacaoEntradaDTO dto) {

        String resultado =
                service.padronizar(dto.getAtributo());

        return new PadronizacaoSaidaDTO(
                dto.getAtributo(),
                resultado
        );
    }
}