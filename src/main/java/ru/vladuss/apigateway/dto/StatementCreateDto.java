package ru.vladuss.apigateway.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import ru.vladuss.apigateway.constant.StatementType;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDate;
import java.util.StringJoiner;

public record StatementCreateDto(
        @NotBlank String lastName,
        @NotBlank String firstName,
        String middleName,
        LocalDate birthDate,
        @Email String email,
        String phone,
        String groupOrPosition,
        @NotNull StatementType type
) implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Override
    public String toString() {
        return new StringJoiner(", ", StatementCreateDto.class.getSimpleName() + "[", "]")
                .add("lastName='" + lastName + "'")
                .add("firstName='" + firstName + "'")
                .add("middleName='" + middleName + "'")
                .add("birthDate=" + birthDate)
                .add("email='" + email + "'")
                .add("phone='" + phone + "'")
                .add("groupOrPosition='" + groupOrPosition + "'")
                .add("type=" + type)
                .toString();
    }
}
