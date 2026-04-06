package com.univesp.PCPView.dto.product.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record ProductRequestDTO(@NotNull
                                @NotBlank
                                String id,

                                @NotNull
                                @NotBlank
                                String sku,

                                @NotNull
                                @NotBlank
                                String nome) {
}
