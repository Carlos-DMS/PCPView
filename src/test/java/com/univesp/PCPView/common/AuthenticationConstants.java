package com.univesp.PCPView.common;

import com.univesp.PCPView.dto.authentication.request.LoginRequestDTO;
import com.univesp.PCPView.dto.authentication.request.RegisterRequestDTO;
import com.univesp.PCPView.dto.authentication.response.UserResponseDTO;
import com.univesp.PCPView.models.UserModel;
import com.univesp.PCPView.models.enums.RoleEnum;

import java.lang.reflect.Field;
import java.util.UUID;

public class AuthenticationConstants {

    public static final UUID USER_ID_VALIDO = UUID.fromString("11111111-1111-1111-1111-111111111111");
    public static final UUID USER_ID_INEXISTENTE = UUID.fromString("00000000-0000-0000-0000-000000000000");

    public static final LoginRequestDTO LOGIN_REQUEST_VALIDO = new LoginRequestDTO("operador@teste.com", "senha123");
    public static final LoginRequestDTO LOGIN_REQUEST_INVALIDO = new LoginRequestDTO("operador@teste.com", "senhaIncorreta");

    public static final RegisterRequestDTO REGISTER_REQUEST_VALIDO = new RegisterRequestDTO("Operador", "novo@teste.com", "senha123");
    public static final RegisterRequestDTO REGISTER_REQUEST_EXISTENTE = new RegisterRequestDTO("Operador", "operador@teste.com", "senha123");

    public static final UserModel USUARIO_COMUM = criarUsuario(
            USER_ID_VALIDO,
            "Operador",
            "operador@teste.com",
            "senha123",
            RoleEnum.USER,
            true
    );

    public static final UserResponseDTO USER_RESPONSE_COMUM = new UserResponseDTO(
            USER_ID_VALIDO,
            "operador@teste.com",
            RoleEnum.USER,
            true
    );

    public static UserModel criarUsuario(UUID id, String nome, String email, String senha, RoleEnum role, boolean ativo) {
        UserModel user = new UserModel(nome, email, senha);
        user.setRole(role);
        user.setAtivo(ativo);
        try {
            Field field = UserModel.class.getDeclaredField("id");
            field.setAccessible(true);
            field.set(user, id);
        } catch (Exception e) {
            throw new RuntimeException("Erro ao atribuir ID por reflexao nos testes", e);
        }
        return user;
    }
}