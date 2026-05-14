package com.univesp.PCPView.dto.execution.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record ExecutionStartRequestDTO(@NotNull
                                       @NotBlank
                                       String idMaquina,

                                       @NotNull
                                       @NotBlank
                                       String idEtapaSubOrdem) {
}
