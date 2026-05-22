package com.univesp.PCPView.services;

import com.univesp.PCPView.dto.machine.request.MachineRequestDTO;
import com.univesp.PCPView.dto.machine.request.UpdateMachineNameDTO;
import com.univesp.PCPView.dto.machine.response.MachineResponseDTO;
import com.univesp.PCPView.exceptions.NonExistentMachineException;
import com.univesp.PCPView.models.ExecutionModel;
import com.univesp.PCPView.models.MachineModel;
import com.univesp.PCPView.models.OrderModel;
import com.univesp.PCPView.models.SubOrderModel;
import com.univesp.PCPView.models.enums.ExecutionStatus;
import com.univesp.PCPView.models.enums.StatusProducaoEnum;
import com.univesp.PCPView.repository.ExecutionRepository;
import com.univesp.PCPView.repository.MachineRepository;
import com.univesp.PCPView.repository.OrderRepository;
import com.univesp.PCPView.repository.SubOrderRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class MachineService {

    private final MachineRepository machineRepository;
    private final ExecutionRepository executionRepository;
    private final SubOrderRepository subOrderRepository;
    private final OrderRepository orderRepository;

    public MachineService(MachineRepository machineRepository, ExecutionRepository executionRepository, SubOrderRepository subOrderRepository, OrderRepository orderRepository) {
        this.machineRepository = machineRepository;
        this.executionRepository = executionRepository;
        this.subOrderRepository = subOrderRepository;
        this.orderRepository = orderRepository;
    }

    @Transactional
    public MachineResponseDTO registrarMaquina(MachineRequestDTO body) {
        MachineModel maquina = new MachineModel(body.id(), body.nome());

        machineRepository.save(maquina);

        return converterMaquinaParaResponseDTO(maquina);
    }

    public MachineResponseDTO buscarMaquinaPorID(String id) {
        MachineModel maquina = machineRepository.findById(id).orElseThrow(NonExistentMachineException::new);

        return converterMaquinaParaResponseDTO(maquina);
    }

    public List<MachineResponseDTO> buscarTodasMaquinas() {
        return machineRepository.findAll().stream().map(this::converterMaquinaParaResponseDTO).toList();
    }

    public List<MachineResponseDTO> buscarMaquinasPorStatusOperacional(Boolean operacional) {
        return machineRepository.findByOperacional(operacional).stream().map(this::converterMaquinaParaResponseDTO).toList();
    }

    @Transactional
    public MachineResponseDTO alternarStatusOperacional(String id) {
        MachineModel maquina = machineRepository.findById(id).orElseThrow(NonExistentMachineException::new);

        maquina.alternarStatusOperacional();

        if (!maquina.getOperacional()) {
            devolverExecucaoAbertaParaFila(maquina);
        }

        machineRepository.save(maquina);

        return converterMaquinaParaResponseDTO(maquina);
    }

    @Transactional
    public MachineResponseDTO alterarNome(String id, UpdateMachineNameDTO body) {
        MachineModel maquina = machineRepository.findById(id).orElseThrow(NonExistentMachineException::new);

        maquina.setNome(body.nome());

        machineRepository.save(maquina);

        return converterMaquinaParaResponseDTO(maquina);
    }

    @Transactional
    public void deletarMaquina(String id) {
        MachineModel maquina = machineRepository.findById(id).orElseThrow(NonExistentMachineException::new);

        machineRepository.delete(maquina);
    }

    private MachineResponseDTO converterMaquinaParaResponseDTO(MachineModel maquina) {
        return new MachineResponseDTO(maquina.getId(), maquina.getNome(), maquina.getOperacional(), statusOperacional(maquina));
    }

    private String statusOperacional(MachineModel maquina) {
        if (!Boolean.TRUE.equals(maquina.getOperacional())) {
            return "MANUTENCAO";
        }

        return temExecucaoAberta(maquina) ? "TRABALHANDO" : "DISPONIVEL";
    }

    private boolean temExecucaoAberta(MachineModel maquina) {
        return executionRepository.existsByMaquinaAndStatusIn(maquina, statusExecucaoAberta());
    }

    private void devolverExecucaoAbertaParaFila(MachineModel maquina) {
        executionRepository.findFirstByMaquinaAndStatusInOrderByDataInicioDesc(maquina, statusExecucaoAberta())
                .ifPresent(execucao -> {
                    execucao.finalizarSetup();
                    execucao.inserirDataFim();
                    execucao.setStatus(ExecutionStatus.CANCELADA_MANUTENCAO);
                    execucao.setQuantidadeFeitaNestaSessao(0);

                    SubOrderModel subOrdem = execucao.getSubOrdem();
                    subOrdem.setStatus(StatusProducaoEnum.AGUARDANDO);

                    OrderModel ordem = subOrdem.getOrdemPrincipal();
                    ordem.setStatus(StatusProducaoEnum.AGUARDANDO);

                    executionRepository.save(execucao);
                    subOrderRepository.save(subOrdem);
                    orderRepository.save(ordem);
                });
    }

    private List<ExecutionStatus> statusExecucaoAberta() {
        return List.of(ExecutionStatus.RODANDO, ExecutionStatus.PAUSADA_POR_QUEBRA);
    }
}
