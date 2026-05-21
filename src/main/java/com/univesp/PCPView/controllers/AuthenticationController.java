package com.univesp.PCPView.controllers;

import com.univesp.PCPView.dto.authentication.request.LoginRequestDTO;
import com.univesp.PCPView.dto.authentication.response.LoginResponseDTO;
import com.univesp.PCPView.dto.authentication.request.RegisterRequestDTO;
import com.univesp.PCPView.dto.authentication.response.UserResponseDTO;
import com.univesp.PCPView.services.AuthenticationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/auth")
@Tag(name = "Authentication Controller", description = "Autenticação do usuário.")
public class AuthenticationController {

    private final AuthenticationService authenticationService;

    public AuthenticationController(AuthenticationService authenticationService) {
        this.authenticationService = authenticationService;
    }

    @PostMapping
    @Operation(summary = "Cadastra um novo usuário")
    @ApiResponse(responseCode = "201", description = "Usuário cadastrado com sucesso!")
    @ApiResponse(responseCode = "409", description = "Usuário já cadastrado.")
    public ResponseEntity<?> registrar(@RequestBody @Valid RegisterRequestDTO body){
        authenticationService.registrar(body);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @PostMapping("/login")
    @Operation(summary = "Conecta usuário já cadastrado")
    @ApiResponse(responseCode = "202", description = "Usuário conectado com sucesso!")
    public ResponseEntity<LoginResponseDTO> login(@RequestBody @Valid LoginRequestDTO body){
        return ResponseEntity.status(HttpStatus.ACCEPTED).body((authenticationService.login(body)));
    }

    @GetMapping
    @Operation(summary = "Lista todos os usuários cadastrados (APENAS ADMINISTRADORES)")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    @ApiResponse(responseCode = "200", description = "Lista de usuários retornada com sucesso!")
    @ApiResponse(responseCode = "403", description = "Acesso negado. Apenas administradores podem acessar esta rota.")
    public ResponseEntity<List<UserResponseDTO>> listarUsuarios() {
        return ResponseEntity.status(HttpStatus.OK).body(authenticationService.listarUsuarios());
    }

    @PatchMapping("/{id}/promote")
    @Operation(summary = "Promove um usuário comum para Administrador (APENAS ADMINISTRADORES)")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    @ApiResponse(responseCode = "200", description = "Usuário promovido a ADMIN com sucesso!")
    @ApiResponse(responseCode = "403", description = "Acesso negado. Apenas administradores podem promover usuários.")
    @ApiResponse(responseCode = "404", description = "Usuário não encontrado.")
    public ResponseEntity<UserResponseDTO> promoverParaAdmin(@PathVariable UUID id) {
        return ResponseEntity.status(HttpStatus.OK).body(authenticationService.promoverParaAdmin(id));
    }
}
