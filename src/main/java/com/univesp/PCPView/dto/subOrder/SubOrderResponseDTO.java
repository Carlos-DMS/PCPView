package com.univesp.PCPView.dto.subOrder;

public record SubOrderResponseDTO(String codigoEtapa,
                                  Integer quantidadeTotal,
                                  Integer quantidadeProduzida,
                                  String maquinaIdealId,
                                  String maquinaIdealNome,
                                  Integer posicaoFila){
}
