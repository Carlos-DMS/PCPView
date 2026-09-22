package com.univesp.PCPView.dto.authentication.response;

import com.univesp.PCPView.models.enums.RoleEnum;

import java.util.UUID;

public record UserResponseDTO(UUID id,
                              String login,
                              RoleEnum role,
                              Boolean ativo) {

}

