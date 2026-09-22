package com.univesp.PCPView.services;

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
import java.util.UUID;

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

        if (ordemPrincipal.getStatus().equals(StatusProducaoEnum.AGUARDANDO)) {
            ordemPrincipal.setStatus(StatusProducaoEnum.EM_PROCESSAMENTO);

            orderRepository.save(ordemPrincipal);
        }

        ExecutionModel execucao = executionRepository.save(new ExecutionModel(operador, maquina, subOrdem));

        return new ExecutionResponseDTO(
                execucao.getId(),
                maquina.getId(),
                maquina.getNome(),
                subOrdem.getId(),
                operador.getUsername(),
                execucao.getStatus(),
                execucao.getDataInicio(),
                execucao.getDataFim()
        );
    }

    @Transactional
    public ExecutionResponseDTO finalizarExecucao(UUID idExecucao, Integer quantidadeProduzida) {
        UserModel usuarioLogado = authenticationService.extractUser();

        ExecutionModel execucao = executionRepository.findById(idExecucao).orElseThrow(NonExistentExecutionException::new);

        if (!execucao.getOperador().getId().equals(usuarioLogado.getId()) && !usuarioLogado.getRole().equals(RoleEnum.ADMIN)) {
            throw new UnauthorizedExecutionAccessException();
        }

        if (!execucao.getStatus().equals(ExecutionStatus.RODANDO)) {
            throw new ExecutionNotRunningException();
        }

        SubOrderModel subOrdem = subOrderRepository.findById(execucao.getSubOrdem().getId()).orElseThrow(NonExistentSubOrderException::new);

        OrderModel ordemPrincipal = orderRepository.findById(subOrdem.getOrdemPrincipal().getNumeroOrdem()).orElseThrow(DatabaseException::new);

        MachineModel maquina = machineRepository.findById(execucao.getMaquina().getId()).orElseThrow(NonExistentMachineException::new);

        execucao.setQuantidadeFeitaNestaSessao(quantidadeProduzida);
        execucao.inserirDataFim();
        execucao.setStatus(ExecutionStatus.FINALIZADA);

        subOrdem.adicionarQuantidadeProduzida(quantidadeProduzida);

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
                maquina.getId(),
                maquina.getNome(),
                subOrdem.getId(),
                usuarioLogado.getUsername(),
                execucao.getStatus(),
                execucao.getDataInicio(),
                execucao.getDataFim()
        );
    }

    @Transactional(readOnly = true)
    public List<ExecutionResponseDTO> listarTodas(String maquinaId, ExecutionStatus status, String ordemId) {

        List<ExecutionModel> execucoes = executionRepository.findComFiltros(maquinaId, status, ordemId);

        return execucoes.stream()
                .map(execucao -> new ExecutionResponseDTO(
                        execucao.getId(),
                        execucao.getMaquina().getId(),
                        execucao.getMaquina().getNome(),
                        execucao.getSubOrdem().getId(),
                        execucao.getOperador().getUsername(),
                        execucao.getStatus(),
                        execucao.getDataInicio(),
                        execucao.getDataFim()
                )).toList();
    }

    @Transactional
    public void cancelarExecucao(UUID idExecucao) {
        ExecutionModel execucao = executionRepository.findById(idExecucao)
                .orElseThrow(NonExistentExecutionException::new);

        if (!execucao.getStatus().equals(ExecutionStatus.RODANDO)) {
            throw new ExecutionAlreadyFinishedException();
        }

        UserModel usuarioLogado = authenticationService.extractUser();

        if (!usuarioLogado.getRole().equals(RoleEnum.ADMIN) && !execucao.getOperador().equals(usuarioLogado)) {
            throw new UnauthorizedExecutionAccessException();
        }

        executionRepository.delete(execucao);
    }

    private void operacaoValidaParaExecucao(OrderModel ordemPrincipal, SubOrderModel subOrdem) {
        if (ordemPrincipal.getStatus().equals(StatusProducaoEnum.CANCELADO) || ordemPrincipal.getStatus().equals(StatusProducaoEnum.FINALIZADO)) {
            List<SubOrderModel> subOrdensDaOrdemPrincipal = subOrderRepository.findAllByOrdemPrincipal(ordemPrincipal);

            subOrdensDaOrdemPrincipal.forEach(s -> s.setStatus(ordemPrincipal.getStatus()));

            subOrderRepository.saveAll(subOrdensDaOrdemPrincipal);

            throw new ClosedOrderException();
        }
        else if (subOrdem.getStatus().equals(StatusProducaoEnum.CANCELADO) || subOrdem.getStatus().equals(StatusProducaoEnum.FINALIZADO)) {
            throw new ClosedSubOrderException();
        }
    }
}
