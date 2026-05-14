package com.univesp.PCPView.models;

import jakarta.persistence.*;
import java.io.Serializable;

@Entity
@Table(name = "tb_maquinas")
public class MachineModel implements Serializable {

    @Id
    private String id;

    @Column(nullable = false)
    private String nome;

    private Boolean operacional;

    public MachineModel() {
    }

    public MachineModel(String id, String nome) {
        this.id = id;
        this.nome = nome;
        this.operacional = true;
    }

    public String getId() {
        return id;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public Boolean getOperacional() {
        return operacional;
    }

    public void alternarStatusOperacional() {
        if (this.operacional == true) {
            this.operacional = false;
        }
        else {
            this.operacional = true;
        }
    }
}