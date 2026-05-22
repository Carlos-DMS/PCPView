package com.univesp.PCPView.services;

import com.univesp.PCPView.dto.order.OrderRequestDTO;
import com.univesp.PCPView.dto.order.OrderResponseDTO;
import com.univesp.PCPView.dto.subOrder.QueueMachineRequestDTO;
import com.univesp.PCPView.dto.subOrder.QueueReorderRequestDTO;
import com.univesp.PCPView.dto.subOrder.SubOrderResponseDTO;
import com.univesp.PCPView.dto.subOrder.SubsetRequestDTO;
import com.univesp.PCPView.exceptions.DatabaseException;
import com.univesp.PCPView.models.ExecutionModel;
import com.univesp.PCPView.models.MachineModel;
import com.univesp.PCPView.models.OrderModel;
import com.univesp.PCPView.models.ProductModel;
import com.univesp.PCPView.models.SubOrderModel;
import com.univesp.PCPView.models.enums.ExecutionStatus;
import com.univesp.PCPView.repository.ExecutionRepository;
import com.univesp.PCPView.repository.MachineRepository;
import com.univesp.PCPView.repository.OrderRepository;
import com.univesp.PCPView.repository.ProductRepository;
import com.univesp.PCPView.repository.SubOrderRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Service
public class OrderService {
    private final OrderRepository orderRepository;
    private final SubOrderRepository subOrderRepository;
    private final ExecutionRepository executionRepository;
    private final MachineRepository machineRepository;
    private final ProductRepository productRepository;

    public OrderService(OrderRepository orderRepository, SubOrderRepository subOrderRepository, ExecutionRepository executionRepository, MachineRepository machineRepository, ProductRepository productRepository) {
        this.orderRepository = orderRepository;
        this.subOrderRepository = subOrderRepository;
        this.executionRepository = executionRepository;
        this.machineRepository = machineRepository;
        this.productRepository = productRepository;
    }

    @Transactional
    public OrderResponseDTO criarOrdemComEtapas(OrderRequestDTO body) {
        OrderModel ordemPrincipal = new OrderModel(body.numeroOrdem(), body.quantidadeTotal());
        vincularProdutoInformado(body, ordemPrincipal);
        ordemPrincipal = orderRepository.save(ordemPrincipal);

        List<SubOrderModel> subOrdens = getSubOrdens(body, ordemPrincipal);

        subOrderRepository.saveAll(subOrdens);

        return new OrderResponseDTO(ordemPrincipal.getNumeroOrdem(),
                produtoNome(ordemPrincipal),
                produtoSku(ordemPrincipal),
                ordemPrincipal.getQuantidadeTotal(),
                calcularQuantidadeProduzida(ordemPrincipal, subOrdens),
                ordemPrincipal.getStatus(),
                subOrdens.stream().map(this::converterSubOrdemParaResponseDTO).toList(),
                null,
                null,
                0L,
                0L,
                0L,
                0L,
                0L
        );
    }

    @Transactional
    public List<OrderResponseDTO> listarTodas() {
        return orderRepository.findAll().stream()
                .map(ordem -> {
                    List<SubOrderModel> subOrdens = subOrderRepository.findAllByOrdemPrincipal(ordem);
                    OrdemTempoResumo resumo = calcularResumoTempos(ordem, subOrdens);

                    return new OrderResponseDTO(
                            ordem.getNumeroOrdem(),
                            produtoNome(ordem),
                            produtoSku(ordem),
                            ordem.getQuantidadeTotal(),
                            calcularQuantidadeProduzida(ordem, subOrdens),
                            ordem.getStatus(),
                            subOrdens.stream()
                                .map(this::converterSubOrdemParaResponseDTO)
                                .toList(),
                            resumo.dataInicio(),
                            resumo.dataFim(),
                            resumo.tempoProdutivoSegundos(),
                            resumo.tempoSetupSegundos(),
                            resumo.tempoRestanteLoteSegundos(),
                            resumo.tempoMedioPorPecaSegundos(),
                            resumo.tempoMedioRestanteSegundos());
                })
                .toList();
    }

    @Transactional
    public void deletarOrdem(String numeroOrdem) {
        OrderModel ordem = orderRepository.findById(numeroOrdem).orElseThrow(DatabaseException::new);
        subOrderRepository.deleteAll(subOrderRepository.findAllByOrdemPrincipal(ordem));
        orderRepository.delete(ordem);
    }

