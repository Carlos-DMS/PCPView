package com.univesp.PCPView.dto.order;

import com.univesp.PCPView.dto.subOrder.SubOrderResponseDTO;
import com.univesp.PCPView.models.enums.StatusProducaoEnum;

import java.time.LocalDateTime;
import java.util.List;

public record OrderResponseDTO(String numeroOrdem,
                               String produtoNome,
                               String produtoSku,
                               Integer quantidadeTotal,
                               Integer quantidadeProduzida,
                               StatusProducaoEnum status,
                               List<SubOrderResponseDTO> subOrdens,
                               LocalDateTime dataInicio,
                               LocalDateTime dataFim,
                               Long tempoProdutivoSegundos,
                               Long tempoSetupSegundos,
                               Long tempoRestanteLoteSegundos,
                               Long tempoMedioPorPecaSegundos,
                               Long tempoMedioRestanteSegundos) {
}
