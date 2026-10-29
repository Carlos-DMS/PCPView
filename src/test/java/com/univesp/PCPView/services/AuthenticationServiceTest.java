package com.univesp.PCPView.services;

import com.univesp.PCPView.common.AuthenticationConstants;
import com.univesp.PCPView.dto.authentication.response.LoginResponseDTO;
import com.univesp.PCPView.dto.authentication.response.UserResponseDTO;
import com.univesp.PCPView.exceptions.InvalidCredentialsException;
import com.univesp.PCPView.exceptions.NonExistentUserException;
import com.univesp.PCPView.exceptions.UserAlreadyExistsException;
import com.univesp.PCPView.models.UserModel;
import com.univesp.PCPView.models.enums.RoleEnum;
import com.univesp.PCPView.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class AuthenticationServiceTest {

    @InjectMocks
    private AuthenticationService authenticationService;

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private UserRepository userRepository;

    @Mock
    private TokenService tokenService;

    @Test
    public void login_comCredenciaisValidas_retornaLoginResponseDTO() {
        // Arrange
        Authentication authentication = Mockito.mock(Authentication.class);
        when(authentication.getPrincipal()).thenReturn(AuthenticationConstants.USUARIO_COMUM);

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(authentication);
        when(tokenService.generateToken(AuthenticationConstants.USUARIO_COMUM))
                .thenReturn("token-jwt-valido");

        // Act
        LoginResponseDTO sut = authenticationService.login(AuthenticationConstants.LOGIN_REQUEST_VALIDO);

        // Assert
        assertThat(sut).isNotNull();
        assertThat(sut.token()).isEqualTo("token-jwt-valido");
    }

    @Test
    public void login_comCredenciaisInvalidas_lancaInvalidCredentialsException() {
        // Arrange
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenThrow(new BadCredentialsException("Bad credentials"));

        // Act e Assert
        assertThatThrownBy(() -> authenticationService.login(AuthenticationConstants.LOGIN_REQUEST_INVALIDO))
                .isInstanceOf(InvalidCredentialsException.class);
    }

    @Test
    public void registrar_comEmailNaoCadastrado_registraComSucesso() {
        // Arrange
        when(userRepository.findByEmail(AuthenticationConstants.REGISTER_REQUEST_VALIDO.email()))
                .thenReturn(null);

        // Act e Assert
        assertThatCode(() -> authenticationService.registrar(AuthenticationConstants.REGISTER_REQUEST_VALIDO))
                .doesNotThrowAnyException();

        verify(userRepository).save(any(UserModel.class));
    }

    @Test
    public void registrar_comEmailJaCadastrado_lancaUserAlreadyExistsException() {
        // Arrange
        when(userRepository.findByEmail(AuthenticationConstants.REGISTER_REQUEST_EXISTENTE.email()))
                .thenReturn(AuthenticationConstants.USUARIO_COMUM);

        // Act e Assert
        assertThatThrownBy(() -> authenticationService.registrar(AuthenticationConstants.REGISTER_REQUEST_EXISTENTE))
                .isInstanceOf(UserAlreadyExistsException.class);

        verify(userRepository, never()).save(any());
    }

    @Test
    public void listarUsuarios_quandoExistiremUsuarios_retornaListaDeUserResponseDTO() {
        // Arrange
        when(userRepository.findAll()).thenReturn(List.of(AuthenticationConstants.USUARIO_COMUM));

        // Act
        List<UserResponseDTO> sut = authenticationService.listarUsuarios();

        // Assert
        assertThat(sut).isNotEmpty();
        assertThat(sut).hasSize(1);
        assertThat(sut.getFirst()).usingRecursiveComparison().isEqualTo(AuthenticationConstants.USER_RESPONSE_COMUM);
    }

    @Test
    public void promoverParaAdmin_comIdExistente_retornaUserResponseDTOAdmin() {
        // Arrange
        UserModel userEmMemoria = AuthenticationConstants.criarUsuario(
                AuthenticationConstants.USER_ID_VALIDO,
                "Operador",
                "operador@teste.com",
                "senha123",
                RoleEnum.USER,
                true
        );

        when(userRepository.findById(userEmMemoria.getId())).thenReturn(Optional.of(userEmMemoria));
        when(userRepository.save(any(UserModel.class))).thenReturn(userEmMemoria);

        // Act
        UserResponseDTO sut = authenticationService.promoverParaAdmin(userEmMemoria.getId());

        // Assert
        assertThat(sut.role()).isEqualTo(RoleEnum.ADMIN);
        assertThat(userEmMemoria.getRole()).isEqualTo(RoleEnum.ADMIN);
        verify(userRepository).save(userEmMemoria);
    }

    @Test
    public void promoverParaAdmin_comIdInexistente_lancaNonExistentUserException() {
        // Arrange
        when(userRepository.findById(AuthenticationConstants.USER_ID_INEXISTENTE))
                .thenReturn(Optional.empty());

        // Act e Assert
        assertThatThrownBy(() -> authenticationService.promoverParaAdmin(AuthenticationConstants.USER_ID_INEXISTENTE))
                .isInstanceOf(NonExistentUserException.class);
    }

    @Test
    public void desativarUsuario_comIdExistenteEUsuarioAtivo_desativaComSucesso() {
        // Arrange
        UserModel userAtivo = AuthenticationConstants.criarUsuario(
                AuthenticationConstants.USER_ID_VALIDO,
                "Operador",
                "operador@teste.com",
                "senha123",
                RoleEnum.USER,
                true
        );

        when(userRepository.findById(userAtivo.getId())).thenReturn(Optional.of(userAtivo));

        // Act e Assert
        assertThatCode(() -> authenticationService.desativarUsuario(userAtivo.getId()))
                .doesNotThrowAnyException();

        assertThat(userAtivo.getAtivo()).isFalse();
        verify(userRepository).save(userAtivo);
    }

    @Test
    public void desativarUsuario_comUsuarioJaDesativado_lancaRuntimeException() {
        // Arrange
        UserModel userDesativado = AuthenticationConstants.criarUsuario(
                AuthenticationConstants.USER_ID_VALIDO,
                "Operador",
                "operador@teste.com",
                "senha123",
                RoleEnum.USER,
                false
        );

        when(userRepository.findById(userDesativado.getId())).thenReturn(Optional.of(userDesativado));

        // Act e Assert
        assertThatThrownBy(() -> authenticationService.desativarUsuario(userDesativado.getId()))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Este usuário já está desativado.");

        verify(userRepository, never()).save(any());
    }

    @Test
    public void desativarUsuario_comIdInexistente_lancaNonExistentUserException() {
        // Arrange
        when(userRepository.findById(AuthenticationConstants.USER_ID_INEXISTENTE))
                .thenReturn(Optional.empty());

        // Act e Assert
        assertThatThrownBy(() -> authenticationService.desativarUsuario(AuthenticationConstants.USER_ID_INEXISTENTE))
                .isInstanceOf(NonExistentUserException.class);
    }

    @Test
    public void extractUser_comContextoAutenticado_retornaUserModel() {
        // Arrange
        SecurityContext securityContext = Mockito.mock(SecurityContext.class);
        Authentication authentication = Mockito.mock(Authentication.class);

        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getPrincipal()).thenReturn(AuthenticationConstants.USUARIO_COMUM);
        SecurityContextHolder.setContext(securityContext);

        // Act
        UserModel sut = authenticationService.extractUser();

        // Assert
        assertThat(sut).usingRecursiveComparison().isEqualTo(AuthenticationConstants.USUARIO_COMUM);
    }
}