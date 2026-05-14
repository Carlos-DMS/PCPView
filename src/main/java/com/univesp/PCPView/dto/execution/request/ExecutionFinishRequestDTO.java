package com.univesp.PCPView.dto.execution.request;

import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record ExecutionFinishRequestDTO(@NotNull
                                        UUID idExecucao,

                                        @NotNull
                                        Integer quantidadeProduzida) {
}
