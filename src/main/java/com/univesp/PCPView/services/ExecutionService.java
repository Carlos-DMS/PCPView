package com.univesp.PCPView.services;

import com.univesp.PCPView.dto.execution.request.ExecutionFinishRequestDTO;
import com.univesp.PCPView.dto.execution.request.ExecutionStartRequestDTO;
import com.univesp.PCPView.dto.execution.response.ExecutionResponseDTO;
import com.univesp.PCPView.exceptions.*;
import com.univesp.PCPView.models.*;
import com.univesp.PCPView.models.enums.ExecutionStatus;
import com.univesp.PCPView.models.enums.RoleEnum;
import com.univesp.PCPView.models.enums.StatusProducaoEnum;
import com.univesp.PCPView.repository.*;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ExecutionService {
    private final ExecutionRepository executionRepository;
    private final AuthenticationService authenticationService;
    private final MachineRepository machineRepository;
    private final OrderRepository orderRepository;
    private final SubOrderRepository subOrderRepository;
    private final UserRepository userRepository;

    public ExecutionService(ExecutionRepository executionRepository, AuthenticationService authenticationService, MachineRepository machineRepository, OrderRepository orderRepository, SubOrderRepository subOrderRepository, UserRepository userRepository) {
        this.executionRepository = executionRepository;
        this.authenticationService = authenticationService;
        this.machineRepository = machineRepository;
        this.orderRepository = orderRepository;
        this.subOrderRepository = subOrderRepository;
        this.userRepository = userRepository;
    }

    @Transactional
    public ExecutionResponseDTO iniciarExecucao(ExecutionStartRequestDTO body) {
        UserModel operador = authenticationService.extractUser();

        MachineModel maquina = machineRepository.findById(body.idMaquina()).orElseThrow(NonExistentMachineException::new);

        SubOrderModel subOrdem = subOrderRepository.findById(body.idEtapaSubOrdem()).orElseThrow(NonExistentSubOrderException::new);

        OrderModel ordemPrincipal = orderRepository.findById(subOrdem.getOrdemPrincipal().getNumeroOrdem()).orElseThrow(DatabaseException::new);

        operacaoValidaParaExecucao(ordemPrincipal, subOrdem);

        if (subOrdem.getQuantidadeRestante() <= 0) {
            throw new ExcessQuantityException();
        }

        if (subOrdem.getStatus().equals(StatusProducaoEnum.AGUARDANDO)) {
            subOrdem.setStatus(StatusProducaoEnum.EM_PROCESSAMENTO);

            subOrderRepository.save(subOrdem);
        }

        ExecutionModel execucao = executionRepository.save(new ExecutionModel(operador, maquina, subOrdem));

        return new ExecutionResponseDTO(
                execucao.getId(),
                maquina.getNome(),
                subOrdem.getId(),
                operador.getUsername(),
                execucao.getStatus(),
                execucao.getDataInicio(),
                execucao.getDataFim()
        );
    }

    @Transactional
    public ExecutionResponseDTO finalizarExecucao(ExecutionFinishRequestDTO body) {
        UserModel usuarioLogado = authenticationService.extractUser();

        ExecutionModel execucao = executionRepository.findById(body.idExecucao()).orElseThrow(NonExistentExecutionException::new);

        SubOrderModel subOrdem = subOrderRepository.findById(execucao.getSubOrdem().getId()).orElseThrow(NonExistentSubOrderException::new);

        OrderModel ordemPrincipal = orderRepository.findById(subOrdem.getOrdemPrincipal().getNumeroOrdem()).orElseThrow(DatabaseException::new);

        MachineModel maquina = machineRepository.findById(execucao.getMaquina().getId()).orElseThrow(NonExistentMachineException::new);

        if (!execucao.getOperador().getId().equals(usuarioLogado.getId()) && !usuarioLogado.getRole().equals(RoleEnum.ADMIN)) {
            throw new UnauthorizedExecutionAccessException();
        }

        if (!execucao.getStatus().equals(ExecutionStatus.RODANDO)) {
            throw new ExecutionNotRunningException();
        }

        execucao.setQuantidadeFeitaNestaSessao(body.quantidadeProduzida());
        execucao.inserirDataFim();
        execucao.setStatus(ExecutionStatus.FINALIZADA);

        subOrdem.adicionarQuantidadeProduzida(body.quantidadeProduzida());

        operacaoValidaParaExecucao(ordemPrincipal, subOrdem);

        if (subOrdem.getQuantidadeRestante() <= 0) {
            subOrdem.setStatus(StatusProducaoEnum.FINALIZADO);

            List<SubOrderModel> subOrdensDaOrdemPrincipal = subOrderRepository.findAllByOrdemPrincipal(ordemPrincipal);

            boolean todasFinalizadas = subOrdensDaOrdemPrincipal.stream()
                    .allMatch(s -> s.getStatus().equals(StatusProducaoEnum.FINALIZADO));

            if (todasFinalizadas) {
                ordemPrincipal.setStatus(StatusProducaoEnum.FINALIZADO);

                orderRepository.save(ordemPrincipal);
            }
        }

        subOrderRepository.save(subOrdem);
        executionRepository.save(execucao);

        return new ExecutionResponseDTO(
                execucao.getId(),
                maquina.getNome(),
                subOrdem.getId(),
                usuarioLogado.getUsername(),
                execucao.getStatus(),
                execucao.getDataInicio(),
                execucao.getDataFim()
        );
    }

    @Transactional(readOnly = true)
    public List<ExecutionResponseDTO> listarTodas() {
        List<ExecutionModel> execucoes = executionRepository.findAllComDetalhes();

        return execucoes.stream()
                .map(execucao -> new ExecutionResponseDTO(
                        execucao.getId(),
                        execucao.getMaquina().getNome(),
                        execucao.getSubOrdem().getId(),
                        execucao.getOperador().getUsername(),
                        execucao.getStatus(),
                        execucao.getDataInicio(),
                        execucao.getDataFim()
                )).toList();
    }

    private boolean operacaoValidaParaExecucao(OrderModel ordemPrincipal, SubOrderModel subOrdem) {
        if (ordemPrincipal.getStatus().equals(StatusProducaoEnum.CANCELADO) || ordemPrincipal.getStatus().equals(StatusProducaoEnum.FINALIZADO)) {
            List<SubOrderModel> subOrdensDaOrdemPrincipal = subOrderRepository.findAllByOrdemPrincipal(ordemPrincipal);

            subOrdensDaOrdemPrincipal.forEach(s -> s.setStatus(ordemPrincipal.getStatus()));

            subOrderRepository.saveAll(subOrdensDaOrdemPrincipal);

            throw new ClosedOrderException();
        }
        else if (subOrdem.getStatus().equals(StatusProducaoEnum.CANCELADO) || subOrdem.getStatus().equals(StatusProducaoEnum.FINALIZADO)) {
            throw new ClosedSubOrderException();
        }

        return true;
    }
}
