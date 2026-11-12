package com.experiencefieldops.api.util;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;

import com.experiencefieldops.api.entities.InspectionItemSnapshot;
import com.experiencefieldops.api.enums.ResponseType;

class ResponseValueValidatorTest {

    private static InspectionItemSnapshot item(ResponseType type) {
        return InspectionItemSnapshot.builder().responseType(type).build();
    }

    private static void ok(ResponseType t, String v) {
        assertThatCode(() -> ResponseValueValidator.validate(item(t), v)).doesNotThrowAnyException();
    }

    private static void invalid(ResponseType t, String v) {
        assertThatThrownBy(() -> ResponseValueValidator.validate(item(t), v))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void date_aceitaSoIsoValido() {
        ok(ResponseType.DATE, "2026-10-15");
        invalid(ResponseType.DATE, "15/10/2026"); // máscara do app
        invalid(ResponseType.DATE, "2026-02-30"); // data inexistente (a regex antiga aceitava)
    }

    @Test
    void boolean_number_e_vazio() {
        ok(ResponseType.BOOLEAN, "TRUE");
        invalid(ResponseType.BOOLEAN, "sim");
        ok(ResponseType.NUMBER, "3.5");
        invalid(ResponseType.NUMBER, "3,5");
        ok(ResponseType.DATE, null);
        ok(ResponseType.TEXT_SHORT, "qualquer coisa");
    }
}
