package com.univesp.PCPView.models.enums;

public enum MachineType {
    MANDRILHADORA_WOTAN("Mandrilhadora WOTAN-MHC1"),
    MANDRILHADORA_LAZZATI("Mandrilhadora LAZZATI-MHC2"),
    CENTRO_USINAGEM("Centro de Usinagem 1500 CUV-01");

    private final String descricao;

    MachineType(String descricao) {
        this.descricao = descricao;
    }

    public String getDescricao() {
        return descricao;
    }
}