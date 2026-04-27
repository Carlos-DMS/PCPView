package com.univesp.PCPView.dto.machine.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record MachineRequestDTO(@NotNull
                                @NotBlank
                                String id,

                                @NotNull
                                @NotBlank
                                String nome) {
}
