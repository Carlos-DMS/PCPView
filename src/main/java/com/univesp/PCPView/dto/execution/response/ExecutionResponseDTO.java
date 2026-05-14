package com.univesp.PCPView.dto.execution.response;

import com.univesp.PCPView.models.enums.ExecutionStatus;

import java.time.LocalDateTime;
import java.util.UUID;

public record ExecutionResponseDTO(UUID id,
                                   String maquinaNome,
                                   String subOrdemId,
                                   String operadorNome,
                                   ExecutionStatus status,
                                   LocalDateTime dataInicio,
                                   LocalDateTime dataFim) {
}
