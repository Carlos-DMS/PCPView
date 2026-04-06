package com.univesp.PCPView.dto.authentication.request;

import com.univesp.PCPView.models.enums.RoleEnum;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record RegisterRequestDTO(@NotBlank
                                 String userName,

                                 @NotBlank
                                 @Email
                                 String email,

                                 @NotBlank
                                 String password,

                                 @NotNull
                                 RoleEnum role) {
}
