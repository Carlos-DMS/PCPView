package com.univesp.PCPView.dto.authentication.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record RegisterRequestDTO(@NotBlank
                                 String userName,

                                 @NotBlank
                                 @Email
                                 String email,

                                 @NotBlank
                                 String password){
}
