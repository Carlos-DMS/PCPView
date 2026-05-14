package com.univesp.PCPView.models;

import com.univesp.PCPView.exceptions.ExcessQuantityException;
import com.univesp.PCPView.models.enums.StatusProducaoEnum;
import jakarta.persistence.*;

import java.io.Serializable;
import java.time.LocalDateTime;

@Entity
@Table(name = "tb_ordens")
public class OrderModel implements Serializable {

    @Id
    @Column(nullable = false, unique = true)
    private String numeroOrdem;

    @Column(nullable = false)
    private Integer quantidadeTotal;

    private Integer quantidadeProduzida;

    @Enumerated(EnumType.STRING)
    private StatusProducaoEnum status;

    private LocalDateTime dataCriacao;

    @ManyToOne
    @JoinColumn(name = "product_id")
    private ProductModel produto;

    public OrderModel(String numeroOrdem, Integer quantidadeTotal) {
        this.numeroOrdem = numeroOrdem;
        this.quantidadeTotal = quantidadeTotal;
        this.quantidadeProduzida = 0;
        this.dataCriacao = LocalDateTime.now();
        this.status = StatusProducaoEnum.AGUARDANDO;
    }

    public OrderModel() {
    }

    public String getNumeroOrdem() {
        return numeroOrdem;
    }

    public void setNumeroOrdem(String numeroOrdem) {
        this.numeroOrdem = numeroOrdem;
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

    public void setQuantidadeProduzida(Integer quantidadeProduzida) {
        this.quantidadeProduzida = quantidadeProduzida;
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

    public LocalDateTime getDataCriacao() {
        return dataCriacao;
    }

    public ProductModel getProduto() {
        return produto;
    }


    public void setProduto(ProductModel produto) {
        this.produto = produto;
    }

}
