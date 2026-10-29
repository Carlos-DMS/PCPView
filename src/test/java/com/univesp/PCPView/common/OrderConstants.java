package com.univesp.PCPView.common;

import com.univesp.PCPView.dto.order.request.OrderRequestDTO;
import com.univesp.PCPView.dto.order.response.OrderResponseDTO;
import com.univesp.PCPView.dto.order.response.SimpleOrderResponseDTO;
import com.univesp.PCPView.dto.subOrder.request.SubsetRequestDTO;
import com.univesp.PCPView.dto.subOrder.response.SubOrderResponseDTO;
import com.univesp.PCPView.models.OrderModel;
import com.univesp.PCPView.models.SubOrderModel;
import com.univesp.PCPView.models.enums.StatusProducaoEnum;

import java.util.List;

public class OrderConstants {

    public static final String NUMERO_ORDEM_VALIDA = "ORD-001";
    public static final String NUMERO_ORDEM_INEXISTENTE = "ORD-INEXISTENTE";

    public static final OrderRequestDTO ORDEM_REQUEST_VALIDA = new OrderRequestDTO(
            NUMERO_ORDEM_VALIDA,
            100,
            List.of(new SubsetRequestDTO("A", 1))
    );

    public static final OrderModel ORDEM_PADRAO = new OrderModel(NUMERO_ORDEM_VALIDA, 100);

    public static final SubOrderModel SUB_ORDEM_PADRAO = new SubOrderModel(
            "ORD-001-A-01",
            ORDEM_PADRAO,
            100
    );

    public static final OrderResponseDTO ORDEM_RESPONSE_VALIDA = new OrderResponseDTO(
            NUMERO_ORDEM_VALIDA,
            100,
            0,
            5,
            StatusProducaoEnum.AGUARDANDO,
            ORDEM_PADRAO.getDataCriacao(),
            List.of(new SubOrderResponseDTO("ORD-001-A-01", 100, 0, StatusProducaoEnum.AGUARDANDO))
    );

    public static final SimpleOrderResponseDTO ORDEM_SIMPLES_RESPONSE_VALIDA = new SimpleOrderResponseDTO(
            NUMERO_ORDEM_VALIDA,
            100,
            0,
            5,
            ORDEM_PADRAO.getDataCriacao()
    );
}