package com.experiencefieldops.api.enums;

import java.util.Arrays;
import java.util.Locale;

/**
 * Valores possíveis para itens do tipo CONFORMITY (B-01 / D-07).
 * "Não aplicável" não é um valor: usa-se a flag notApplicable da resposta.
 */
public enum ConformityValue {
    CONFORMING,
    ATTENTION,
    NON_CONFORMING,
    NOT_VERIFIED;

    /** Lança IllegalArgumentException (→ 400) se o valor não for do enum (case-insensitive). */
    public static void validate(String value) {
        try {
            valueOf(value.toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException(
                    "Valor inválido para tipo CONFORMITY: '" + value + "'. Esperado: "
                            + Arrays.toString(values()) + ".");
        }
    }
}
