package com.experiencefieldops.api.util;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;

import com.experiencefieldops.api.entities.InspectionItemSnapshot;
import com.experiencefieldops.api.enums.ConformityValue;

/**
 * Formato do value por responseType (RN-036 / B-09), compartilhado pelos dois fluxos de resposta.
 * Lança IllegalArgumentException (→ 400). Valor vazio não é validado aqui.
 */
public final class ResponseValueValidator {

    private ResponseValueValidator() { }

    public static void validate(InspectionItemSnapshot item, String value) {
        if (value == null || value.isBlank()) return;

        switch (item.getResponseType()) {
            case NUMBER -> {
                try {
                    Double.parseDouble(value);
                } catch (NumberFormatException e) {
                    throw new IllegalArgumentException(
                            "Valor inválido para tipo NUMBER: '" + value + "'. Esperado: número decimal com ponto (ex.: 3.5).");
                }
            }
            case BOOLEAN -> {
                if (!value.equalsIgnoreCase("true") && !value.equalsIgnoreCase("false")) {
                    throw new IllegalArgumentException(
                            "Valor inválido para tipo BOOLEAN: '" + value + "'. Esperado: 'true' ou 'false'.");
                }
            }
            case CONFORMITY -> ConformityValue.validate(value);
            case DATE -> {
                try {
                    LocalDate.parse(value); // ISO-8601 estrito: rejeita dd/MM/yyyy e datas inexistentes
                } catch (DateTimeParseException e) {
                    throw new IllegalArgumentException(
                            "Valor inválido para tipo DATE: '" + value + "'. Esperado: formato ISO-8601 (yyyy-MM-dd).");
                }
            }
            case SINGLE_CHOICE -> {
                if (item.getOptionsJson() == null || item.getOptionsJson().isBlank()) {
                    throw new IllegalArgumentException(
                            "Item do tipo SINGLE_CHOICE não possui opções configuradas.");
                }
                // Valida apenas que o valor foi informado — validação das opções do JSON
                // pode ser expandida futuramente sem quebrar o contrato
            }
            // TEXT_SHORT e TEXT_LONG aceitam qualquer string — sem validação adicional
            default -> { }
        }
    }
}
