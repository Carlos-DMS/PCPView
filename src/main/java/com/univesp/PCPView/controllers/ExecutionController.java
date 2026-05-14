package com.univesp.PCPView.controllers;

import com.univesp.PCPView.dto.execution.request.ExecutionFinishRequestDTO;
import com.univesp.PCPView.dto.execution.request.ExecutionStartRequestDTO;
import com.univesp.PCPView.dto.execution.response.ExecutionResponseDTO;
import com.univesp.PCPView.infra.security.WebSecurityConfig;
import com.univesp.PCPView.services.ExecutionService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/execucoes")
@SecurityRequirement(name = WebSecurityConfig.SECURITY)
public class ExecutionController {

    private final ExecutionService executionService;

    public ExecutionController(ExecutionService executionService) {
        this.executionService = executionService;
    }

    @PostMapping("/iniciar")
    public ResponseEntity<ExecutionResponseDTO> iniciar(@RequestBody ExecutionStartRequestDTO body) {
        return ResponseEntity.status(HttpStatus.CREATED).body(executionService.iniciarExecucao(body));
    }

    @PutMapping("/finalizar")
    public ResponseEntity<ExecutionResponseDTO> finalizar(@RequestBody ExecutionFinishRequestDTO body) {
        return ResponseEntity.status(HttpStatus.OK).body(executionService.finalizarExecucao(body));
    }

    @GetMapping
    public ResponseEntity<List<ExecutionResponseDTO>> listarTodas() {
        return ResponseEntity.status(HttpStatus.OK).body(executionService.listarTodas());
    }
}