package com.experiencefieldops.api.enums;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;

class ConformityValueTest {

    @Test
    void validate_aceitaOsQuatroValoresIgnorandoCaixa() {
        for (String v : new String[] {"CONFORMING", "attention", "Non_Conforming", "NOT_VERIFIED"}) {
            assertThatCode(() -> ConformityValue.validate(v)).doesNotThrowAnyException();
        }
    }

    @Test
    void validate_rejeitaRotuloPtEValorAntigo() {
        assertThatThrownBy(() -> ConformityValue.validate("Conforme"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("ATTENTION");
        assertThatThrownBy(() -> ConformityValue.validate("NOT_APPLICABLE"))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
