package com.univesp.PCPView.services;

import com.univesp.PCPView.dto.subOrder.response.SubOrderResponseDTO;
import com.univesp.PCPView.exceptions.NonExistentOrderException;
import com.univesp.PCPView.exceptions.NonExistentSubOrderException;
import com.univesp.PCPView.exceptions.OrderAlreadyStartedException;
import com.univesp.PCPView.models.OrderModel;
import com.univesp.PCPView.models.SubOrderModel;
import com.univesp.PCPView.models.enums.StatusProducaoEnum;
import com.univesp.PCPView.repository.OrderRepository;
import com.univesp.PCPView.repository.SubOrderRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class SubOrderService {

    private final SubOrderRepository subOrderRepository;
    private final OrderRepository orderRepository;

    public SubOrderService(SubOrderRepository subOrderRepository, OrderRepository orderRepository) {
        this.subOrderRepository = subOrderRepository;
        this.orderRepository = orderRepository;
    }

    @Transactional(readOnly = true)
    public List<SubOrderResponseDTO> listarSubOrdensPorOrdemPai(String numeroOrdem) {
        OrderModel ordem = orderRepository.findById(numeroOrdem)
                .orElseThrow(NonExistentOrderException::new);

        List<SubOrderModel> subOrdens = subOrderRepository.findAllByOrdemPrincipal(ordem);

        return subOrdens.stream()
                .map(s -> new SubOrderResponseDTO(
                        s.getId(),
                        s.getQuantidadeTotal(),
                        s.getQuantidadeProduzida()
                )).toList();
    }

    @Transactional
    public void deletarSubOrdem(String idSubOrdem) {
        SubOrderModel subOrdem = subOrderRepository.findById(idSubOrdem).orElseThrow(NonExistentSubOrderException::new);

        if (!subOrdem.getStatus().equals(StatusProducaoEnum.AGUARDANDO)) {
            throw new OrderAlreadyStartedException();
        }

        subOrderRepository.delete(subOrdem);
    }

    @Transactional
    public SubOrderResponseDTO criarNovaEtapa(String numeroOrdem, String letra) {
        OrderModel ordem = orderRepository.findById(numeroOrdem).orElseThrow(NonExistentOrderException::new);

        List<SubOrderModel> subOrdensExistentes = subOrderRepository.findAllByOrdemPrincipal(ordem);

        int maxEtapa = subOrdensExistentes.stream()
                .filter(s -> s.getId().contains("-" + letra.toUpperCase() + "-"))
                .mapToInt(s -> {
                    String[] partes = s.getId().split("-");
                    return Integer.parseInt(partes[partes.length - 1]);
                })
                .max()
                .orElse(0);

        String numeroEtapaFormatado = String.format("%02d", maxEtapa + 1);

        String novoCodigoDaEtapa = ordem.getNumeroOrdem() + "-" + letra.toUpperCase() + "-" + numeroEtapaFormatado;

        SubOrderModel novaSubOrdem = new SubOrderModel(novoCodigoDaEtapa, ordem, ordem.getQuantidadeTotal());

        subOrderRepository.save(novaSubOrdem);

        return new SubOrderResponseDTO(
                novaSubOrdem.getId(),
                novaSubOrdem.getQuantidadeTotal(),
                novaSubOrdem.getQuantidadeProduzida()
        );
    }

    @Transactional
    public SubOrderResponseDTO alterarStatusSubOrdem(String idSubOrdem, StatusProducaoEnum novoStatus) {
        SubOrderModel subOrdem = subOrderRepository.findById(idSubOrdem)
                .orElseThrow(NonExistentSubOrderException::new);

        subOrdem.setStatus(novoStatus);
        subOrderRepository.save(subOrdem);

        OrderModel ordemPrincipal = subOrdem.getOrdemPrincipal();
        List<SubOrderModel> todasAsSubOrdens = subOrderRepository.findAllByOrdemPrincipal(ordemPrincipal);

        if (novoStatus.equals(StatusProducaoEnum.FINALIZADO)) {
            boolean todasFinalizadas = todasAsSubOrdens.stream()
                    .allMatch(s -> s.getStatus().equals(StatusProducaoEnum.FINALIZADO));

            if (todasFinalizadas) {
                ordemPrincipal.setStatus(StatusProducaoEnum.FINALIZADO);

                orderRepository.save(ordemPrincipal);
            }
        }
        else if (ordemPrincipal.getStatus().equals(StatusProducaoEnum.FINALIZADO)) {
            ordemPrincipal.setStatus(StatusProducaoEnum.EM_PROCESSAMENTO);

            orderRepository.save(ordemPrincipal);
        }

        return new SubOrderResponseDTO(
                subOrdem.getId(),
                subOrdem.getQuantidadeTotal(),
                subOrdem.getQuantidadeProduzida()
        );
    }
}