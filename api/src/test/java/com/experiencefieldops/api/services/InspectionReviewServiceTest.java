package com.experiencefieldops.api.services;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.experiencefieldops.api.dto.ApproveRequest;
import com.experiencefieldops.api.dto.RejectRequest;
import com.experiencefieldops.api.dto.ReviewRequest;
import com.experiencefieldops.api.dto.ReviewResponse;
import com.experiencefieldops.api.entities.Inspection;
import com.experiencefieldops.api.entities.InspectionReview;
import com.experiencefieldops.api.entities.User;
import com.experiencefieldops.api.enums.AuditAction;
import com.experiencefieldops.api.enums.InspectionStatus;
import com.experiencefieldops.api.enums.ReviewDecision;
import com.experiencefieldops.api.enums.UserRole;
import com.experiencefieldops.api.exception.ConflictException;
import com.experiencefieldops.api.exception.ForbiddenException;
import com.experiencefieldops.api.repositories.InspectionRepository;
import com.experiencefieldops.api.repositories.InspectionReviewRepository;
import com.experiencefieldops.api.repositories.UserRepository;
import com.experiencefieldops.api.services.impl.InspectionReviewServiceImpl;

@ExtendWith(MockitoExtension.class)
class InspectionReviewServiceTest {

    @Mock
    private InspectionReviewRepository reviewRepository;
    @Mock
    private InspectionRepository inspectionRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private AuditLogService auditLogService;

    @InjectMocks
    private InspectionReviewServiceImpl service;

    private User supervisor(Long id) {
        return User.builder().id(id).name("Maria Supervisora")
                .email("sup@fieldops.com").role(UserRole.SUPERVISOR).active(true).build();
    }

    private User tecnico(Long id) {
        return User.builder().id(id).name("Carlos").email("tec@fieldops.com")
                .role(UserRole.TECNICO).active(true).build();
    }

    private Inspection inspection(Long id, InspectionStatus status) {
        Inspection i = new Inspection();
        i.setId(id);
        i.setStatus(status);
        return i;
    }

    // ── startReview ───────────────────────────────────────────────────────────

    @Test
    void startReview_deveIniciarQuandoInspecaoCompleted() {
        Inspection insp = inspection(1L, InspectionStatus.COMPLETED);
        when(inspectionRepository.findById(1L)).thenReturn(Optional.of(insp));
        when(userRepository.findById(10L)).thenReturn(Optional.of(supervisor(10L)));
        when(reviewRepository.existsByInspectionIdAndDecision(1L, ReviewDecision.PENDING))
                .thenReturn(false);
        when(reviewRepository.findTopByInspectionIdOrderByRoundNumberDesc(1L))
                .thenReturn(Optional.empty());
        when(reviewRepository.save(any(InspectionReview.class)))
                .thenAnswer(inv -> { InspectionReview r = inv.getArgument(0); r.setId(100L); return r; });

        ReviewRequest req = new ReviewRequest();
        req.setComment("Iniciando revisão");

        ReviewResponse response = service.startReview(1L, req, 10L);

        assertThat(response).isNotNull();
        assertThat(insp.getStatus()).isEqualTo(InspectionStatus.UNDER_REVIEW);
        verify(auditLogService).log(eq(10L), anyString(), eq(AuditAction.STATUS_CHANGE),
                eq("Inspection"), eq(1L), anyString());
    }

    @Test
    void startReview_deveFalharSeInspecaoNaoCompleted() {
        Inspection insp = inspection(1L, InspectionStatus.IN_PROGRESS);
        when(inspectionRepository.findById(1L)).thenReturn(Optional.of(insp));
        when(userRepository.findById(10L)).thenReturn(Optional.of(supervisor(10L)));

        assertThatThrownBy(() -> service.startReview(1L, new ReviewRequest(), 10L))
                .isInstanceOf(ConflictException.class);

        verify(reviewRepository, never()).save(any());
    }

    @Test
    void startReview_deveFalharSeRevisorNaoForSupervisor() {
        Inspection insp = inspection(1L, InspectionStatus.COMPLETED);
        when(inspectionRepository.findById(1L)).thenReturn(Optional.of(insp));
        when(userRepository.findById(20L)).thenReturn(Optional.of(tecnico(20L)));

        assertThatThrownBy(() -> service.startReview(1L, new ReviewRequest(), 20L))
                .isInstanceOf(ForbiddenException.class);
    }

