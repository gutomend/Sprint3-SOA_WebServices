package br.com.fiap.autospec_api.controller;

import br.com.fiap.autospec_api.dto.VeiculoRespostaDTO;
import br.com.fiap.autospec_api.service.ConsultaTecnicaService;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/consulta-tecnica")
public class ConsultaTecnicaController {

    private final ConsultaTecnicaService service;

    public ConsultaTecnicaController(
            ConsultaTecnicaService service) {

        this.service = service;
    }

    @Operation(summary = "Consultar especificações técnicas padronizadas")
    @GetMapping("/{marca}/{modelo}/{versao}")
    public VeiculoRespostaDTO consultar(
            @PathVariable String marca,
            @PathVariable String modelo,
            @PathVariable String versao) {

        return service.consultar(
                marca,
                modelo,
                versao
        );
    }
}