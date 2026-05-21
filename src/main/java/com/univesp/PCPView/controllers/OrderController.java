package com.univesp.PCPView.controllers;

import com.univesp.PCPView.dto.order.request.OrderRequestDTO;
import com.univesp.PCPView.dto.order.response.OrderResponseDTO;
import com.univesp.PCPView.dto.order.response.SimpleOrderResponseDTO;
import com.univesp.PCPView.models.enums.StatusProducaoEnum;
import com.univesp.PCPView.services.OrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/ordens")
@Validated
@Tag(name = "Order Controller", description = "Operações relacionadas às Ordens de Produção")
public class OrderController {

    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @PostMapping
    @Operation(summary = "Cria uma nova ordem principal e suas sub-ordens (etapas)")
    @ApiResponse(responseCode = "201", description = "Ordem e etapas criadas com sucesso!")
    @ApiResponse(responseCode = "400", description = "Erro na requisição ou dados inválidos.")
    public ResponseEntity<OrderResponseDTO> criarOrdemComEtapas(@RequestBody @Valid OrderRequestDTO body) {
        return ResponseEntity.status(HttpStatus.CREATED).body(orderService.criarOrdemComEtapas(body));
    }

    @GetMapping
    @Operation(summary = "Busca todas as ordens, podendo ser filtradas por status")
    @ApiResponse(responseCode = "200", description = "Lista de ordens retornada com sucesso!")
    public ResponseEntity<List<OrderResponseDTO>> buscarTodasAsOrdens(@RequestParam(required = false) StatusProducaoEnum status) {
        return ResponseEntity.status(HttpStatus.OK).body(orderService.buscarTodasAsOrdens(status));
    }

    @PatchMapping("/status/{codigoOrdem}")
    @Operation(summary = "Altera o status de uma ordem principal (e reflete nas sub-ordens se cancelada/finalizada)")
    @ApiResponse(responseCode = "200", description = "Status atualizado com sucesso!")
    @ApiResponse(responseCode = "404", description = "A ordem não existe.")
    public ResponseEntity<OrderResponseDTO> alterarStatusOrdem(
            @PathVariable String codigoOrdem,
            @RequestParam StatusProducaoEnum status)
    {
        return ResponseEntity.status(HttpStatus.OK).body(orderService.alterarStatusOrdem(status, codigoOrdem));
    }

    @PatchMapping("/quantidade/{codigoOrdem}")
    @Operation(summary = "Altera a quantidade total de uma ordem e de suas sub-ordens")
    @ApiResponse(responseCode = "200", description = "Quantidade atualizada com sucesso!")
    @ApiResponse(responseCode = "400", description = "Erro de validação (quantidade menor que 1).")
    @ApiResponse(responseCode = "404", description = "A ordem não existe.")
    public ResponseEntity<SimpleOrderResponseDTO> alterarQuantidadeOrdem(
            @PathVariable String codigoOrdem,
            @RequestParam @Min(value = 1, message = "A quantidade deve ser de no mínimo 1") Integer quantidade)
    {
        return ResponseEntity.status(HttpStatus.OK).body(orderService.alterarQuantidadeOrdem(quantidade, codigoOrdem));
    }

    @PatchMapping("/prioridade/{codigoOrdem}")
    @Operation(summary = "Altera a prioridade de uma ordem (1 é a maior, 5 é a menor) (APENAS ADMINISTRADORES)")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    @ApiResponse(responseCode = "200", description = "Prioridade atualizada com sucesso!")
    @ApiResponse(responseCode = "400", description = "Erro de validação (prioridade fora do limite).")
    @ApiResponse(responseCode = "403", description = "Acesso negado. Apenas administradores podem alterar a prioridade.")
    @ApiResponse(responseCode = "404", description = "A ordem não existe.")
    public ResponseEntity<SimpleOrderResponseDTO> alterarPrioridadeOrdem(
            @PathVariable String codigoOrdem,
            @RequestParam
            @Min(value = 1, message = "A prioridade máxima é 1")
            @Max(value = 5, message = "A prioridade mínima é 5") Integer prioridade)
    {
        return ResponseEntity.status(HttpStatus.OK).body(orderService.alterarPrioridadeOrdem(prioridade, codigoOrdem));
    }

    @DeleteMapping("/{codigoOrdem}")
    @Operation(summary = "Deleta uma ordem e suas sub-ordens (apenas se estiver AGUARDANDO) / (APENAS ADMINISTRADORES)")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    @ApiResponse(responseCode = "204", description = "Ordem deletada com sucesso!")
    @ApiResponse(responseCode = "400", description = "Não é possível excluir uma ordem que já entrou em produção.")
    @ApiResponse(responseCode = "403", description = "Acesso negado. Apenas administradores podem excluir ordens.")
    @ApiResponse(responseCode = "404", description = "A ordem não existe.")
    public ResponseEntity<Void> deletarOrdemESuasSubOrdens(@PathVariable String codigoOrdem) {
        orderService.deletarOrdemESuasSubOrdens(codigoOrdem);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }
}