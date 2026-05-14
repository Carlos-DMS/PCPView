package com.univesp.PCPView.dto.order;

import com.univesp.PCPView.dto.subOrder.SubsetRequestDTO;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public record OrderRequestDTO(@NotNull
                              @NotBlank
                              String numeroOrdem,

                              @NotNull
                              Integer quantidadeTotal,

                              List<SubsetRequestDTO> subconjuntos) {
}
