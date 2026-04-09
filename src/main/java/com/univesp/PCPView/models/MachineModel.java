package com.univesp.PCPView.models;

import com.univesp.PCPView.models.enums.MachineStatus;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Entity
@Table(name = "tb_machines")
@Data
@NoArgsConstructor

@AllArgsConstructor
public class MachineModel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String nome;
    private String tipo;
    @Column(unique = true)
    private String codigo;

    @Enumerated(EnumType.STRING)
    private MachineStatus status;

}