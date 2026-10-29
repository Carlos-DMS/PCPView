package com.univesp.PCPView.services;

import com.univesp.PCPView.common.ExecutionConstants;
import com.univesp.PCPView.dto.execution.response.ExecutionResponseDTO;
import com.univesp.PCPView.exceptions.ClosedOrderException;
import com.univesp.PCPView.exceptions.ClosedSubOrderException;
import com.univesp.PCPView.exceptions.DatabaseException;
import com.univesp.PCPView.exceptions.ExcessQuantityException;
import com.univesp.PCPView.exceptions.ExecutionAlreadyFinishedException;
import com.univesp.PCPView.exceptions.ExecutionNotRunningException;
import com.univesp.PCPView.exceptions.NonExistentExecutionException;
import com.univesp.PCPView.exceptions.NonExistentMachineException;
import com.univesp.PCPView.exceptions.NonExistentSubOrderException;
import com.univesp.PCPView.exceptions.UnauthorizedExecutionAccessException;
import com.univesp.PCPView.models.ExecutionModel;
import com.univesp.PCPView.models.OrderModel;
import com.univesp.PCPView.models.SubOrderModel;
import com.univesp.PCPView.models.enums.ExecutionStatus;
import com.univesp.PCPView.models.enums.StatusProducaoEnum;
import com.univesp.PCPView.repository.ExecutionRepository;
import com.univesp.PCPView.repository.MachineRepository;
import com.univesp.PCPView.repository.OrderRepository;
import com.univesp.PCPView.repository.SubOrderRepository;
import com.univesp.PCPView.repository.UserRepository;
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
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class ExecutionServiceTest {

    @InjectMocks
    private ExecutionService executionService;

    @Mock
    private ExecutionRepository executionRepository;

    @Mock
    private AuthenticationService authenticationService;

    @Mock
    private MachineRepository machineRepository;

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private SubOrderRepository subOrderRepository;

    @Mock
    private UserRepository userRepository;

    @Test
    public void iniciarExecucao_comDadosValidos_retornaExecutionResponseDTO() {
        // Arrange
        OrderModel ordem = new OrderModel("ORD-001", 100);
        SubOrderModel subOrdem = new SubOrderModel("ORD-001-A-01", ordem, 100);
        ExecutionModel execucaoSalva = ExecutionConstants.criarExecucao(
                ExecutionConstants.EXECUCAO_ID_VALIDO,
                ExecutionConstants.OPERADOR,
                ExecutionConstants.MAQUINA,
                subOrdem,
                ExecutionStatus.RODANDO
        );

        when(authenticationService.extractUser()).thenReturn(ExecutionConstants.OPERADOR);
        when(machineRepository.findById(ExecutionConstants.EXECUCAO_START_REQUEST.idMaquina()))
                .thenReturn(Optional.of(ExecutionConstants.MAQUINA));
        when(subOrderRepository.findById(ExecutionConstants.EXECUCAO_START_REQUEST.idEtapaSubOrdem()))
                .thenReturn(Optional.of(subOrdem));
        when(orderRepository.findById(ordem.getNumeroOrdem()))
                .thenReturn(Optional.of(ordem));
        when(executionRepository.save(any(ExecutionModel.class))).thenReturn(execucaoSalva);

        // Act
        ExecutionResponseDTO sut = executionService.iniciarExecucao(ExecutionConstants.EXECUCAO_START_REQUEST);

        // Assert
        assertThat(sut).isNotNull();
        assertThat(sut.id()).isEqualTo(ExecutionConstants.EXECUCAO_ID_VALIDO);
        assertThat(sut.maquinaId()).isEqualTo(ExecutionConstants.MAQUINA.getId());
        assertThat(sut.subOrdemId()).isEqualTo(subOrdem.getId());
        assertThat(sut.operadorNome()).isEqualTo(ExecutionConstants.OPERADOR.getUsername());
        assertThat(sut.status()).isEqualTo(ExecutionStatus.RODANDO);
        assertThat(subOrdem.getStatus()).isEqualTo(StatusProducaoEnum.EM_PROCESSAMENTO);
        assertThat(ordem.getStatus()).isEqualTo(StatusProducaoEnum.EM_PROCESSAMENTO);

        verify(subOrderRepository).save(subOrdem);
        verify(orderRepository).save(ordem);
        verify(executionRepository).save(any(ExecutionModel.class));
    }

    @Test
    public void iniciarExecucao_comMaquinaInexistente_lancaNonExistentMachineException() {
        // Arrange
        when(authenticationService.extractUser()).thenReturn(ExecutionConstants.OPERADOR);
        when(machineRepository.findById(ExecutionConstants.EXECUCAO_START_REQUEST.idMaquina()))
                .thenReturn(Optional.empty());

        // Act e Assert
        assertThatThrownBy(() -> executionService.iniciarExecucao(ExecutionConstants.EXECUCAO_START_REQUEST))
                .isInstanceOf(NonExistentMachineException.class);
    }

    @Test
    public void iniciarExecucao_comSubOrdemInexistente_lancaNonExistentSubOrderException() {
        // Arrange
        when(authenticationService.extractUser()).thenReturn(ExecutionConstants.OPERADOR);
        when(machineRepository.findById(ExecutionConstants.EXECUCAO_START_REQUEST.idMaquina()))
                .thenReturn(Optional.of(ExecutionConstants.MAQUINA));
        when(subOrderRepository.findById(ExecutionConstants.EXECUCAO_START_REQUEST.idEtapaSubOrdem()))
                .thenReturn(Optional.empty());

        // Act e Assert
        assertThatThrownBy(() -> executionService.iniciarExecucao(ExecutionConstants.EXECUCAO_START_REQUEST))
                .isInstanceOf(NonExistentSubOrderException.class);
    }

    @Test
    public void iniciarExecucao_comOrdemPrincipalInexistente_lancaDatabaseException() {
        // Arrange
        OrderModel ordem = new OrderModel("ORD-001", 100);
        SubOrderModel subOrdem = new SubOrderModel("ORD-001-A-01", ordem, 100);

        when(authenticationService.extractUser()).thenReturn(ExecutionConstants.OPERADOR);
        when(machineRepository.findById(ExecutionConstants.EXECUCAO_START_REQUEST.idMaquina()))
                .thenReturn(Optional.of(ExecutionConstants.MAQUINA));
        when(subOrderRepository.findById(ExecutionConstants.EXECUCAO_START_REQUEST.idEtapaSubOrdem()))
                .thenReturn(Optional.of(subOrdem));
        when(orderRepository.findById(ordem.getNumeroOrdem())).thenReturn(Optional.empty());

        // Act e Assert
        assertThatThrownBy(() -> executionService.iniciarExecucao(ExecutionConstants.EXECUCAO_START_REQUEST))
                .isInstanceOf(DatabaseException.class);
    }

    @Test
    public void iniciarExecucao_quandoOrdemPrincipalCancelada_lancaClosedOrderException() {
        // Arrange
        OrderModel ordemCancelada = new OrderModel("ORD-001", 100);
        ordemCancelada.setStatus(StatusProducaoEnum.CANCELADO);
        SubOrderModel subOrdem = new SubOrderModel("ORD-001-A-01", ordemCancelada, 100);

        when(authenticationService.extractUser()).thenReturn(ExecutionConstants.OPERADOR);
        when(machineRepository.findById(ExecutionConstants.EXECUCAO_START_REQUEST.idMaquina()))
                .thenReturn(Optional.of(ExecutionConstants.MAQUINA));
        when(subOrderRepository.findById(ExecutionConstants.EXECUCAO_START_REQUEST.idEtapaSubOrdem()))
                .thenReturn(Optional.of(subOrdem));
        when(orderRepository.findById(ordemCancelada.getNumeroOrdem()))
                .thenReturn(Optional.of(ordemCancelada));
        when(subOrderRepository.findAllByOrdemPrincipal(ordemCancelada)).thenReturn(List.of(subOrdem));

        // Act e Assert
        assertThatThrownBy(() -> executionService.iniciarExecucao(ExecutionConstants.EXECUCAO_START_REQUEST))
                .isInstanceOf(ClosedOrderException.class);

        verify(subOrderRepository).saveAll(anyList());
    }

    @Test
    public void iniciarExecucao_quandoSubOrdemCancelada_lancaClosedSubOrderException() {
        // Arrange
        OrderModel ordem = new OrderModel("ORD-001", 100);
        SubOrderModel subOrdemCancelada = new SubOrderModel("ORD-001-A-01", ordem, 100);
        subOrdemCancelada.setStatus(StatusProducaoEnum.CANCELADO);

        when(authenticationService.extractUser()).thenReturn(ExecutionConstants.OPERADOR);
        when(machineRepository.findById(ExecutionConstants.EXECUCAO_START_REQUEST.idMaquina()))
                .thenReturn(Optional.of(ExecutionConstants.MAQUINA));
        when(subOrderRepository.findById(ExecutionConstants.EXECUCAO_START_REQUEST.idEtapaSubOrdem()))
                .thenReturn(Optional.of(subOrdemCancelada));
        when(orderRepository.findById(ordem.getNumeroOrdem())).thenReturn(Optional.of(ordem));

        // Act e Assert
        assertThatThrownBy(() -> executionService.iniciarExecucao(ExecutionConstants.EXECUCAO_START_REQUEST))
                .isInstanceOf(ClosedSubOrderException.class);
    }

    @Test
    public void iniciarExecucao_quandoQuantidadeRestanteZero_lancaExcessQuantityException() {
        // Arrange
        OrderModel ordem = new OrderModel("ORD-001", 100);
        SubOrderModel subOrdemEsgotada = new SubOrderModel("ORD-001-A-01", ordem, 100);
        subOrdemEsgotada.adicionarQuantidadeProduzida(100);

        when(authenticationService.extractUser()).thenReturn(ExecutionConstants.OPERADOR);
        when(machineRepository.findById(ExecutionConstants.EXECUCAO_START_REQUEST.idMaquina()))
                .thenReturn(Optional.of(ExecutionConstants.MAQUINA));
        when(subOrderRepository.findById(ExecutionConstants.EXECUCAO_START_REQUEST.idEtapaSubOrdem()))
                .thenReturn(Optional.of(subOrdemEsgotada));
        when(orderRepository.findById(ordem.getNumeroOrdem())).thenReturn(Optional.of(ordem));

        // Act e Assert
        assertThatThrownBy(() -> executionService.iniciarExecucao(ExecutionConstants.EXECUCAO_START_REQUEST))
                .isInstanceOf(ExcessQuantityException.class);
    }

    @Test
    public void finalizarExecucao_comOperadorDonoEQuantidadeValida_finalizaComSucesso() {
        // Arrange
        OrderModel ordem = new OrderModel("ORD-001", 100);
        SubOrderModel subOrdem = new SubOrderModel("ORD-001-A-01", ordem, 100);
        ExecutionModel execucao = ExecutionConstants.criarExecucao(
                ExecutionConstants.EXECUCAO_ID_VALIDO,
                ExecutionConstants.OPERADOR,
                ExecutionConstants.MAQUINA,
                subOrdem,
                ExecutionStatus.RODANDO
        );

        when(authenticationService.extractUser()).thenReturn(ExecutionConstants.OPERADOR);
        when(executionRepository.findById(execucao.getId())).thenReturn(Optional.of(execucao));
        when(subOrderRepository.findById(subOrdem.getId())).thenReturn(Optional.of(subOrdem));
        when(orderRepository.findById(ordem.getNumeroOrdem())).thenReturn(Optional.of(ordem));
        when(machineRepository.findById(ExecutionConstants.MAQUINA.getId())).thenReturn(Optional.of(ExecutionConstants.MAQUINA));

        // Act
        ExecutionResponseDTO sut = executionService.finalizarExecucao(execucao.getId(), 50);

        // Assert
        assertThat(sut.status()).isEqualTo(ExecutionStatus.FINALIZADA);
        assertThat(execucao.getQuantidadeFeitaNestaSessao()).isEqualTo(50);
        assertThat(subOrdem.getQuantidadeProduzida()).isEqualTo(50);
        assertThat(execucao.getDataFim()).isNotNull();

        verify(subOrderRepository).save(subOrdem);
        verify(executionRepository).save(execucao);
        verify(orderRepository, never()).save(any());
    }

    @Test
    public void finalizarExecucao_quandoTodasSubOrdensFinalizadas_finalizaOrdemPrincipalTambem() {
        // Arrange
        OrderModel ordem = new OrderModel("ORD-001", 100);
        SubOrderModel subOrdem = new SubOrderModel("ORD-001-A-01", ordem, 100);
        ExecutionModel execucao = ExecutionConstants.criarExecucao(
                ExecutionConstants.EXECUCAO_ID_VALIDO,
                ExecutionConstants.OPERADOR,
                ExecutionConstants.MAQUINA,
                subOrdem,
                ExecutionStatus.RODANDO
        );

        when(authenticationService.extractUser()).thenReturn(ExecutionConstants.OPERADOR);
        when(executionRepository.findById(execucao.getId())).thenReturn(Optional.of(execucao));
        when(subOrderRepository.findById(subOrdem.getId())).thenReturn(Optional.of(subOrdem));
        when(orderRepository.findById(ordem.getNumeroOrdem())).thenReturn(Optional.of(ordem));
        when(machineRepository.findById(ExecutionConstants.MAQUINA.getId())).thenReturn(Optional.of(ExecutionConstants.MAQUINA));
        when(subOrderRepository.findAllByOrdemPrincipal(ordem)).thenReturn(List.of(subOrdem));

        // Act
        ExecutionResponseDTO sut = executionService.finalizarExecucao(execucao.getId(), 100);

        // Assert
        assertThat(sut.status()).isEqualTo(ExecutionStatus.FINALIZADA);
        assertThat(subOrdem.getStatus()).isEqualTo(StatusProducaoEnum.FINALIZADO);
        assertThat(ordem.getStatus()).isEqualTo(StatusProducaoEnum.FINALIZADO);

        verify(orderRepository).save(ordem);
        verify(subOrderRepository).save(subOrdem);
        verify(executionRepository).save(execucao);
    }

    @Test
    public void finalizarExecucao_comOutroOperadorNaoAdmin_lancaUnauthorizedExecutionAccessException() {
        // Arrange
        OrderModel ordem = new OrderModel("ORD-001", 100);
        SubOrderModel subOrdem = new SubOrderModel("ORD-001-A-01", ordem, 100);
        ExecutionModel execucao = ExecutionConstants.criarExecucao(
                ExecutionConstants.EXECUCAO_ID_VALIDO,
                ExecutionConstants.OPERADOR,
                ExecutionConstants.MAQUINA,
                subOrdem,
                ExecutionStatus.RODANDO
        );

        when(authenticationService.extractUser()).thenReturn(ExecutionConstants.OUTRO_OPERADOR);
        when(executionRepository.findById(execucao.getId())).thenReturn(Optional.of(execucao));

        // Act e Assert
        assertThatThrownBy(() -> executionService.finalizarExecucao(execucao.getId(), 50))
                .isInstanceOf(UnauthorizedExecutionAccessException.class);
    }

    @Test
    public void finalizarExecucao_quandoExecucaoNaoEstaRodando_lancaExecutionNotRunningException() {
        // Arrange
        OrderModel ordem = new OrderModel("ORD-001", 100);
        SubOrderModel subOrdem = new SubOrderModel("ORD-001-A-01", ordem, 100);
        ExecutionModel execucaoFinalizada = ExecutionConstants.criarExecucao(
                ExecutionConstants.EXECUCAO_ID_VALIDO,
                ExecutionConstants.OPERADOR,
                ExecutionConstants.MAQUINA,
                subOrdem,
                ExecutionStatus.FINALIZADA
        );

        when(authenticationService.extractUser()).thenReturn(ExecutionConstants.OPERADOR);
        when(executionRepository.findById(execucaoFinalizada.getId())).thenReturn(Optional.of(execucaoFinalizada));

        // Act e Assert
        assertThatThrownBy(() -> executionService.finalizarExecucao(execucaoFinalizada.getId(), 50))
                .isInstanceOf(ExecutionNotRunningException.class);
    }

    @Test
    public void finalizarExecucao_comIdInexistente_lancaNonExistentExecutionException() {
        // Arrange
        when(executionRepository.findById(ExecutionConstants.EXECUCAO_ID_INEXISTENTE))
                .thenReturn(Optional.empty());

        // Act e Assert
        assertThatThrownBy(() -> executionService.finalizarExecucao(ExecutionConstants.EXECUCAO_ID_INEXISTENTE, 50))
                .isInstanceOf(NonExistentExecutionException.class);
    }

    @Test
    public void finalizarExecucao_quandoQuantidadeExcedeLimite_lancaExcessQuantityException() {
        // Arrange
        OrderModel ordem = new OrderModel("ORD-001", 100);
        SubOrderModel subOrdem = new SubOrderModel("ORD-001-A-01", ordem, 100);
        ExecutionModel execucao = ExecutionConstants.criarExecucao(
                ExecutionConstants.EXECUCAO_ID_VALIDO,
                ExecutionConstants.OPERADOR,
                ExecutionConstants.MAQUINA,
                subOrdem,
                ExecutionStatus.RODANDO
        );

        when(authenticationService.extractUser()).thenReturn(ExecutionConstants.OPERADOR);
        when(executionRepository.findById(execucao.getId())).thenReturn(Optional.of(execucao));
        when(subOrderRepository.findById(subOrdem.getId())).thenReturn(Optional.of(subOrdem));
        when(orderRepository.findById(ordem.getNumeroOrdem())).thenReturn(Optional.of(ordem));
        when(machineRepository.findById(ExecutionConstants.MAQUINA.getId())).thenReturn(Optional.of(ExecutionConstants.MAQUINA));

        // Act e Assert
        assertThatThrownBy(() -> executionService.finalizarExecucao(execucao.getId(), 150))
                .isInstanceOf(ExcessQuantityException.class);
    }

    @Test
    public void listarTodas_comFiltros_retornaListaDeExecutionResponseDTO() {
        // Arrange
        OrderModel ordem = new OrderModel("ORD-001", 100);
        SubOrderModel subOrdem = new SubOrderModel("ORD-001-A-01", ordem, 100);
        ExecutionModel execucao = ExecutionConstants.criarExecucao(
                ExecutionConstants.EXECUCAO_ID_VALIDO,
                ExecutionConstants.OPERADOR,
                ExecutionConstants.MAQUINA,
                subOrdem,
                ExecutionStatus.RODANDO
        );

        when(executionRepository.findComFiltros("MAQ-01", ExecutionStatus.RODANDO, "ORD-001"))
                .thenReturn(List.of(execucao));

        // Act
        List<ExecutionResponseDTO> sut = executionService.listarTodas("MAQ-01", ExecutionStatus.RODANDO, "ORD-001");

        // Assert
        assertThat(sut).isNotEmpty();
        assertThat(sut).hasSize(1);
        assertThat(sut.getFirst().id()).isEqualTo(execucao.getId());
    }

    @Test
    public void listarTodas_quandoNaoHouverRegistros_retornaListaVazia() {
        // Arrange
        when(executionRepository.findComFiltros(null, null, null))
                .thenReturn(Collections.emptyList());

        // Act
        List<ExecutionResponseDTO> sut = executionService.listarTodas(null, null, null);

        // Assert
        assertThat(sut).isEmpty();
    }

    @Test
    public void cancelarExecucao_comOperadorDonoEStatusRodando_deletaComSucesso() {
        // Arrange
        OrderModel ordem = new OrderModel("ORD-001", 100);
        SubOrderModel subOrdem = new SubOrderModel("ORD-001-A-01", ordem, 100);
        ExecutionModel execucao = ExecutionConstants.criarExecucao(
                ExecutionConstants.EXECUCAO_ID_VALIDO,
                ExecutionConstants.OPERADOR,
                ExecutionConstants.MAQUINA,
                subOrdem,
                ExecutionStatus.RODANDO
        );

        when(executionRepository.findById(execucao.getId())).thenReturn(Optional.of(execucao));
        when(authenticationService.extractUser()).thenReturn(ExecutionConstants.OPERADOR);

        // Act e Assert
        assertThatCode(() -> executionService.cancelarExecucao(execucao.getId()))
                .doesNotThrowAnyException();

        verify(executionRepository).delete(execucao);
    }

    @Test
    public void cancelarExecucao_comAdminEStatusRodandoMesmoNaoSendoDono_deletaComSucesso() {
        // Arrange
        OrderModel ordem = new OrderModel("ORD-001", 100);
        SubOrderModel subOrdem = new SubOrderModel("ORD-001-A-01", ordem, 100);
        ExecutionModel execucao = ExecutionConstants.criarExecucao(
                ExecutionConstants.EXECUCAO_ID_VALIDO,
                ExecutionConstants.OPERADOR,
                ExecutionConstants.MAQUINA,
                subOrdem,
                ExecutionStatus.RODANDO
        );

        when(executionRepository.findById(execucao.getId())).thenReturn(Optional.of(execucao));
        when(authenticationService.extractUser()).thenReturn(ExecutionConstants.ADMIN);

        // Act e Assert
        assertThatCode(() -> executionService.cancelarExecucao(execucao.getId()))
                .doesNotThrowAnyException();

        verify(executionRepository).delete(execucao);
    }

    @Test
    public void cancelarExecucao_quandoExecucaoJaFinalizada_lancaExecutionAlreadyFinishedException() {
        // Arrange
        OrderModel ordem = new OrderModel("ORD-001", 100);
        SubOrderModel subOrdem = new SubOrderModel("ORD-001-A-01", ordem, 100);
        ExecutionModel execucaoFinalizada = ExecutionConstants.criarExecucao(
                ExecutionConstants.EXECUCAO_ID_VALIDO,
                ExecutionConstants.OPERADOR,
                ExecutionConstants.MAQUINA,
                subOrdem,
                ExecutionStatus.FINALIZADA
        );

        when(executionRepository.findById(execucaoFinalizada.getId())).thenReturn(Optional.of(execucaoFinalizada));

        // Act e Assert
        assertThatThrownBy(() -> executionService.cancelarExecucao(execucaoFinalizada.getId()))
                .isInstanceOf(ExecutionAlreadyFinishedException.class);

        verify(executionRepository, never()).delete(any());
    }

    @Test
    public void cancelarExecucao_comOutroOperadorNaoAdmin_lancaUnauthorizedExecutionAccessException() {
        // Arrange
        OrderModel ordem = new OrderModel("ORD-001", 100);
        SubOrderModel subOrdem = new SubOrderModel("ORD-001-A-01", ordem, 100);
        ExecutionModel execucao = ExecutionConstants.criarExecucao(
                ExecutionConstants.EXECUCAO_ID_VALIDO,
                ExecutionConstants.OPERADOR,
                ExecutionConstants.MAQUINA,
                subOrdem,
                ExecutionStatus.RODANDO
        );

        when(executionRepository.findById(execucao.getId())).thenReturn(Optional.of(execucao));
        when(authenticationService.extractUser()).thenReturn(ExecutionConstants.OUTRO_OPERADOR);

        // Act e Assert
        assertThatThrownBy(() -> executionService.cancelarExecucao(execucao.getId()))
                .isInstanceOf(UnauthorizedExecutionAccessException.class);

        verify(executionRepository, never()).delete(any());
    }

    @Test
    public void cancelarExecucao_comIdInexistente_lancaNonExistentExecutionException() {
        // Arrange
        when(executionRepository.findById(ExecutionConstants.EXECUCAO_ID_INEXISTENTE))
                .thenReturn(Optional.empty());

        // Act e Assert
        assertThatThrownBy(() -> executionService.cancelarExecucao(ExecutionConstants.EXECUCAO_ID_INEXISTENTE))
                .isInstanceOf(NonExistentExecutionException.class);
    }
}