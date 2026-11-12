package com.experiencefieldops.api.entities;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

import com.experiencefieldops.api.enums.NonConformityStatus;
import com.experiencefieldops.api.enums.Severity;

class NonConformityEvidenceRuleTest {

    private static NonConformity nc(Severity sev, NonConformityStatus st, boolean evidence) {
        NonConformity nc = NonConformity.builder().severity(sev).status(st).evidenceAttached(evidence).build();
        nc.applyEvidenceRule();
        return nc;
    }

    @Test
    void criticaSemEvidenciaFicaPendente_comEvidenciaOuNaoCriticaVoltaAberta() {
        assertThat(nc(Severity.CRITICAL, NonConformityStatus.OPEN, false).getStatus())
                .isEqualTo(NonConformityStatus.PENDING_EVIDENCE);
        assertThat(nc(Severity.CRITICAL, NonConformityStatus.PENDING_EVIDENCE, true).getStatus())
                .isEqualTo(NonConformityStatus.OPEN);
        assertThat(nc(Severity.HIGH, NonConformityStatus.PENDING_EVIDENCE, false).getStatus())
                .isEqualTo(NonConformityStatus.OPEN);
        assertThat(nc(Severity.MEDIUM, NonConformityStatus.OPEN, false).getStatus())
                .isEqualTo(NonConformityStatus.OPEN);
    }

    @Test
    void naoMexeEmResolvidaOuDescartada() {
        assertThat(nc(Severity.CRITICAL, NonConformityStatus.RESOLVED, false).getStatus())
                .isEqualTo(NonConformityStatus.RESOLVED);
        assertThat(nc(Severity.CRITICAL, NonConformityStatus.DISMISSED, false).getStatus())
                .isEqualTo(NonConformityStatus.DISMISSED);
    }
}
