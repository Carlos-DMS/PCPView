package com.univesp.PCPView.models;

import com.univesp.PCPView.models.enums.ExecutionStatus;
import jakarta.persistence.*;

import java.io.Serializable;
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

    public ExecutionModel(UserModel operador, MachineModel maquina, SubOrderModel etapaSubOrdem) {
        this.operador = operador;
        this.maquina = maquina;
        this.subOrdem = etapaSubOrdem;
        this.quantidadeFeitaNestaSessao = 0;
        this.dataInicio = LocalDateTime.now();
        this.dataFim = null;
        this.status = ExecutionStatus.RODANDO;
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
}