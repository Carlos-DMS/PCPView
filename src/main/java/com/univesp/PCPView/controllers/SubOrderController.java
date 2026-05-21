package com.univesp.PCPView.controllers;

import com.univesp.PCPView.dto.subOrder.response.SubOrderResponseDTO;
import com.univesp.PCPView.models.enums.StatusProducaoEnum;
import com.univesp.PCPView.services.SubOrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/sub-ordens")
@Validated
@Tag(name = "Sub-Order Controller", description = "Operações relacionadas às Sub-Ordens (Etapas de Produção)")
public class SubOrderController {

    private final SubOrderService subOrderService;

    public SubOrderController(SubOrderService subOrderService) {
        this.subOrderService = subOrderService;
    }

    @PostMapping("/ordem/{numeroOrdem}")
    @Operation(summary = "Adiciona uma nova sub-ordem gerando a numeração sequencial automaticamente")
    @ApiResponse(responseCode = "201", description = "Sub-ordem criada com sucesso!")
    @ApiResponse(responseCode = "404", description = "Ordem pai não encontrada.")
    public ResponseEntity<SubOrderResponseDTO> criarNovaEtapa(
            @PathVariable String numeroOrdem,
            @RequestParam
            @NotBlank(message = "A letra da etapa não pode ser vazia")
            @Size(max = 1, message = "A letra deve ter apenas 1 caractere (Ex: A, B, C)") String letra) {
        return ResponseEntity.status(HttpStatus.CREATED).body(subOrderService.criarNovaEtapa(numeroOrdem, letra));
    }

    @GetMapping("/ordem/{numeroOrdem}")
    @Operation(summary = "Busca todas as sub-ordens (etapas) de uma ordem pai específica")
    @ApiResponse(responseCode = "200", description = "Lista de sub-ordens retornada com sucesso!")
    @ApiResponse(responseCode = "404", description = "Ordem pai não encontrada.")
    public ResponseEntity<List<SubOrderResponseDTO>> listarPorOrdemPai(@PathVariable String numeroOrdem) {
        return ResponseEntity.status(HttpStatus.OK).body(subOrderService.listarSubOrdensPorOrdemPai(numeroOrdem));
    }

    @PatchMapping("/status/{idSubOrdem}")
    @Operation(summary = "Altera manualmente o status de uma sub-ordem (etapa) específica (APENAS ADMINISTRADORES)")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    @ApiResponse(responseCode = "200", description = "Status da sub-ordem atualizado com sucesso!")
    @ApiResponse(responseCode = "404", description = "Sub-ordem não encontrada.")
    public ResponseEntity<SubOrderResponseDTO> alterarStatusSubOrdem(
            @PathVariable String idSubOrdem,
            @RequestParam StatusProducaoEnum status) {
        return ResponseEntity.status(HttpStatus.OK).body(subOrderService.alterarStatusSubOrdem(idSubOrdem, status));
    }

    @DeleteMapping("/{idSubOrdem}")
    @Operation(summary = "Deleta uma sub-ordem específica (apenas se estiver AGUARDANDO) / (APENAS ADMINISTRADORES)")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    @ApiResponse(responseCode = "204", description = "Sub-ordem deletada com sucesso!")
    @ApiResponse(responseCode = "400", description = "Não é possível excluir uma etapa que já entrou em produção.")
    @ApiResponse(responseCode = "404", description = "Sub-ordem não encontrada.")
    public ResponseEntity<Void> deletarSubOrdem(@PathVariable String idSubOrdem) {
        subOrderService.deletarSubOrdem(idSubOrdem);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }
}