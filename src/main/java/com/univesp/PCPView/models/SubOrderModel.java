package com.univesp.PCPView.models;

import com.univesp.PCPView.exceptions.ExcessQuantityException;
import com.univesp.PCPView.models.enums.StatusProducaoEnum;
import jakarta.persistence.*;

import java.io.Serializable;
import java.time.LocalDateTime;

@Entity
@Table(name = "tb_sub_ordens")
public class SubOrderModel implements Serializable {

    @Id
    private String id;

    @ManyToOne
    @JoinColumn(name = "order_id", nullable = false)
    private OrderModel ordemPrincipal;

    @Column(nullable = false)
    private Integer quantidadeTotal;

    @Column(nullable = false)
    private Integer quantidadeProduzida;

    @Enumerated(EnumType.STRING)
    private StatusProducaoEnum status;

    private LocalDateTime inicioDoProcesso;

    public SubOrderModel(String id, OrderModel ordemPrincipal, Integer quantidadeTotal) {
        this.id = id;
        this.ordemPrincipal = ordemPrincipal;
        this.quantidadeTotal = quantidadeTotal;
        this.quantidadeProduzida = 0;
        this.status = StatusProducaoEnum.AGUARDANDO;
    }

    public SubOrderModel() {
    }

    public String getId() {
        return id;
    }

    public OrderModel getOrdemPrincipal() {
        return ordemPrincipal;
    }

    public Integer getQuantidadeTotal() {
        return quantidadeTotal;
    }

    public void setQuantidadeTotal(Integer quantidadeTotal) {
        this.quantidadeTotal = quantidadeTotal;
    }

    public Integer getQuantidadeProduzida() {
        return quantidadeProduzida;
    }

    public void adicionarQuantidadeProduzida(Integer quantidade) {
        if (quantidade > (this.quantidadeTotal - this.quantidadeProduzida)) {
            throw new ExcessQuantityException();
        }

        this.quantidadeProduzida += quantidade;
    }

    @Transient
    public Integer getQuantidadeRestante() {
        return this.quantidadeTotal - this.quantidadeProduzida;
    }

    public StatusProducaoEnum getStatus() {
        return status;
    }

    public void setStatus(StatusProducaoEnum status) {
        this.status = status;
    }

    public LocalDateTime getInicioDoProcesso() {
        return inicioDoProcesso;
    }

    public void iniciarProcesso(LocalDateTime inicioDoProcesso) {
        this.inicioDoProcesso = LocalDateTime.now();
        this.status = StatusProducaoEnum.EM_PROCESSAMENTO;
    }
}
