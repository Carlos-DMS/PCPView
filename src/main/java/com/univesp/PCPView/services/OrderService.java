package com.univesp.PCPView.services;

import com.univesp.PCPView.dto.order.request.OrderRequestDTO;
import com.univesp.PCPView.dto.order.response.OrderResponseDTO;
import com.univesp.PCPView.dto.order.response.SimpleOrderResponseDTO;
import com.univesp.PCPView.dto.subOrder.response.SubOrderResponseDTO;
import com.univesp.PCPView.dto.subOrder.request.SubsetRequestDTO;
import com.univesp.PCPView.exceptions.NonExistentOrderException;
import com.univesp.PCPView.exceptions.OrderAlreadyStartedException;
import com.univesp.PCPView.models.OrderModel;
import com.univesp.PCPView.models.SubOrderModel;
import com.univesp.PCPView.models.enums.StatusProducaoEnum;
import com.univesp.PCPView.repository.OrderRepository;
import com.univesp.PCPView.repository.SubOrderRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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

        List<SubOrderModel> subOrdens = gerarSubOrdens(body, ordemPrincipal);

        subOrderRepository.saveAll(subOrdens);

        return converterOrdemParaDTO(ordemPrincipal, subOrdens);
    }

    @Transactional(readOnly = true)
    public List<OrderResponseDTO> buscarTodasAsOrdens(StatusProducaoEnum status) {
        List<OrderModel> ordens = new ArrayList<>();
        List<OrderResponseDTO> ordensDTO = new ArrayList<>();

        if (status == null) {
            ordens = orderRepository.findAllByOrderByPrioridadeAsc();
        }
        else {
            ordens = orderRepository.findByStatusOrderByPrioridadeAsc(status);
        }

        for (OrderModel ordem : ordens) {
            List<SubOrderModel> subOrdens = subOrderRepository.findAllByOrdemPrincipal(ordem);

            ordensDTO.add(converterOrdemParaDTO(ordem, subOrdens));
        }

        return ordensDTO;
    }

    @Transactional
    public OrderResponseDTO alterarStatusOrdem(StatusProducaoEnum status, String codigoOrdem) {
        OrderModel ordem = orderRepository.findById(codigoOrdem).orElseThrow(NonExistentOrderException::new);

        ordem.setStatus(status);

        orderRepository.save(ordem);

        List<SubOrderModel> subOrdens = subOrderRepository.findAllByOrdemPrincipal(ordem);

        if (status.equals(StatusProducaoEnum.CANCELADO) || status.equals(StatusProducaoEnum.FINALIZADO)) {
            subOrdens.forEach(s -> s.setStatus(status));

            subOrderRepository.saveAll(subOrdens);
        }

        return converterOrdemParaDTO(ordem, subOrdens);
    }

    @Transactional
    public SimpleOrderResponseDTO alterarQuantidadeOrdem(Integer quantidade, String codigoOrdem) {
        OrderModel ordem = orderRepository.findById(codigoOrdem).orElseThrow(NonExistentOrderException::new);

        ordem.setQuantidadeTotal(quantidade);

        List<SubOrderModel> subOrdens = subOrderRepository.findAllByOrdemPrincipal(ordem);

        orderRepository.save(ordem);

        subOrdens.forEach(s -> s.setQuantidadeTotal(quantidade));

        subOrderRepository.saveAll(subOrdens);

        return converterOrdemParaDTOSimples(ordem);
    }

    @Transactional
    public SimpleOrderResponseDTO alterarPrioridadeOrdem(Integer prioridade, String codigoOrdem) {
        OrderModel ordem = orderRepository.findById(codigoOrdem).orElseThrow(NonExistentOrderException::new);

        ordem.setPrioridade(prioridade);

        orderRepository.save(ordem);

        return converterOrdemParaDTOSimples(ordem);
    }

    @Transactional
    public void deletarOrdemESuasSubOrdens (String codigoOrdem) {
        OrderModel ordem = orderRepository.findById(codigoOrdem).orElseThrow(NonExistentOrderException::new);

        if (!ordem.getStatus().equals(StatusProducaoEnum.AGUARDANDO)) {
            throw new OrderAlreadyStartedException();
        }

        List<SubOrderModel> subOrdens = subOrderRepository.findAllByOrdemPrincipal(ordem);

        subOrderRepository.deleteAll(subOrdens);

        orderRepository.delete(ordem);
    }

    private List<SubOrderModel> gerarSubOrdens(OrderRequestDTO body, OrderModel ordemPrincipal) {
        List<SubOrderModel> subOrdens = new ArrayList<>();

        for (SubsetRequestDTO subconjunto : body.subconjuntos()) {
            for (int i = 1; i <= subconjunto.quantidadeEtapas(); i++) {
                String numeroEtapaFormatado = String.format("%02d", i);

                String codigoDaEtapa = ordemPrincipal.getNumeroOrdem() + "-" + subconjunto.letra().toUpperCase() + "-" + numeroEtapaFormatado;

                SubOrderModel subOrdem = new SubOrderModel(codigoDaEtapa, ordemPrincipal, body.quantidadeTotal());

                subOrdens.add(subOrdem);
            }
        }
        return subOrdens;
    }

    private OrderResponseDTO converterOrdemParaDTO (OrderModel ordem, List<SubOrderModel> subOrdens) {
        return new OrderResponseDTO(ordem.getNumeroOrdem(),
                ordem.getQuantidadeTotal(),
                ordem.getQuantidadeProduzida(),
                ordem.getPrioridade(),
                ordem.getDataCriacao(),
                subOrdens.stream().map( s -> new SubOrderResponseDTO(
                        s.getId(),
                        s.getQuantidadeTotal(),
                        s.getQuantidadeProduzida())).toList()
        );
    }

    private SimpleOrderResponseDTO converterOrdemParaDTOSimples (OrderModel ordem) {
        return new SimpleOrderResponseDTO(ordem.getNumeroOrdem(),
                ordem.getQuantidadeTotal(),
                ordem.getQuantidadeProduzida(),
                ordem.getPrioridade(),
                ordem.getDataCriacao()
        );
    }
}
