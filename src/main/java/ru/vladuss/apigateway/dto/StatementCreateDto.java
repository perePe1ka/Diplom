package ru.vladuss.apigateway.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import ru.vladuss.apigateway.constant.StatementType;

import java.time.LocalDate;

public record StatementCreateDto(
        @NotBlank
        String lastName,
        @NotBlank
        String firstName,
        String middleName,
        LocalDate birthDate,
        @Email
        String email,
        String phone,
        String groupOrPosition,
        @NotNull
        StatementType type
) {}