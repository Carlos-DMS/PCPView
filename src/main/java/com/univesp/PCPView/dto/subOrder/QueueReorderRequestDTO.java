package com.univesp.PCPView.dto.subOrder;

import java.util.List;

public record QueueReorderRequestDTO(String maquinaIdealId,
                                     List<String> codigosEtapa) {
}
