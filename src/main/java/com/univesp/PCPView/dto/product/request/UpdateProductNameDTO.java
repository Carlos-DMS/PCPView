package com.univesp.PCPView.dto.product.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record UpdateProductNameDTO (@NotNull
                                    @NotBlank
                                    String nome){
}
