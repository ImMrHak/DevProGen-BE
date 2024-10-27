package com.devprogen.application.generator.record.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import org.springframework.web.multipart.MultipartFile;

public record GenerateProjectDTO(
        @NotEmpty(message = "Field must not be empty") @NotBlank
        MultipartFile file,
        @NotEmpty(message = "Field must not be empty") @NotBlank
        String projectName,
        @NotEmpty(message = "Field must not be empty") @NotBlank
        Boolean isBEOnly
) {
}
