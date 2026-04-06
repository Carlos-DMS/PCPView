package com.univesp.PCPView.service;

import com.univesp.PCPView.dto.authentication.LoginRequestDTO;
import com.univesp.PCPView.dto.authentication.LoginResponseDTO;
import com.univesp.PCPView.dto.authentication.RegisterRequestDTO;
import com.univesp.PCPView.exceptions.UserAlreadyExistsException;
import com.univesp.PCPView.models.UserModel;
import com.univesp.PCPView.repository.UserRepository;
import jakarta.transaction.Transactional;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthenticationService {

    private final AuthenticationManager authenticationManager;

    private final UserRepository repository;

    private final TokenService tokenService;

    public LoginResponseDTO login(LoginRequestDTO body) {
        UsernamePasswordAuthenticationToken usernamePassword = new UsernamePasswordAuthenticationToken(body.email(), body.password());
        Authentication auth = this.authenticationManager.authenticate(usernamePassword);

        return new LoginResponseDTO(tokenService.generateToken((UserModel) auth.getPrincipal()));
    }

    @Transactional
    public void register(RegisterRequestDTO body) {
        if(this.repository.findByEmail(body.email()) == null) {

            String encryptedPassword = new BCryptPasswordEncoder().encode(body.password());
            UserModel user = new UserModel(body.userName(), body.email(), encryptedPassword, body.role());

            repository.save(user);

        } else {
            throw new UserAlreadyExistsException();
        }
    }

    public UserModel extractUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return (UserModel) authentication.getPrincipal();
    }

    public AuthenticationService(AuthenticationManager authenticationManager, UserRepository repository, TokenService tokenService) {
        this.authenticationManager = authenticationManager;
        this.repository = repository;
        this.tokenService = tokenService;
    }
}
