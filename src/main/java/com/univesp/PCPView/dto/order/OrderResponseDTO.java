package com.univesp.PCPView.dto.order;

import com.univesp.PCPView.dto.subOrder.SubOrderResponseDTO;

import java.util.List;

public record OrderResponseDTO(String numeroOrdem,
                               Integer quantidadeTotal,
                               Integer quantidadeProduzida,
                               List<SubOrderResponseDTO> subOrdens) {
}
