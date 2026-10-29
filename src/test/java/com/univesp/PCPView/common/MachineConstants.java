package com.univesp.PCPView.common;

import com.univesp.PCPView.dto.machine.request.MachineRequestDTO;
import com.univesp.PCPView.dto.machine.response.MachineResponseDTO;
import com.univesp.PCPView.models.MachineModel;

public class MachineConstants {

    public static final MachineModel MAQUINA_VALIDA = new MachineModel("MAQ-01", "TORNO CNC");
    public static final MachineRequestDTO MAQUINA_REQUEST_VALIDO = new MachineRequestDTO("MAQ-01", "TORNO CNC");
    public static final MachineRequestDTO MAQUINA_REQUEST_INVALIDO = new MachineRequestDTO("", "");
    public static final MachineResponseDTO MAQUINA_RESPONSE_VALIDA = new MachineResponseDTO("MAQ-01", "TORNO CNC", true);

    public static final String MAQUINA_ID_INEXISTENTE = "MAQ-INEXISTENTE";
    public static final String NOVO_NOME_MAQUINA = "TORNO CNC ATUALIZADO";
    public static final MachineResponseDTO MAQUINA_RESPONSE_NOME_ATUALIZADO = new MachineResponseDTO("MAQ-01", "TORNO CNC ATUALIZADO", true);
}