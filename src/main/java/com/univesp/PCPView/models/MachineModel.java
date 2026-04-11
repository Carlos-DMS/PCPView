package com.univesp.PCPView.models;

import com.univesp.PCPView.models.enums.MachineStatus;
import com.univesp.PCPView.models.enums.MachineType; // alterei aqui para um novo enum para manter as maquinas fixas
import jakarta.persistence.*;
import java.io.Serializable;
import java.util.Objects;

@Entity
@Table(name = "tb_machines")
public class MachineModel implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String nome;

    @Enumerated(EnumType.STRING) // Salva o nome da máquina no banco  mantendo os nomes das maquians fixas
    private MachineType tipo;

    @Column(unique = true)
    private String codigo;

    @Enumerated(EnumType.STRING)
    private MachineStatus status;

    // Construtor vazio
    public MachineModel() {
    }

    // Getters e Setters Manuais Eleminei o lombok
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public MachineType getTipo() {
        return tipo;
    }

    public void setTipo(MachineType tipo) {
        this.tipo = tipo;
    }

    public String getCodigo() {
        return codigo;
    }

    public void setCodigo(String codigo) {
        this.codigo = codigo;
    }

    public MachineStatus getStatus() {
        return status;
    }

    public void setStatus(MachineStatus status) {
        this.status = status;
    }

    // Equals e HashCode (Baseados apenas no ID para evitar erros de performance)
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MachineModel that = (MachineModel) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}