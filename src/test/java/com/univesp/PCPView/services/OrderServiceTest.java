package com.univesp.PCPView.services;

import com.univesp.PCPView.common.OrderConstants;
import com.univesp.PCPView.dto.order.response.OrderResponseDTO;
import com.univesp.PCPView.dto.order.response.SimpleOrderResponseDTO;
import com.univesp.PCPView.exceptions.NonExistentOrderException;
import com.univesp.PCPView.exceptions.OrderAlreadyStartedException;
import com.univesp.PCPView.models.OrderModel;
import com.univesp.PCPView.models.SubOrderModel;
import com.univesp.PCPView.models.enums.StatusProducaoEnum;
import com.univesp.PCPView.repository.OrderRepository;
import com.univesp.PCPView.repository.SubOrderRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class OrderServiceTest {

    @InjectMocks
    private OrderService orderService;

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private SubOrderRepository subOrderRepository;

    @Test
    public void criarOrdemComEtapas_comDadosValidos_retornaOrderResponseDTO() {
        // Arrange
        when(orderRepository.save(any(OrderModel.class))).thenReturn(OrderConstants.ORDEM_PADRAO);

        // Act
        OrderResponseDTO sut = orderService.criarOrdemComEtapas(OrderConstants.ORDEM_REQUEST_VALIDA);

        // Assert
        assertThat(sut).usingRecursiveComparison().isEqualTo(OrderConstants.ORDEM_RESPONSE_VALIDA);
        verify(subOrderRepository).saveAll(anyList());
    }

    @Test
    public void buscarTodasAsOrdens_semStatus_retornaTodasOrdenadasPorPrioridade() {
        // Arrange
        when(orderRepository.findAllByOrderByPrioridadeAsc())
                .thenReturn(List.of(OrderConstants.ORDEM_PADRAO));
        when(subOrderRepository.findAllByOrdemPrincipal(OrderConstants.ORDEM_PADRAO))
                .thenReturn(List.of(OrderConstants.SUB_ORDEM_PADRAO));

        // Act
        List<OrderResponseDTO> sut = orderService.buscarTodasAsOrdens(null);

        // Assert
        assertThat(sut).isNotEmpty();
        assertThat(sut).hasSize(1);
        assertThat(sut.getFirst()).usingRecursiveComparison().isEqualTo(OrderConstants.ORDEM_RESPONSE_VALIDA);
    }

    @Test
    public void buscarTodasAsOrdens_comStatus_retornaFiltradasPorStatus() {
        // Arrange
        when(orderRepository.findByStatusOrderByPrioridadeAsc(StatusProducaoEnum.AGUARDANDO))
                .thenReturn(List.of(OrderConstants.ORDEM_PADRAO));
        when(subOrderRepository.findAllByOrdemPrincipal(OrderConstants.ORDEM_PADRAO))
                .thenReturn(List.of(OrderConstants.SUB_ORDEM_PADRAO));

        // Act
        List<OrderResponseDTO> sut = orderService.buscarTodasAsOrdens(StatusProducaoEnum.AGUARDANDO);

        // Assert
        assertThat(sut).isNotEmpty();
        assertThat(sut.getFirst().status()).isEqualTo(StatusProducaoEnum.AGUARDANDO);
    }

    @Test
    public void alterarStatusOrdem_comIdExistenteParaEmProcessamento_atualizaApenasOrdemPrincipal() {
        // Arrange
        OrderModel ordem = new OrderModel(OrderConstants.NUMERO_ORDEM_VALIDA, 100);
        SubOrderModel subOrdem = new SubOrderModel("ORD-001-A-01", ordem, 100);

        when(orderRepository.findById(OrderConstants.NUMERO_ORDEM_VALIDA)).thenReturn(Optional.of(ordem));
        when(subOrderRepository.findAllByOrdemPrincipal(ordem)).thenReturn(List.of(subOrdem));

        // Act
        OrderResponseDTO sut = orderService.alterarStatusOrdem(StatusProducaoEnum.EM_PROCESSAMENTO, OrderConstants.NUMERO_ORDEM_VALIDA);

        // Assert
        assertThat(sut.status()).isEqualTo(StatusProducaoEnum.EM_PROCESSAMENTO);
        verify(orderRepository).save(ordem);
        verify(subOrderRepository, never()).saveAll(any());
    }

    @Test
    public void alterarStatusOrdem_comIdExistenteParaCancelado_atualizaOrdemESubOrdens() {
        // Arrange
        OrderModel ordem = new OrderModel(OrderConstants.NUMERO_ORDEM_VALIDA, 100);
        SubOrderModel subOrdem = new SubOrderModel("ORD-001-A-01", ordem, 100);

        when(orderRepository.findById(OrderConstants.NUMERO_ORDEM_VALIDA)).thenReturn(Optional.of(ordem));
        when(subOrderRepository.findAllByOrdemPrincipal(ordem)).thenReturn(List.of(subOrdem));

        // Act
        OrderResponseDTO sut = orderService.alterarStatusOrdem(StatusProducaoEnum.CANCELADO, OrderConstants.NUMERO_ORDEM_VALIDA);

        // Assert
        assertThat(sut.status()).isEqualTo(StatusProducaoEnum.CANCELADO);
        assertThat(subOrdem.getStatus()).isEqualTo(StatusProducaoEnum.CANCELADO);
        verify(orderRepository).save(ordem);
        verify(subOrderRepository).saveAll(List.of(subOrdem));
    }

    @Test
    public void alterarStatusOrdem_comIdInexistente_lancaNonExistentOrderException() {
        // Arrange
        when(orderRepository.findById(OrderConstants.NUMERO_ORDEM_INEXISTENTE))
                .thenReturn(Optional.empty());

        // Act e Assert
        assertThatThrownBy(() -> orderService.alterarStatusOrdem(StatusProducaoEnum.CANCELADO, OrderConstants.NUMERO_ORDEM_INEXISTENTE))
                .isInstanceOf(NonExistentOrderException.class);
    }

    @Test
    public void alterarQuantidadeOrdem_comIdExistente_atualizaQuantidadeDaOrdemESubOrdens() {
        // Arrange
        OrderModel ordem = new OrderModel(OrderConstants.NUMERO_ORDEM_VALIDA, 100);
        SubOrderModel subOrdem = new SubOrderModel("ORD-001-A-01", ordem, 100);

        when(orderRepository.findById(OrderConstants.NUMERO_ORDEM_VALIDA)).thenReturn(Optional.of(ordem));
        when(subOrderRepository.findAllByOrdemPrincipal(ordem)).thenReturn(List.of(subOrdem));

        // Act
        SimpleOrderResponseDTO sut = orderService.alterarQuantidadeOrdem(200, OrderConstants.NUMERO_ORDEM_VALIDA);

        // Assert
        assertThat(sut.quantidadeTotal()).isEqualTo(200);
        assertThat(ordem.getQuantidadeTotal()).isEqualTo(200);
        assertThat(subOrdem.getQuantidadeTotal()).isEqualTo(200);
        verify(orderRepository).save(ordem);
        verify(subOrderRepository).saveAll(List.of(subOrdem));
    }

    @Test
    public void alterarQuantidadeOrdem_comIdInexistente_lancaNonExistentOrderException() {
        // Arrange
        when(orderRepository.findById(OrderConstants.NUMERO_ORDEM_INEXISTENTE))
                .thenReturn(Optional.empty());

        // Act e Assert
        assertThatThrownBy(() -> orderService.alterarQuantidadeOrdem(200, OrderConstants.NUMERO_ORDEM_INEXISTENTE))
                .isInstanceOf(NonExistentOrderException.class);
    }

    @Test
    public void alterarPrioridadeOrdem_comIdExistente_atualizaPrioridade() {
        // Arrange
        OrderModel ordem = new OrderModel(OrderConstants.NUMERO_ORDEM_VALIDA, 100);

        when(orderRepository.findById(OrderConstants.NUMERO_ORDEM_VALIDA)).thenReturn(Optional.of(ordem));

        // Act
        SimpleOrderResponseDTO sut = orderService.alterarPrioridadeOrdem(1, OrderConstants.NUMERO_ORDEM_VALIDA);

        // Assert
        assertThat(sut.prioridade()).isEqualTo(1);
        assertThat(ordem.getPrioridade()).isEqualTo(1);
        verify(orderRepository).save(ordem);
    }

    @Test
    public void alterarPrioridadeOrdem_comIdInexistente_lancaNonExistentOrderException() {
        // Arrange
        when(orderRepository.findById(OrderConstants.NUMERO_ORDEM_INEXISTENTE))
                .thenReturn(Optional.empty());

        // Act e Assert
        assertThatThrownBy(() -> orderService.alterarPrioridadeOrdem(1, OrderConstants.NUMERO_ORDEM_INEXISTENTE))
                .isInstanceOf(NonExistentOrderException.class);
    }

    @Test
    public void deletarOrdemESuasSubOrdens_comStatusAguardando_deletaOrdemESubOrdens() {
        // Arrange
        OrderModel ordem = new OrderModel(OrderConstants.NUMERO_ORDEM_VALIDA, 100);
        SubOrderModel subOrdem = new SubOrderModel("ORD-001-A-01", ordem, 100);

        when(orderRepository.findById(OrderConstants.NUMERO_ORDEM_VALIDA)).thenReturn(Optional.of(ordem));
        when(subOrderRepository.findAllByOrdemPrincipal(ordem)).thenReturn(List.of(subOrdem));

        // Act e Assert
        assertThatCode(() -> orderService.deletarOrdemESuasSubOrdens(OrderConstants.NUMERO_ORDEM_VALIDA))
                .doesNotThrowAnyException();

        verify(subOrderRepository).deleteAll(List.of(subOrdem));
        verify(orderRepository).delete(ordem);
    }

    @Test
    public void deletarOrdemESuasSubOrdens_comStatusDiferenteDeAguardando_lancaOrderAlreadyStartedException() {
        // Arrange
        OrderModel ordem = new OrderModel(OrderConstants.NUMERO_ORDEM_VALIDA, 100);
        ordem.setStatus(StatusProducaoEnum.EM_PROCESSAMENTO);

        when(orderRepository.findById(OrderConstants.NUMERO_ORDEM_VALIDA)).thenReturn(Optional.of(ordem));

        // Act e Assert
        assertThatThrownBy(() -> orderService.deletarOrdemESuasSubOrdens(OrderConstants.NUMERO_ORDEM_VALIDA))
                .isInstanceOf(OrderAlreadyStartedException.class);

        verify(subOrderRepository, never()).deleteAll(any());
        verify(orderRepository, never()).delete(any());
    }

    @Test
    public void deletarOrdemESuasSubOrdens_comIdInexistente_lancaNonExistentOrderException() {
        // Arrange
        when(orderRepository.findById(OrderConstants.NUMERO_ORDEM_INEXISTENTE))
                .thenReturn(Optional.empty());

        // Act e Assert
        assertThatThrownBy(() -> orderService.deletarOrdemESuasSubOrdens(OrderConstants.NUMERO_ORDEM_INEXISTENTE))
                .isInstanceOf(NonExistentOrderException.class);
    }
}