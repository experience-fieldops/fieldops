package com.experiencefieldops.api.entities;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import java.time.LocalDateTime;

import org.junit.jupiter.api.Test;

class InspectionCodeTest {

    @Test
    void getCode_usaAnoDeCriacaoEIdComTresDigitos() {
        Inspection i = Inspection.builder().id(7L)
                .createdAt(LocalDateTime.of(2026, 3, 1, 10, 0))
                .scheduledDate(LocalDate.of(2027, 1, 1)).build();
        assertThat(i.getCode()).isEqualTo("INSP-2026-007");

        i.setId(12345L);
        assertThat(i.getCode()).isEqualTo("INSP-2026-12345");
    }

    @Test
    void getCode_semCreatedAtUsaAnoAgendadoESemIdRetornaNull() {
        Inspection i = Inspection.builder().id(1L).scheduledDate(LocalDate.of(2026, 10, 15)).build();
        assertThat(i.getCode()).isEqualTo("INSP-2026-001");

        i.setId(null);
        assertThat(i.getCode()).isNull();
    }
}
