package com.univesp.PCPView.models;

import com.univesp.PCPView.models.enums.ExecutionStatus;
import jakarta.persistence.*;

import java.io.Serializable;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "tb_execucoes")
public class ExecutionModel implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(columnDefinition = "UUID")
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "machine_id", nullable = false)
    private MachineModel maquina;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sub_order_id", nullable = false)
    private SubOrderModel subOrdem;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private UserModel operador;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ExecutionStatus status;

    @Column(nullable = false)
    private Integer quantidadeFeitaNestaSessao;

    private LocalDateTime dataInicio;

    private LocalDateTime dataFim;

    private LocalDateTime pausaInicio;

    private Long tempoPausadoSegundos;

    private LocalDateTime setupInicio;

    private LocalDateTime setupFim;

    private Long tempoSetupPausadoSegundos;

    private Long tempoSetupSegundos;

    public ExecutionModel(UserModel operador, MachineModel maquina, SubOrderModel etapaSubOrdem, Boolean setupPrimeiraPeca) {
        this.operador = operador;
        this.maquina = maquina;
        this.subOrdem = etapaSubOrdem;
        this.quantidadeFeitaNestaSessao = 0;
        this.dataInicio = LocalDateTime.now();
        this.dataFim = null;
        this.status = ExecutionStatus.RODANDO;
        this.pausaInicio = null;
        this.tempoPausadoSegundos = 0L;
        this.setupInicio = Boolean.TRUE.equals(setupPrimeiraPeca) ? this.dataInicio : null;
        this.setupFim = null;
        this.tempoSetupPausadoSegundos = 0L;
        this.tempoSetupSegundos = 0L;
    }

    public ExecutionModel() {
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public MachineModel getMaquina() {
        return maquina;
    }

    public void setMaquina(MachineModel maquina) {
        this.maquina = maquina;
    }

    public SubOrderModel getSubOrdem() {
        return subOrdem;
    }

    public void setSubOrdem(SubOrderModel subOrdem) {
        this.subOrdem = subOrdem;
    }

    public UserModel getOperador() {
        return operador;
    }

    public ExecutionStatus getStatus() {
        return status;
    }

    public void setStatus(ExecutionStatus status) {
        this.status = status;
    }

    public Integer getQuantidadeFeitaNestaSessao() {
        return quantidadeFeitaNestaSessao;
    }

    public void setQuantidadeFeitaNestaSessao(Integer quantidadeFeitaNestaSessao) {
        this.quantidadeFeitaNestaSessao = quantidadeFeitaNestaSessao;
    }

    public LocalDateTime getDataInicio() {
        return dataInicio;
    }

    public LocalDateTime getDataFim() {
        return dataFim;
    }

    public void inserirDataFim() {
        this.dataFim = LocalDateTime.now();
    }

    public Boolean getSetupPrimeiraPeca() {
        return this.setupInicio != null;
    }

    public Long getTempoSetupSegundos() {
        if (setupInicio == null) {
            return 0L;
        }

        if (setupFim != null) {
            return tempoSetupSegundos == null ? 0L : tempoSetupSegundos;
        }

        long segundos = Duration.between(setupInicio, LocalDateTime.now()).getSeconds() - getTempoSetupPausadoSegundos();
        return Math.max(segundos, 0L);
    }

    public Long getTempoPausadoSegundos() {
        return tempoPausadoSegundos == null ? 0L : tempoPausadoSegundos;
    }

    public Long getTempoSetupPausadoSegundos() {
        return tempoSetupPausadoSegundos == null ? 0L : tempoSetupPausadoSegundos;
    }

    public Long getTempoProdutivoSegundos() {
        LocalDateTime fim = dataFim == null ? LocalDateTime.now() : dataFim;
        long segundos = Duration.between(dataInicio, fim).getSeconds() - getTempoPausadoSegundos();
        return Math.max(segundos, 0L);
    }

    public Long getTempoRestanteLoteSegundos() {
        return Math.max(getTempoProdutivoSegundos() - getTempoSetupSegundos(), 0L);
    }

    public Long getTempoMedioPorPecaSegundos() {
        if (quantidadeFeitaNestaSessao == null || quantidadeFeitaNestaSessao <= 0) {
            return 0L;
        }

        return getTempoProdutivoSegundos() / quantidadeFeitaNestaSessao;
    }

    public Long getTempoMedioRestanteSegundos() {
        int quantidadeRestante = getSetupPrimeiraPeca()
                ? Math.max((quantidadeFeitaNestaSessao == null ? 0 : quantidadeFeitaNestaSessao) - 1, 0)
                : (quantidadeFeitaNestaSessao == null ? 0 : quantidadeFeitaNestaSessao);

        if (quantidadeRestante <= 0) {
            return 0L;
        }

        return getTempoRestanteLoteSegundos() / quantidadeRestante;
    }

    public void pausar() {
        if (this.status.equals(ExecutionStatus.RODANDO)) {
            this.pausaInicio = LocalDateTime.now();
            this.status = ExecutionStatus.PAUSADA_POR_QUEBRA;
        }
    }

    public void retomar() {
        if (this.status.equals(ExecutionStatus.PAUSADA_POR_QUEBRA) && this.pausaInicio != null) {
            long segundosPausados = Duration.between(this.pausaInicio, LocalDateTime.now()).getSeconds();
            this.tempoPausadoSegundos = getTempoPausadoSegundos() + segundosPausados;

            if (this.setupInicio != null && this.setupFim == null) {
                this.tempoSetupPausadoSegundos = getTempoSetupPausadoSegundos() + segundosPausados;
            }

            this.pausaInicio = null;
            this.status = ExecutionStatus.RODANDO;
        }
    }

    public void finalizarSetup() {
        if (this.setupInicio != null && this.setupFim == null) {
            LocalDateTime fimSetup = LocalDateTime.now();
            long segundos = Duration.between(this.setupInicio, fimSetup).getSeconds() - getTempoSetupPausadoSegundos();
            this.tempoSetupSegundos = Math.max(segundos, 0L);
            this.setupFim = fimSetup;
        }
    }
}