    @Transactional
    public OrderResponseDTO alterarMaquinaIdealDaSubOrdem(String codigoEtapa, QueueMachineRequestDTO body) {
        SubOrderModel subOrdem = subOrderRepository.findById(codigoEtapa).orElseThrow(DatabaseException::new);
        MachineModel maquinaIdeal = machineRepository.findById(body.maquinaIdealId()).orElseThrow(DatabaseException::new);

        subOrdem.setMaquinaIdeal(maquinaIdeal);
        subOrdem.setPosicaoFila(proximaPosicaoFila(maquinaIdeal));
        subOrderRepository.save(subOrdem);

        return buscarOrdemResponse(subOrdem.getOrdemPrincipal());
    }

    @Transactional
    public OrderResponseDTO removerSubOrdemDaFila(String codigoEtapa) {
        SubOrderModel subOrdem = subOrderRepository.findById(codigoEtapa).orElseThrow(DatabaseException::new);

        subOrdem.setMaquinaIdeal(null);
        subOrdem.setPosicaoFila(null);
        subOrderRepository.save(subOrdem);

        return buscarOrdemResponse(subOrdem.getOrdemPrincipal());
    }

    @Transactional
    public void reordenarFila(QueueReorderRequestDTO body) {
        MachineModel maquinaIdeal = machineRepository.findById(body.maquinaIdealId()).orElseThrow(DatabaseException::new);

        for (int i = 0; i < body.codigosEtapa().size(); i++) {
            SubOrderModel subOrdem = subOrderRepository.findById(body.codigosEtapa().get(i)).orElseThrow(DatabaseException::new);

            subOrdem.setMaquinaIdeal(maquinaIdeal);
            subOrdem.setPosicaoFila(i + 1);
            subOrderRepository.save(subOrdem);
        }
    }

    private Integer calcularQuantidadeProduzida(OrderModel ordem, List<SubOrderModel> subOrdens) {
        return subOrdens.stream()
                .map(SubOrderModel::getQuantidadeProduzida)
                .max(Integer::compareTo)
                .orElse(ordem.getQuantidadeProduzida());
    }

    private OrdemTempoResumo calcularResumoTempos(OrderModel ordem, List<SubOrderModel> subOrdens) {
        List<ExecutionModel> execucoes = subOrdens.stream()
                .flatMap(subOrdem -> executionRepository.findAllBySubOrdem(subOrdem).stream())
                .filter(execucao -> !execucao.getStatus().equals(ExecutionStatus.CANCELADA_MANUTENCAO))
                .toList();

        if (execucoes.isEmpty()) {
            return new OrdemTempoResumo(null, null, 0L, 0L, 0L, 0L, 0L);
        }

        LocalDateTime dataInicio = execucoes.stream()
                .map(ExecutionModel::getDataInicio)
                .min(Comparator.naturalOrder())
                .orElse(null);

        LocalDateTime dataFim = execucoes.stream()
                .map(ExecutionModel::getDataFim)
                .filter(data -> data != null)
                .max(Comparator.naturalOrder())
                .orElse(null);

        long tempoProdutivo = execucoes.stream().mapToLong(ExecutionModel::getTempoProdutivoSegundos).sum();
        long tempoSetup = execucoes.stream().mapToLong(ExecutionModel::getTempoSetupSegundos).sum();
        long tempoRestante = Math.max(tempoProdutivo - tempoSetup, 0L);
        int quantidadeProduzida = calcularQuantidadeProduzida(ordem, subOrdens);
        boolean teveSetup = tempoSetup > 0;
        int quantidadeRestante = teveSetup ? Math.max(quantidadeProduzida - 1, 0) : quantidadeProduzida;

        long tempoMedioPorPeca = quantidadeProduzida > 0 ? tempoProdutivo / quantidadeProduzida : 0L;
        long tempoMedioRestante = quantidadeRestante > 0 ? tempoRestante / quantidadeRestante : 0L;

        return new OrdemTempoResumo(dataInicio, dataFim, tempoProdutivo, tempoSetup, tempoRestante, tempoMedioPorPeca, tempoMedioRestante);
    }

    private record OrdemTempoResumo(LocalDateTime dataInicio,
                                    LocalDateTime dataFim,
                                    Long tempoProdutivoSegundos,
                                    Long tempoSetupSegundos,
                                    Long tempoRestanteLoteSegundos,
                                    Long tempoMedioPorPecaSegundos,
                                    Long tempoMedioRestanteSegundos) {
    }

