package com.devprogen.application.project.record.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;

public record ProjectUpdateNameDTO(
        @NotEmpty(message = "Field must not be empty") @NotBlank
        Long idProject,
        @NotEmpty(message = "Field must not be empty") @NotBlank
        String name
) {
}
