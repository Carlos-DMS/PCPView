package com.univesp.PCPView.dto.subOrder.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record SubsetRequestDTO(@NotNull
                               @NotBlank
                               String letra,

                               @NotNull
                               Integer quantidadeEtapas) {
}
