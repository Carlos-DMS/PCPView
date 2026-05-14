package com.univesp.PCPView.controllers;

import com.univesp.PCPView.dto.order.OrderRequestDTO;
import com.univesp.PCPView.dto.order.OrderResponseDTO;
import com.univesp.PCPView.infra.security.WebSecurityConfig;
import com.univesp.PCPView.services.OrderService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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
}
