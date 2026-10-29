package com.univesp.PCPView.services;

import com.univesp.PCPView.common.MachineConstants;
import com.univesp.PCPView.dto.machine.response.MachineResponseDTO;
import com.univesp.PCPView.exceptions.NonExistentMachineException;
import com.univesp.PCPView.models.ExecutionModel;
import com.univesp.PCPView.models.MachineModel;
import com.univesp.PCPView.models.enums.ExecutionStatus;
import com.univesp.PCPView.repository.ExecutionRepository;
import com.univesp.PCPView.repository.MachineRepository;
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
public class MachineServiceTest {

    @InjectMocks
    private MachineService machineService;

    @Mock
    private MachineRepository machineRepository;

    @Mock
    private ExecutionRepository executionRepository;

    @Test
    public void registrarMaquina_comDadosValidos_retornaMachineResponseDTO() {
        // Arrange
        when(machineRepository.save(any(MachineModel.class))).thenReturn(MachineConstants.MAQUINA_VALIDA);

        // Act
        MachineResponseDTO sut = machineService.registrarMaquina(MachineConstants.MAQUINA_REQUEST_VALIDO);

        // Assert
        assertThat(sut).usingRecursiveComparison().isEqualTo(MachineConstants.MAQUINA_RESPONSE_VALIDA);
    }

    @Test
    public void registrarMaquina_comDadosInvalidos_lancaExcecao() {
        // Arrange
        when(machineRepository.save(any(MachineModel.class))).thenThrow(RuntimeException.class);

        // Act e Assert
        assertThatThrownBy(() -> machineService.registrarMaquina(MachineConstants.MAQUINA_REQUEST_INVALIDO))
                .isInstanceOf(RuntimeException.class);
    }

    @Test
    public void buscarMaquinaPorID_comIdExistente_retornaMachineResponseDTO() {
        // Arrange
        when(machineRepository.findById(MachineConstants.MAQUINA_VALIDA.getId()))
                .thenReturn(Optional.of(MachineConstants.MAQUINA_VALIDA));

        // Act
        MachineResponseDTO sut = machineService.buscarMaquinaPorID(MachineConstants.MAQUINA_VALIDA.getId());

        // Assert
        assertThat(sut).usingRecursiveComparison().isEqualTo(MachineConstants.MAQUINA_RESPONSE_VALIDA);
    }

    @Test
    public void buscarMaquinaPorID_comIdInexistente_lancaNonExistentMachineException() {
        // Arrange
        when(machineRepository.findById(MachineConstants.MAQUINA_ID_INEXISTENTE))
                .thenReturn(Optional.empty());

        // Act e Assert
        assertThatThrownBy(() -> machineService.buscarMaquinaPorID(MachineConstants.MAQUINA_ID_INEXISTENTE))
                .isInstanceOf(NonExistentMachineException.class);
    }

    @Test
    public void buscarTodasMaquinas_quandoExistiremMaquinas_retornaListaDeMachineResponseDTO() {
        // Arrange
        when(machineRepository.findAll()).thenReturn(List.of(MachineConstants.MAQUINA_VALIDA));

        // Act
        List<MachineResponseDTO> sut = machineService.buscarTodasMaquinas();

        // Assert
        assertThat(sut).isNotEmpty();
        assertThat(sut).hasSize(1);
        assertThat(sut.getFirst()).usingRecursiveComparison().isEqualTo(MachineConstants.MAQUINA_RESPONSE_VALIDA);
    }

    @Test
    public void buscarTodasMaquinas_quandoNaoExistiremMaquinas_retornaListaVazia() {
        // Arrange
        when(machineRepository.findAll()).thenReturn(Collections.emptyList());

        // Act
        List<MachineResponseDTO> sut = machineService.buscarTodasMaquinas();

        // Assert
        assertThat(sut).isEmpty();
    }

    @Test
    public void buscarMaquinasPorStatusOperacional_quandoExistiremMaquinas_retornaListaFiltrada() {
        // Arrange
        when(machineRepository.findByOperacional(true)).thenReturn(List.of(MachineConstants.MAQUINA_VALIDA));

        // Act
        List<MachineResponseDTO> sut = machineService.buscarMaquinasPorStatusOperacional(true);

        // Assert
        assertThat(sut).isNotEmpty();
        assertThat(sut.getFirst().operacional()).isTrue();
    }

    @Test
    public void alternarStatusOperacional_comIdExistenteSemExecucoes_alternaStatusParaInoperante() {
        // Arrange
        MachineModel maquinaEmMemoria = new MachineModel(MachineConstants.MAQUINA_VALIDA.getId(), MachineConstants.MAQUINA_VALIDA.getNome());
        when(machineRepository.findById(maquinaEmMemoria.getId())).thenReturn(Optional.of(maquinaEmMemoria));
        when(machineRepository.save(any(MachineModel.class))).thenReturn(maquinaEmMemoria);
        when(executionRepository.findByMaquinaIdAndStatusNot(maquinaEmMemoria.getId(), ExecutionStatus.FINALIZADA))
                .thenReturn(Collections.emptyList());

        // Act
        MachineResponseDTO sut = machineService.alternarStatusOperacional(maquinaEmMemoria.getId());

        // Assert
        assertThat(sut.operacional()).isFalse();
        verify(executionRepository, never()).saveAll(any());
    }

