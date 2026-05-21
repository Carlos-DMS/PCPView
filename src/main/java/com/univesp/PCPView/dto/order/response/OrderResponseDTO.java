package com.univesp.PCPView.dto.order.response;

import com.univesp.PCPView.dto.subOrder.response.SubOrderResponseDTO;

import java.time.LocalDateTime;
import java.util.List;

public record OrderResponseDTO(String numeroOrdem,
                               Integer quantidadeTotal,
                               Integer quantidadeProduzida,
                               Integer prioridade,
                               LocalDateTime dataCriacao,
                               List<SubOrderResponseDTO> subOrdens) {
}
