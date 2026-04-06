package com.univesp.PCPView.controllers;

import com.univesp.PCPView.dto.authentication.request.LoginRequestDTO;
import com.univesp.PCPView.dto.authentication.response.LoginResponseDTO;
import com.univesp.PCPView.dto.authentication.request.RegisterRequestDTO;
import com.univesp.PCPView.services.AuthenticationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
    @ApiResponse(responseCode = "201",description = "Usuário cadastrado com sucesso!")
    @ApiResponse(responseCode = "409",description = "Usuário já cadastrado.")
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
}
