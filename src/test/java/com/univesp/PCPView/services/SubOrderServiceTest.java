package com.univesp.PCPView.services;

import com.univesp.PCPView.common.SubOrderConstants;
import com.univesp.PCPView.dto.subOrder.response.SubOrderResponseDTO;
import com.univesp.PCPView.exceptions.NonExistentOrderException;
import com.univesp.PCPView.exceptions.NonExistentSubOrderException;
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

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class SubOrderServiceTest {

    @InjectMocks
    private SubOrderService subOrderService;

    @Mock
    private SubOrderRepository subOrderRepository;

    @Mock
    private OrderRepository orderRepository;

    @Test
    public void listarSubOrdensPorOrdemPai_comOrdemExistente_retornaListaDeSubOrderResponseDTO() {
        // Arrange
        when(orderRepository.findById(SubOrderConstants.NUMERO_ORDEM_VALIDA))
                .thenReturn(Optional.of(SubOrderConstants.ORDEM_PADRAO));
        when(subOrderRepository.findAllByOrdemPrincipal(SubOrderConstants.ORDEM_PADRAO))
                .thenReturn(List.of(SubOrderConstants.SUB_ORDEM_AGUARDANDO));

        // Act
        List<SubOrderResponseDTO> sut = subOrderService.listarSubOrdensPorOrdemPai(SubOrderConstants.NUMERO_ORDEM_VALIDA);

        // Assert
        assertThat(sut).isNotEmpty();
        assertThat(sut).hasSize(1);
        assertThat(sut.getFirst()).usingRecursiveComparison().isEqualTo(SubOrderConstants.SUB_ORDEM_RESPONSE_AGUARDANDO);
    }

    @Test
    public void listarSubOrdensPorOrdemPai_comOrdemInexistente_lancaNonExistentOrderException() {
        // Arrange
        when(orderRepository.findById(SubOrderConstants.NUMERO_ORDEM_INEXISTENTE))
                .thenReturn(Optional.empty());

        // Act e Assert
        assertThatThrownBy(() -> subOrderService.listarSubOrdensPorOrdemPai(SubOrderConstants.NUMERO_ORDEM_INEXISTENTE))
                .isInstanceOf(NonExistentOrderException.class);
    }

    @Test
    public void deletarSubOrdem_comStatusAguardando_deletaComSucesso() {
        // Arrange
        SubOrderModel subOrdemAguardando = new SubOrderModel(
                SubOrderConstants.SUB_ORDEM_ID_VALIDO,
                SubOrderConstants.ORDEM_PADRAO,
                100
        );
        when(subOrderRepository.findById(subOrdemAguardando.getId()))
                .thenReturn(Optional.of(subOrdemAguardando));

        // Act e Assert
        assertThatCode(() -> subOrderService.deletarSubOrdem(subOrdemAguardando.getId()))
                .doesNotThrowAnyException();

        verify(subOrderRepository).delete(subOrdemAguardando);
    }

    @Test
    public void deletarSubOrdem_comStatusDiferenteDeAguardando_lancaOrderAlreadyStartedException() {
        // Arrange
        SubOrderModel subOrdemIniciada = new SubOrderModel(
                SubOrderConstants.SUB_ORDEM_ID_VALIDO,
                SubOrderConstants.ORDEM_PADRAO,
                100
        );
        subOrdemIniciada.setStatus(StatusProducaoEnum.EM_PROCESSAMENTO);

        when(subOrderRepository.findById(subOrdemIniciada.getId()))
                .thenReturn(Optional.of(subOrdemIniciada));

        // Act e Assert
        assertThatThrownBy(() -> subOrderService.deletarSubOrdem(subOrdemIniciada.getId()))
                .isInstanceOf(OrderAlreadyStartedException.class);

        verify(subOrderRepository, never()).delete(any());
    }

    @Test
    public void deletarSubOrdem_comIdInexistente_lancaNonExistentSubOrderException() {
        // Arrange
        when(subOrderRepository.findById(SubOrderConstants.SUB_ORDEM_ID_INEXISTENTE))
                .thenReturn(Optional.empty());

        // Act e Assert
        assertThatThrownBy(() -> subOrderService.deletarSubOrdem(SubOrderConstants.SUB_ORDEM_ID_INEXISTENTE))
                .isInstanceOf(NonExistentSubOrderException.class);

        verify(subOrderRepository, never()).delete(any());
    }

    @Test
    public void criarNovaEtapa_comOrdemExistenteESemEtapasAnteriores_criaPrimeiraEtapaComSucesso() {
        // Arrange
        when(orderRepository.findById(SubOrderConstants.NUMERO_ORDEM_VALIDA))
                .thenReturn(Optional.of(SubOrderConstants.ORDEM_PADRAO));
        when(subOrderRepository.findAllByOrdemPrincipal(SubOrderConstants.ORDEM_PADRAO))
                .thenReturn(Collections.emptyList());

        // Act
        SubOrderResponseDTO sut = subOrderService.criarNovaEtapa(SubOrderConstants.NUMERO_ORDEM_VALIDA, SubOrderConstants.LETRA_ETAPA);

        // Assert
        assertThat(sut).usingRecursiveComparison().isEqualTo(SubOrderConstants.SUB_ORDEM_RESPONSE_CRIADA);
        assertThat(SubOrderConstants.ORDEM_PADRAO.getQuantidadeProduzida()).isEqualTo(0);
        verify(subOrderRepository).save(any(SubOrderModel.class));
        verify(orderRepository).save(SubOrderConstants.ORDEM_PADRAO);
    }

    @Test
    public void criarNovaEtapa_comOrdemInexistente_lancaNonExistentOrderException() {
        // Arrange
        when(orderRepository.findById(SubOrderConstants.NUMERO_ORDEM_INEXISTENTE))
                .thenReturn(Optional.empty());

        // Act e Assert
        assertThatThrownBy(() -> subOrderService.criarNovaEtapa(SubOrderConstants.NUMERO_ORDEM_INEXISTENTE, SubOrderConstants.LETRA_ETAPA))
                .isInstanceOf(NonExistentOrderException.class);

        verify(subOrderRepository, never()).save(any());
    }

    @Test
    public void alterarStatusSubOrdem_paraStatusEmProcessamento_atualizaApenasSubOrdem() {
        // Arrange
        SubOrderModel subOrdem = new SubOrderModel(
                SubOrderConstants.SUB_ORDEM_ID_VALIDO,
                SubOrderConstants.ORDEM_PADRAO,
                100
        );

        when(subOrderRepository.findById(subOrdem.getId())).thenReturn(Optional.of(subOrdem));
        when(subOrderRepository.findAllByOrdemPrincipal(SubOrderConstants.ORDEM_PADRAO))
                .thenReturn(List.of(subOrdem));

        // Act
        SubOrderResponseDTO sut = subOrderService.alterarStatusSubOrdem(subOrdem.getId(), StatusProducaoEnum.EM_PROCESSAMENTO);

        // Assert
        assertThat(sut.status()).isEqualTo(StatusProducaoEnum.EM_PROCESSAMENTO);
        verify(subOrderRepository).save(subOrdem);
        verify(orderRepository, never()).save(any());
    }

    @Test
    public void alterarStatusSubOrdem_paraFinalizadoQuandoTodasFicamFinalizadas_atualizaOrdemPrincipalParaFinalizado() {
        // Arrange
        OrderModel ordemPrincipal = new OrderModel(SubOrderConstants.NUMERO_ORDEM_VALIDA, 100);
        SubOrderModel subOrdem = new SubOrderModel(
                SubOrderConstants.SUB_ORDEM_ID_VALIDO,
                ordemPrincipal,
                100
        );

        when(subOrderRepository.findById(subOrdem.getId())).thenReturn(Optional.of(subOrdem));
        when(subOrderRepository.findAllByOrdemPrincipal(ordemPrincipal))
                .thenReturn(List.of(subOrdem));

        // Act
        SubOrderResponseDTO sut = subOrderService.alterarStatusSubOrdem(subOrdem.getId(), StatusProducaoEnum.FINALIZADO);

        // Assert
        assertThat(sut.status()).isEqualTo(StatusProducaoEnum.FINALIZADO);
        assertThat(ordemPrincipal.getStatus()).isEqualTo(StatusProducaoEnum.FINALIZADO);
        verify(subOrderRepository).save(subOrdem);
        verify(orderRepository).save(ordemPrincipal);
    }

    @Test
    public void alterarStatusSubOrdem_comIdInexistente_lancaNonExistentSubOrderException() {
        // Arrange
        when(subOrderRepository.findById(SubOrderConstants.SUB_ORDEM_ID_INEXISTENTE))
                .thenReturn(Optional.empty());

        // Act e Assert
        assertThatThrownBy(() -> subOrderService.alterarStatusSubOrdem(SubOrderConstants.SUB_ORDEM_ID_INEXISTENTE, StatusProducaoEnum.FINALIZADO))
                .isInstanceOf(NonExistentSubOrderException.class);
    }
}