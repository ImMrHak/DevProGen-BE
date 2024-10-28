package com.devprogen.application.user.record.response;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;

public record UserInformationDTO (
        @NotEmpty(message = "Field must not be empty") @NotBlank
        String firstName,
        @NotEmpty(message = "Field must not be empty") @NotBlank
        String lastName,
        @NotEmpty(message = "Field must not be empty") @NotBlank @Email
        String email
){
}
