package com.univesp.PCPView.controllers;

import com.univesp.PCPView.dto.order.OrderRequestDTO;
import com.univesp.PCPView.dto.order.OrderResponseDTO;
import com.univesp.PCPView.dto.subOrder.QueueMachineRequestDTO;
import com.univesp.PCPView.dto.subOrder.QueueReorderRequestDTO;
import com.univesp.PCPView.infra.security.WebSecurityConfig;
import com.univesp.PCPView.services.OrderService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/ordens")
@SecurityRequirement(name = WebSecurityConfig.SECURITY)
public class OrderController {

    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @PostMapping
    public ResponseEntity<OrderResponseDTO> criarOrdemComEtapas(@RequestBody OrderRequestDTO body) {
        OrderResponseDTO response = orderService.criarOrdemComEtapas(body);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    public ResponseEntity<List<OrderResponseDTO>> listarTodas() {
        return ResponseEntity.status(HttpStatus.OK).body(orderService.listarTodas());
    }

    @DeleteMapping("/{numeroOrdem}")
    public ResponseEntity<?> deletarOrdem(@PathVariable String numeroOrdem) {
        orderService.deletarOrdem(numeroOrdem);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    @PatchMapping("/subordens/{codigoEtapa}/fila")
    public ResponseEntity<OrderResponseDTO> alterarMaquinaIdealDaSubOrdem(@PathVariable String codigoEtapa, @RequestBody QueueMachineRequestDTO body) {
        return ResponseEntity.status(HttpStatus.OK).body(orderService.alterarMaquinaIdealDaSubOrdem(codigoEtapa, body));
    }

    @DeleteMapping("/subordens/{codigoEtapa}/fila")
    public ResponseEntity<OrderResponseDTO> removerSubOrdemDaFila(@PathVariable String codigoEtapa) {
        return ResponseEntity.status(HttpStatus.OK).body(orderService.removerSubOrdemDaFila(codigoEtapa));
    }

    @PatchMapping("/subordens/fila/reordenar")
    public ResponseEntity<?> reordenarFila(@RequestBody QueueReorderRequestDTO body) {
        orderService.reordenarFila(body);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }
}