    @Test
    void startReview_deveFalharSeJaExisteRevisaoPendente() {
        Inspection insp = inspection(1L, InspectionStatus.COMPLETED);
        when(inspectionRepository.findById(1L)).thenReturn(Optional.of(insp));
        when(userRepository.findById(10L)).thenReturn(Optional.of(supervisor(10L)));
        when(reviewRepository.existsByInspectionIdAndDecision(1L, ReviewDecision.PENDING))
                .thenReturn(true);

        assertThatThrownBy(() -> service.startReview(1L, new ReviewRequest(), 10L))
                .isInstanceOf(ConflictException.class);
    }

    // ── approve ───────────────────────────────────────────────────────────────

    @Test
    void approve_deveAprovarRevisaoPendente() {
        User sup = supervisor(10L);
        Inspection insp = inspection(1L, InspectionStatus.UNDER_REVIEW);
        InspectionReview review = InspectionReview.builder()
                .id(100L).inspection(insp).reviewer(sup)
                .decision(ReviewDecision.PENDING).roundNumber(1).build();

        when(reviewRepository.findById(100L)).thenReturn(Optional.of(review));
        when(userRepository.findById(10L)).thenReturn(Optional.of(sup));
        when(reviewRepository.save(any(InspectionReview.class)))
                .thenAnswer(inv -> inv.getArgument(0));

        ApproveRequest req = new ApproveRequest();
        req.setComment("Tudo certo");

        ReviewResponse response = service.approve(100L, req, 10L);

        assertThat(response).isNotNull();
        assertThat(review.getDecision()).isEqualTo(ReviewDecision.APPROVED);
        assertThat(insp.getStatus()).isEqualTo(InspectionStatus.APPROVED);
        verify(auditLogService).log(eq(10L), anyString(), eq(AuditAction.APPROVE),
                eq("Inspection"), eq(1L), anyString());
    }

    @Test
    void approve_deveFalharSeRevisaoNaoPendente() {
        User sup = supervisor(10L);
        Inspection insp = inspection(1L, InspectionStatus.APPROVED);
        InspectionReview review = InspectionReview.builder()
                .id(100L).inspection(insp).reviewer(sup)
                .decision(ReviewDecision.APPROVED).roundNumber(1).build();

        when(reviewRepository.findById(100L)).thenReturn(Optional.of(review));
        when(userRepository.findById(10L)).thenReturn(Optional.of(sup));

        assertThatThrownBy(() -> service.approve(100L, new ApproveRequest(), 10L))
                .isInstanceOf(ConflictException.class);
    }

    @Test
    void approve_deveFalharSeRevisorDiferente() {
        User sup = supervisor(10L);
        User outroSup = supervisor(11L);
        Inspection insp = inspection(1L, InspectionStatus.UNDER_REVIEW);
        InspectionReview review = InspectionReview.builder()
                .id(100L).inspection(insp).reviewer(sup)
                .decision(ReviewDecision.PENDING).roundNumber(1).build();

        when(reviewRepository.findById(100L)).thenReturn(Optional.of(review));
        when(userRepository.findById(11L)).thenReturn(Optional.of(outroSup));

        assertThatThrownBy(() -> service.approve(100L, new ApproveRequest(), 11L))
                .isInstanceOf(ForbiddenException.class);
    }

    // ── reject ────────────────────────────────────────────────────────────────

    @Test
    void reject_deveReprovarComMotivo() {
        User sup = supervisor(10L);
        Inspection insp = inspection(1L, InspectionStatus.UNDER_REVIEW);
        InspectionReview review = InspectionReview.builder()
                .id(100L).inspection(insp).reviewer(sup)
                .decision(ReviewDecision.PENDING).roundNumber(1).build();

        when(reviewRepository.findById(100L)).thenReturn(Optional.of(review));
        when(userRepository.findById(10L)).thenReturn(Optional.of(sup));
        when(reviewRepository.save(any(InspectionReview.class)))
                .thenAnswer(inv -> inv.getArgument(0));

        RejectRequest req = new RejectRequest();
        req.setRejectionReason("Faltam evidências");

        ReviewResponse response = service.reject(100L, req, 10L);

        assertThat(response).isNotNull();
        assertThat(review.getDecision()).isEqualTo(ReviewDecision.REJECTED);
        assertThat(review.getRejectionReason()).isEqualTo("Faltam evidências");
        assertThat(insp.getStatus()).isEqualTo(InspectionStatus.REJECTED);
        verify(auditLogService).log(anyLong(), anyString(), eq(AuditAction.REJECT),
                eq("Inspection"), eq(1L), anyString());
    }
}
