package com.univesp.PCPView.common;

import com.univesp.PCPView.dto.subOrder.response.SubOrderResponseDTO;
import com.univesp.PCPView.models.OrderModel;
import com.univesp.PCPView.models.SubOrderModel;
import com.univesp.PCPView.models.enums.StatusProducaoEnum;

public class SubOrderConstants {

    public static final String NUMERO_ORDEM_VALIDA = "ORD-001";
    public static final String NUMERO_ORDEM_INEXISTENTE = "ORD-INEXISTENTE";
    public static final String SUB_ORDEM_ID_VALIDO = "ORD-001-A-01";
    public static final String SUB_ORDEM_ID_INEXISTENTE = "SUB-INEXISTENTE";
    public static final String LETRA_ETAPA = "A";

    public static final OrderModel ORDEM_PADRAO = new OrderModel(NUMERO_ORDEM_VALIDA, 100);

    public static final SubOrderModel SUB_ORDEM_AGUARDANDO = new SubOrderModel(
            SUB_ORDEM_ID_VALIDO,
            ORDEM_PADRAO,
            100
    );

    public static final SubOrderResponseDTO SUB_ORDEM_RESPONSE_AGUARDANDO = new SubOrderResponseDTO(
            SUB_ORDEM_ID_VALIDO,
            100,
            0,
            StatusProducaoEnum.AGUARDANDO
    );

    public static final SubOrderResponseDTO SUB_ORDEM_RESPONSE_CRIADA = new SubOrderResponseDTO(
            "ORD-001-A-01",
            100,
            0,
            StatusProducaoEnum.AGUARDANDO
    );
}