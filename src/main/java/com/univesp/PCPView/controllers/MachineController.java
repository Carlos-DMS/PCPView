package com.univesp.PCPView.controllers;

import com.univesp.PCPView.dto.machine.request.MachineRequestDTO;
import com.univesp.PCPView.dto.machine.request.UpdateMachineNameDTO;
import com.univesp.PCPView.dto.machine.response.MachineResponseDTO;
import com.univesp.PCPView.infra.security.WebSecurityConfig;
import com.univesp.PCPView.services.MachineService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/machine")
@Tag(name = "Machine Controller", description = "Operações relacionadas as máquinas.")
@SecurityRequirement(name = WebSecurityConfig.SECURITY)
public class MachineController {

    private final MachineService machineService;

    public MachineController(MachineService machineService) {
        this.machineService = machineService;
    }

    @PostMapping
    @Operation(summary = "Cadastra uma novo máquina")
    @ApiResponse(responseCode = "201",description = "Máquina cadastrada com sucesso!")
    @ApiResponse(responseCode = "400", description = "Requisição inválida.")
    public ResponseEntity<MachineResponseDTO> registrarMaquina(@RequestBody @Valid MachineRequestDTO body) {
        return ResponseEntity.status(HttpStatus.CREATED).body(machineService.registrarMaquina(body));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Procura uma máquina pelo seu ID")
    @ApiResponse(responseCode = "200",description = "Máquina encontrada com sucesso!")
    @ApiResponse(responseCode = "404",description = "A máquina não existe.")
    public ResponseEntity<MachineResponseDTO> buscarMaquinaPorID(@PathVariable(value = "id") String id) {
        return ResponseEntity.status(HttpStatus.OK).body(machineService.buscarMaquinaPorID(id));
    }

    @GetMapping
    @Operation(summary = "Procura todas as máquinas, podendo ser filtradas via parâmetro pelo status operacional")
    @ApiResponse(responseCode = "200",description = "Máquinas encontradas com sucesso!")
    public ResponseEntity<List<MachineResponseDTO>> buscarTodasMaquinas(@RequestParam(required = false) Boolean operacional) {
        if (operacional != null) {
            return ResponseEntity.status(HttpStatus.OK).body(machineService.buscarMaquinasPorStatusOperacional(operacional));
        }

        return ResponseEntity.status(HttpStatus.OK).body(machineService.buscarTodasMaquinas());
    }

    @PatchMapping("/toggleOperationalStatus/{id}")
    @Operation(summary = "Alterna o status operacional de uma máquina")
    @ApiResponse(responseCode = "200",description = "Máquina atualizada com sucesso!")
    @ApiResponse(responseCode = "404",description = "A máquina não existe.")
    public ResponseEntity<MachineResponseDTO> alternarStatusOperacional(@PathVariable(value = "id") String id){
        return ResponseEntity.status(HttpStatus.OK).body(machineService.alternarStatusOperacional(id));
    }

    @PatchMapping("/updateName/{id}")
    @Operation(summary = "Alterna o nome de uma máquina")
    @ApiResponse(responseCode = "200",description = "Máquina atualizada com sucesso!")
    @ApiResponse(responseCode = "404",description = "A máquina não existe.")
    public ResponseEntity<MachineResponseDTO> alterarNome(@PathVariable(value = "id") String id, @RequestBody @Valid UpdateMachineNameDTO body) {
        return ResponseEntity.status(HttpStatus.OK).body(machineService.alterarNome(id, body));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Deleta uma máquina pelo seu ID")
    @ApiResponse(responseCode = "204",description = "Máquina deletada com sucesso!")
    @ApiResponse(responseCode = "404",description = "A máquina não existe.")
    public ResponseEntity<?> deletarMaquina(@PathVariable(value = "id") String id) {
        machineService.deletarMaquina(id);

        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }
}
