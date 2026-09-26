package br.com.fiap.autospec_api.controller;

import br.com.fiap.autospec_api.dto.EspecificacaoDTO;
import br.com.fiap.autospec_api.dto.VeiculoDTO;
import br.com.fiap.autospec_api.model.Especificacao;
import br.com.fiap.autospec_api.model.Veiculo;
import br.com.fiap.autospec_api.service.VeiculoService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;

import java.util.List;
@RestController
@RequestMapping("/api/veiculos")
@Tag(
        name = "Veículos",
        description = "Operações de cadastro, consulta, atualização e exclusão de veículos"
)
@SecurityRequirement(name = "bearerAuth")
public class VeiculoController {

    private final VeiculoService service;

    public VeiculoController(VeiculoService service) {
        this.service = service;
    }

    @Operation(summary = "Cadastrar veículo com especificações")
        @PostMapping
        @PreAuthorize("hasAnyRole('ADMIN','ADMINISTRADOR','GESTOR')")
        public ResponseEntity<Veiculo> criar(
            @RequestBody @NotNull @Valid VeiculoDTO dto) {

        Veiculo veiculo = converterVeiculo(dto);

        Veiculo veiculoSalvo = service.salvar(veiculo);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(veiculoSalvo);
    }

    @Operation(summary = "Listar todos os veículos")
    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN','ANALISTA','ADMINISTRADOR','BRIGADISTA','GESTOR')")
    public ResponseEntity<List<Veiculo>> listarTodos() {

        return ResponseEntity.ok(
                service.listarTodos()
        );
    }

    @Operation(summary = "Buscar veículo por ID")
    @GetMapping("/{id}")
        @PreAuthorize("hasAnyRole('ADMIN','ANALISTA','ADMINISTRADOR','BRIGADISTA','GESTOR')")
        public ResponseEntity<Veiculo> buscarPorId(
            @PathVariable Long id) {

        return ResponseEntity.ok(
                service.buscarPorId(id)
        );
    }

    @Operation(summary = "Atualizar veículo")
    @PutMapping("/{id}")
        @PreAuthorize("hasAnyRole('ADMIN','ADMINISTRADOR','GESTOR')")
        public ResponseEntity<Veiculo> atualizar(
            @PathVariable Long id,
            @RequestBody @NotNull @Valid VeiculoDTO dto) {

        Veiculo veiculo = converterVeiculo(dto);

        return ResponseEntity.ok(
                service.atualizar(id, veiculo)
        );
    }

    @Operation(summary = "Excluir veículo")
    @DeleteMapping("/{id}")
        @PreAuthorize("hasAnyRole('ADMIN','ADMINISTRADOR')")
        public ResponseEntity<Void> excluir(
            @PathVariable Long id) {

        service.excluir(id);

        return ResponseEntity.noContent().build();
    }

    private Veiculo converterVeiculo(VeiculoDTO dto) {

        Veiculo veiculo = new Veiculo();

        veiculo.setMarca(dto.getMarca());
        veiculo.setModelo(dto.getModelo());
        veiculo.setVersao(dto.getVersao());

        if (dto.getEspecificacoes() != null) {

            List<Especificacao> especificacoes =
                    dto.getEspecificacoes()
                            .stream()
                            .map(this::converterEspecificacao)
                            .toList();

            veiculo.setEspecificacoes(especificacoes);
        }

        return veiculo;
    }

    private Especificacao converterEspecificacao(
            EspecificacaoDTO dto) {

        Especificacao especificacao = new Especificacao();

        especificacao.setAtributo(dto.getAtributo());
        especificacao.setValor(dto.getValor());

        return especificacao;
    }
}