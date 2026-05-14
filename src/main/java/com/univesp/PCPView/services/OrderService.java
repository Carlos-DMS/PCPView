package com.univesp.PCPView.services;

import com.univesp.PCPView.dto.order.OrderRequestDTO;
import com.univesp.PCPView.dto.order.OrderResponseDTO;
import com.univesp.PCPView.dto.subOrder.SubOrderResponseDTO;
import com.univesp.PCPView.dto.subOrder.SubsetRequestDTO;
import com.univesp.PCPView.models.OrderModel;
import com.univesp.PCPView.models.SubOrderModel;
import com.univesp.PCPView.repository.OrderRepository;
import com.univesp.PCPView.repository.SubOrderRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class OrderService {
    private final OrderRepository orderRepository;
    private final SubOrderRepository subOrderRepository;

    public OrderService(OrderRepository orderRepository, SubOrderRepository subOrderRepository) {
        this.orderRepository = orderRepository;
        this.subOrderRepository = subOrderRepository;
    }

    @Transactional
    public OrderResponseDTO criarOrdemComEtapas(OrderRequestDTO body) {
        OrderModel ordemPrincipal = orderRepository.save(new OrderModel(body.numeroOrdem(), body.quantidadeTotal()));

        List<SubOrderModel> subOrdens = getSubOrdens(body, ordemPrincipal);

        subOrderRepository.saveAll(subOrdens);

        return new OrderResponseDTO(ordemPrincipal.getNumeroOrdem(),
                ordemPrincipal.getQuantidadeTotal(),
                ordemPrincipal.getQuantidadeProduzida(),
                subOrdens.stream().map( s -> new SubOrderResponseDTO(
                        s.getId(),
                        s.getQuantidadeTotal(),
                        s.getQuantidadeProduzida())).toList()
        );
    }

    private List<SubOrderModel> getSubOrdens(OrderRequestDTO body, OrderModel ordemPrincipal) {
        List<SubOrderModel> subOrdens = new ArrayList<>();

        for (SubsetRequestDTO subconjunto : body.subconjuntos()) {
            for (int i = 1; i <= subconjunto.quantidadeEtapas(); i++) {
                String numeroEtapaFormatado = String.format("%02d", i);

                String codigoDaEtapa = ordemPrincipal.getNumeroOrdem() + "-" + subconjunto.letra() + "-" + numeroEtapaFormatado;

                SubOrderModel subOrdem = new SubOrderModel(codigoDaEtapa, ordemPrincipal, body.quantidadeTotal());

                subOrdens.add(subOrdem);
            }
        }
        return subOrdens;
    }
}
