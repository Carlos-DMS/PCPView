package com.univesp.PCPView.models.enums;

public enum StatusProducaoEnum {
    AGUARDANDO,      // OP criada, mas não iniciada
    EM_PROCESSAMENTO,// Peças estão na máquina/usinagem
    FINALIZADO,      // OP concluída com sucesso
    CANCELADO        // OP cancelada por algum problema
}
