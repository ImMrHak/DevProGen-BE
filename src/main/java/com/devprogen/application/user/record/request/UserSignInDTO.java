package com.devprogen.application.user.record.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;

public record UserSignInDTO(
        @NotEmpty(message = "Field must not be empty") @NotBlank
        String usernameOrEmail,
        @NotEmpty(message = "Field must not be empty") @NotBlank
        String password
) {
}
