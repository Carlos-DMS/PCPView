package com.univesp.PCPView.common;

import com.univesp.PCPView.dto.execution.request.ExecutionStartRequestDTO;
import com.univesp.PCPView.models.ExecutionModel;
import com.univesp.PCPView.models.MachineModel;
import com.univesp.PCPView.models.SubOrderModel;
import com.univesp.PCPView.models.UserModel;
import com.univesp.PCPView.models.enums.ExecutionStatus;
import com.univesp.PCPView.models.enums.RoleEnum;

import java.lang.reflect.Field;
import java.util.UUID;

public class ExecutionConstants {

    public static final UUID EXECUCAO_ID_VALIDO = UUID.fromString("22222222-2222-2222-2222-222222222222");
    public static final UUID EXECUCAO_ID_INEXISTENTE = UUID.fromString("00000000-0000-0000-0000-000000000000");

    public static final UUID OPERADOR_ID = UUID.fromString("11111111-1111-1111-1111-111111111111");
    public static final UUID OUTRO_OPERADOR_ID = UUID.fromString("33333333-3333-3333-3333-333333333333");
    public static final UUID ADMIN_ID = UUID.fromString("44444444-4444-4444-4444-444444444444");

    public static final UserModel OPERADOR = criarUsuario(OPERADOR_ID, "Operador", "operador@teste.com", RoleEnum.USER);
    public static final UserModel OUTRO_OPERADOR = criarUsuario(OUTRO_OPERADOR_ID, "Outro", "outro@teste.com", RoleEnum.USER);
    public static final UserModel ADMIN = criarUsuario(ADMIN_ID, "Admin", "admin@teste.com", RoleEnum.ADMIN);

    public static final MachineModel MAQUINA = new MachineModel("MAQ-01", "TORNO CNC");

    public static final ExecutionStartRequestDTO EXECUCAO_START_REQUEST = new ExecutionStartRequestDTO("MAQ-01", "ORD-001-A-01");

    public static ExecutionModel criarExecucao(UUID id, UserModel operador, MachineModel maquina, SubOrderModel subOrdem, ExecutionStatus status) {
        ExecutionModel execucao = new ExecutionModel(operador, maquina, subOrdem);
        execucao.setId(id);
        execucao.setStatus(status);
        return execucao;
    }

    public static UserModel criarUsuario(UUID id, String nome, String email, RoleEnum role) {
        UserModel user = new UserModel(nome, email, "senha123");
        user.setRole(role);
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