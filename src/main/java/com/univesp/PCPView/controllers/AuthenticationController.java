package com.univesp.PCPView.controllers;

import com.univesp.PCPView.dto.authentication.LoginRequestDTO;
import com.univesp.PCPView.dto.authentication.LoginResponseDTO;
import com.univesp.PCPView.dto.authentication.RegisterRequestDTO;
import com.univesp.PCPView.service.AuthenticationService;
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
@Tag(name = "Authentication Controller", description = "Autentificação do usuário.")
public class AuthenticationController {

    private final AuthenticationService authenticationService;

    @PostMapping("/register")
    @Operation(summary = "Cadastra um novo usuário")
    @ApiResponse(responseCode = "201",description = "Usuário cadastrado com sucesso!")
    public ResponseEntity<?> register(@RequestBody @Valid RegisterRequestDTO body){
        authenticationService.register(body);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @PostMapping("/login")
    @Operation(summary = "Conecta usuário já cadastrado")
    @ApiResponse(responseCode = "202", description = "Usuário conectado com sucesso!")
    public ResponseEntity<LoginResponseDTO> login(@RequestBody @Valid LoginRequestDTO body){
        return ResponseEntity.status(HttpStatus.ACCEPTED).body((authenticationService.login(body)));
    }

    public AuthenticationController(AuthenticationService authenticationService) {
        this.authenticationService = authenticationService;
    }
}
