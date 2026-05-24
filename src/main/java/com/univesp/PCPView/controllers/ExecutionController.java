package com.univesp.PCPView.controllers;

import com.univesp.PCPView.dto.execution.request.ExecutionStartRequestDTO;
import com.univesp.PCPView.dto.execution.response.ExecutionResponseDTO;
import com.univesp.PCPView.infra.security.WebSecurityConfig;
import com.univesp.PCPView.models.enums.ExecutionStatus;
import com.univesp.PCPView.services.ExecutionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/execucoes")
@Validated
@SecurityRequirement(name = WebSecurityConfig.SECURITY)
@Tag(name = "Execution Controller", description = "Operações de Apontamento de Produção (Painel da Máquina)")
public class ExecutionController {

    private final ExecutionService executionService;

    public ExecutionController(ExecutionService executionService) {
        this.executionService = executionService;
    }

    @PostMapping("/iniciar")
    @Operation(summary = "Inicia a execução de uma etapa em uma máquina")
    @ApiResponse(responseCode = "201", description = "Sessão de produção iniciada com sucesso!")
    @ApiResponse(responseCode = "400", description = "A quantidade solicitada já foi atingida ou etapa bloqueada.")
    @ApiResponse(responseCode = "404", description = "Máquina ou etapa (sub-ordem) não encontrada.")
    public ResponseEntity<ExecutionResponseDTO> iniciar(@RequestBody @Valid ExecutionStartRequestDTO body) {
        return ResponseEntity.status(HttpStatus.CREATED).body(executionService.iniciarExecucao(body));
    }

    @PatchMapping("/finalizar/{idExecucao}")
    @Operation(summary = "Finaliza uma execução em andamento contabilizando as peças feitas (APENAS OPERADOR RESPONSÁVEL/ADMINISTRADORES)")
    @ApiResponse(responseCode = "200", description = "Execução finalizada com sucesso!")
    @ApiResponse(responseCode = "400", description = "Execução não está rodando ou limite de peças excedido.")
    @ApiResponse(responseCode = "403", description = "Apenas o operador dono da sessão ou um Admin podem finalizá-la.")
    @ApiResponse(responseCode = "404", description = "Execução não encontrada.")
    public ResponseEntity<ExecutionResponseDTO> finalizar(
            @PathVariable UUID idExecucao,
            @RequestParam
            @NotNull(message = "A quantidade produzida é obrigatória.")
            @Min(value = 0, message = "A quantidade não pode ser negativa.") Integer quantidadeProduzida)
    {
        return ResponseEntity.status(HttpStatus.OK).body(executionService.finalizarExecucao(idExecucao, quantidadeProduzida));
    }

    @GetMapping
    @Operation(summary = "Lista as execuções, permitindo filtrar por status, máquina e/ou ordem principal")
    @ApiResponse(responseCode = "200", description = "Lista retornada com sucesso!")
    public ResponseEntity<List<ExecutionResponseDTO>> listarTodas(
            @RequestParam(required = false) ExecutionStatus status,
            @RequestParam(required = false) String maquinaId,
            @RequestParam(required = false) String ordemId)
    {
        return ResponseEntity.status(HttpStatus.OK).body(executionService.listarTodas(maquinaId, status, ordemId));
    }

    @DeleteMapping("/{idExecucao}")
    @Operation(summary = "Cancela e exclui uma execução iniciada por engano (Apenas se estiver RODANDO) / (APENAS OPERADOR RESPONSÁVEL/ADMINISTRADORES)")
    @ApiResponse(responseCode = "204", description = "Execução cancelada e excluída com sucesso!")
    @ApiResponse(responseCode = "400", description = "A execução já foi finalizada e não pode ser apagada.")
    @ApiResponse(responseCode = "403", description = "Apenas o operador que iniciou a execução ou um administrador podem cancelá-la.")
    @ApiResponse(responseCode = "404", description = "Execução não encontrada.")
    public ResponseEntity<Void> cancelarExecucao(@PathVariable UUID idExecucao) {
        executionService.cancelarExecucao(idExecucao);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }
}