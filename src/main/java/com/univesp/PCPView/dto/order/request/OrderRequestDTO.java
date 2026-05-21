package com.univesp.PCPView.dto.order.request;

import com.univesp.PCPView.dto.subOrder.request.SubsetRequestDTO;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public record OrderRequestDTO(@NotNull
                              @NotBlank
                              String numeroOrdem,

                              @NotNull
                              @Min(value = 0, message = "A quantidade não pode ser negativa.")
                              Integer quantidadeTotal,

                              List<SubsetRequestDTO> subconjuntos) {
}
