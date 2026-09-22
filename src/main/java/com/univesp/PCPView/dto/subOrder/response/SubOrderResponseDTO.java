package com.univesp.PCPView.dto.subOrder.response;

import com.univesp.PCPView.models.enums.StatusProducaoEnum;

public record SubOrderResponseDTO(String codigoEtapa,
                                  Integer quantidadeTotal,
                                  Integer quantidadeProduzida,
                                  StatusProducaoEnum status){
}
