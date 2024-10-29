package com.devprogen.application.user.record.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;

public record ContactSelectUser(
        @NotEmpty(message = "Field must not be empty") @NotBlank
        String email,
        @NotEmpty(message = "Field must not be empty") @NotBlank
        String message
) {
}
