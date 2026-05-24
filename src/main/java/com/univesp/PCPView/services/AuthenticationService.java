package com.univesp.PCPView.services;

import com.univesp.PCPView.dto.authentication.request.LoginRequestDTO;
import com.univesp.PCPView.dto.authentication.response.LoginResponseDTO;
import com.univesp.PCPView.dto.authentication.request.RegisterRequestDTO;
import com.univesp.PCPView.dto.authentication.response.UserResponseDTO;
import com.univesp.PCPView.exceptions.NonExistentUserException;
import com.univesp.PCPView.exceptions.UserAlreadyExistsException;
import com.univesp.PCPView.models.UserModel;
import com.univesp.PCPView.models.enums.RoleEnum;
import com.univesp.PCPView.repository.UserRepository;
import jakarta.transaction.Transactional;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class AuthenticationService {

    private final AuthenticationManager authenticationManager;

    private final UserRepository userRepository;

    private final TokenService tokenService;

    public AuthenticationService(AuthenticationManager authenticationManager, UserRepository repository, TokenService tokenService) {
        this.authenticationManager = authenticationManager;
        this.userRepository = repository;
        this.tokenService = tokenService;
    }

    public LoginResponseDTO login(LoginRequestDTO body) {
        UsernamePasswordAuthenticationToken usernamePassword = new UsernamePasswordAuthenticationToken(body.email(), body.password());
        Authentication auth = this.authenticationManager.authenticate(usernamePassword);

        return new LoginResponseDTO(tokenService.generateToken((UserModel) auth.getPrincipal()));
    }

    @Transactional
    public void registrar(RegisterRequestDTO body) {
        if(this.userRepository.findByEmail(body.email()) == null) {

            String encryptedPassword = new BCryptPasswordEncoder().encode(body.password());
            UserModel user = new UserModel(body.userName(), body.email(), encryptedPassword);

            userRepository.save(user);

        } else {
            throw new UserAlreadyExistsException();
        }
    }

    public List<UserResponseDTO> listarUsuarios() {
        return userRepository.findAll().stream()
                .map(u -> new UserResponseDTO(u.getId(), u.getUsername(), u.getRole()))
                .toList();
    }

    @Transactional
    public UserResponseDTO promoverParaAdmin(UUID id) {
        UserModel user = userRepository.findById(id)
                .orElseThrow(NonExistentUserException::new);

        user.setRole(RoleEnum.ADMIN);

        userRepository.save(user);

        return new UserResponseDTO(user.getId(), user.getUsername(), user.getRole());
    }

    @Transactional
    public void desativarUsuario(UUID id) {
        UserModel user = userRepository.findById(id)
                .orElseThrow(NonExistentUserException::new);

        if (!user.isEnabled()) {
            throw new RuntimeException("Este usuário já está desativado.");
        }

        user.setAtivo(false);
        userRepository.save(user);
    }

    public UserModel extractUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return (UserModel) authentication.getPrincipal();
    }
}