    private List<SubOrderModel> getSubOrdens(OrderRequestDTO body, OrderModel ordemPrincipal) {
        List<SubOrderModel> subOrdens = new ArrayList<>();
        MachineModel maquinaIdeal = body.maquinaIdealId() == null || body.maquinaIdealId().isBlank()
                ? null
                : machineRepository.findById(body.maquinaIdealId()).orElseThrow(DatabaseException::new);
        int posicaoInicial = maquinaIdeal == null ? 0 : proximaPosicaoFila(maquinaIdeal);

        for (SubsetRequestDTO subconjunto : body.subconjuntos()) {
            for (int i = 1; i <= subconjunto.quantidadeEtapas(); i++) {
                String numeroEtapaFormatado = String.format("%02d", i);

                String codigoDaEtapa = ordemPrincipal.getNumeroOrdem() + "-" + subconjunto.letra() + "-" + numeroEtapaFormatado;

                SubOrderModel subOrdem = new SubOrderModel(codigoDaEtapa, ordemPrincipal, body.quantidadeTotal());
                if (maquinaIdeal != null) {
                    subOrdem.setMaquinaIdeal(maquinaIdeal);
                    subOrdem.setPosicaoFila(posicaoInicial++);
                }

                subOrdens.add(subOrdem);
            }
        }
        return subOrdens;
    }

    private OrderResponseDTO buscarOrdemResponse(OrderModel ordem) {
        List<SubOrderModel> subOrdens = subOrderRepository.findAllByOrdemPrincipal(ordem);
        OrdemTempoResumo resumo = calcularResumoTempos(ordem, subOrdens);

        return new OrderResponseDTO(
                ordem.getNumeroOrdem(),
                produtoNome(ordem),
                produtoSku(ordem),
                ordem.getQuantidadeTotal(),
                calcularQuantidadeProduzida(ordem, subOrdens),
                ordem.getStatus(),
                subOrdens.stream().map(this::converterSubOrdemParaResponseDTO).toList(),
                resumo.dataInicio(),
                resumo.dataFim(),
                resumo.tempoProdutivoSegundos(),
                resumo.tempoSetupSegundos(),
                resumo.tempoRestanteLoteSegundos(),
                resumo.tempoMedioPorPecaSegundos(),
                resumo.tempoMedioRestanteSegundos());
    }

    private void vincularProdutoInformado(OrderRequestDTO body, OrderModel ordemPrincipal) {
        if (body.produtoNome() == null || body.produtoNome().isBlank()) {
            return;
        }

        String codigoProduto = body.numeroOrdem().trim();
        ProductModel produto = productRepository.findById(codigoProduto)
                .map(produtoExistente -> {
                    produtoExistente.setNome(body.produtoNome().trim());
                    return produtoExistente;
                })
                .orElseGet(() -> new ProductModel(codigoProduto, codigoProduto, body.produtoNome().trim()));

        ordemPrincipal.setProduto(productRepository.save(produto));
    }

    private String produtoNome(OrderModel ordem) {
        return ordem.getProduto() == null ? null : ordem.getProduto().getNome();
    }

    private String produtoSku(OrderModel ordem) {
        return ordem.getProduto() == null ? null : ordem.getProduto().getSku();
    }

    private SubOrderResponseDTO converterSubOrdemParaResponseDTO(SubOrderModel subOrdem) {
        MachineModel maquinaIdeal = subOrdem.getMaquinaIdeal();

        return new SubOrderResponseDTO(
                subOrdem.getId(),
                subOrdem.getQuantidadeTotal(),
                subOrdem.getQuantidadeProduzida(),
                maquinaIdeal == null ? null : maquinaIdeal.getId(),
                maquinaIdeal == null ? null : maquinaIdeal.getNome(),
                subOrdem.getPosicaoFila());
    }

    private Integer proximaPosicaoFila(MachineModel maquinaIdeal) {
        return subOrderRepository.findAllByMaquinaIdealOrderByPosicaoFilaAsc(maquinaIdeal).stream()
                .map(SubOrderModel::getPosicaoFila)
                .filter(posicao -> posicao != null)
                .max(Integer::compareTo)
                .orElse(0) + 1;
    }
}
