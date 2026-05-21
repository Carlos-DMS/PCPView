package com.univesp.PCPView.dto.order.response;

import java.time.LocalDateTime;

public record SimpleOrderResponseDTO(String numeroOrdem,
                                     Integer quantidadeTotal,
                                     Integer quantidadeProduzida,
                                     Integer prioridade,
                                     LocalDateTime dataCriacao) {
}