    @Test
    public void alternarStatusOperacional_quandoMaquinaFicaInoperanteComExecucoes_pausaExecucoesPorQuebra() {
        // Arrange
        MachineModel maquinaEmMemoria = new MachineModel(MachineConstants.MAQUINA_VALIDA.getId(), MachineConstants.MAQUINA_VALIDA.getNome());
        ExecutionModel execucao = new ExecutionModel();
        execucao.setStatus(ExecutionStatus.RODANDO);

        when(machineRepository.findById(maquinaEmMemoria.getId())).thenReturn(Optional.of(maquinaEmMemoria));
        when(machineRepository.save(any(MachineModel.class))).thenReturn(maquinaEmMemoria);
        when(executionRepository.findByMaquinaIdAndStatusNot(maquinaEmMemoria.getId(), ExecutionStatus.FINALIZADA))
                .thenReturn(List.of(execucao));

        // Act
        MachineResponseDTO sut = machineService.alternarStatusOperacional(maquinaEmMemoria.getId());

        // Assert
        assertThat(sut.operacional()).isFalse();
        assertThat(execucao.getStatus()).isEqualTo(ExecutionStatus.PAUSADA_POR_QUEBRA);
        verify(executionRepository).saveAll(List.of(execucao));
    }

    @Test
    public void alternarStatusOperacional_quandoMaquinaRetornaParaOperacionalComExecucoes_retomaExecucoesParaRodando() {
        // Arrange
        MachineModel maquinaInoperante = new MachineModel(MachineConstants.MAQUINA_VALIDA.getId(), MachineConstants.MAQUINA_VALIDA.getNome());
        maquinaInoperante.alternarStatusOperacional(); // Inicializa como inoperante (false)

        ExecutionModel execucaoPausada = new ExecutionModel();
        execucaoPausada.setStatus(ExecutionStatus.PAUSADA_POR_QUEBRA);

        when(machineRepository.findById(maquinaInoperante.getId())).thenReturn(Optional.of(maquinaInoperante));
        when(machineRepository.save(any(MachineModel.class))).thenReturn(maquinaInoperante);
        when(executionRepository.findByMaquinaIdAndStatusNot(maquinaInoperante.getId(), ExecutionStatus.FINALIZADA))
                .thenReturn(List.of(execucaoPausada));

        // Act
        MachineResponseDTO sut = machineService.alternarStatusOperacional(maquinaInoperante.getId());

        // Assert
        assertThat(sut.operacional()).isTrue();
        assertThat(execucaoPausada.getStatus()).isEqualTo(ExecutionStatus.RODANDO);
        verify(executionRepository).saveAll(List.of(execucaoPausada));
    }

    @Test
    public void alternarStatusOperacional_comIdInexistente_lancaNonExistentMachineException() {
        // Arrange
        when(machineRepository.findById(MachineConstants.MAQUINA_ID_INEXISTENTE))
                .thenReturn(Optional.empty());

        // Act e Assert
        assertThatThrownBy(() -> machineService.alternarStatusOperacional(MachineConstants.MAQUINA_ID_INEXISTENTE))
                .isInstanceOf(NonExistentMachineException.class);
    }

    @Test
    public void alterarNome_comIdExistente_retornaMachineResponseDTOAtualizado() {
        // Arrange
        MachineModel maquinaEmMemoria = new MachineModel(MachineConstants.MAQUINA_VALIDA.getId(), MachineConstants.MAQUINA_VALIDA.getNome());
        when(machineRepository.findById(maquinaEmMemoria.getId())).thenReturn(Optional.of(maquinaEmMemoria));
        when(machineRepository.save(any(MachineModel.class))).thenReturn(maquinaEmMemoria);

        // Act
        MachineResponseDTO sut = machineService.alterarNome(maquinaEmMemoria.getId(), MachineConstants.NOVO_NOME_MAQUINA);

        // Assert
        assertThat(sut).usingRecursiveComparison().isEqualTo(MachineConstants.MAQUINA_RESPONSE_NOME_ATUALIZADO);
    }

    @Test
    public void alterarNome_comIdInexistente_lancaNonExistentMachineException() {
        // Arrange
        when(machineRepository.findById(MachineConstants.MAQUINA_ID_INEXISTENTE))
                .thenReturn(Optional.empty());

        // Act e Assert
        assertThatThrownBy(() -> machineService.alterarNome(MachineConstants.MAQUINA_ID_INEXISTENTE, MachineConstants.NOVO_NOME_MAQUINA))
                .isInstanceOf(NonExistentMachineException.class);
    }

    @Test
    public void deletarMaquina_comIdExistente_deletaComSucesso() {
        // Arrange
        when(machineRepository.findById(MachineConstants.MAQUINA_VALIDA.getId()))
                .thenReturn(Optional.of(MachineConstants.MAQUINA_VALIDA));

        // Act e Assert
        assertThatCode(() -> machineService.deletarMaquina(MachineConstants.MAQUINA_VALIDA.getId()))
                .doesNotThrowAnyException();

        verify(machineRepository).delete(MachineConstants.MAQUINA_VALIDA);
    }

    @Test
    public void deletarMaquina_comIdInexistente_lancaNonExistentMachineException() {
        // Arrange
        when(machineRepository.findById(MachineConstants.MAQUINA_ID_INEXISTENTE))
                .thenReturn(Optional.empty());

        // Act e Assert
        assertThatThrownBy(() -> machineService.deletarMaquina(MachineConstants.MAQUINA_ID_INEXISTENTE))
                .isInstanceOf(NonExistentMachineException.class);

        verify(machineRepository, never()).delete(any());
    }
}