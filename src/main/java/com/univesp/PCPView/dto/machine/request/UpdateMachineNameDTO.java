package com.univesp.PCPView.dto.machine.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record UpdateMachineNameDTO (@NotNull
                                    @NotBlank
                                    String nome){
}
