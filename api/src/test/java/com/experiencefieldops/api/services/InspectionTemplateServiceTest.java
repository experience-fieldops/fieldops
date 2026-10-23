package com.experiencefieldops.api.services;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.experiencefieldops.api.dto.TemplateRequest;
import com.experiencefieldops.api.dto.TemplateResponse;
import com.experiencefieldops.api.dto.TemplateStatusRequest;
import com.experiencefieldops.api.entities.InspectionTemplate;
import com.experiencefieldops.api.entities.InspectionTemplateVersion;
import com.experiencefieldops.api.entities.TemplateSection;
import com.experiencefieldops.api.enums.TemplateStatus;
import com.experiencefieldops.api.exception.ResourceNotFoundException;
import com.experiencefieldops.api.repositories.InspectionTemplateRepository;
import com.experiencefieldops.api.repositories.InspectionTemplateVersionRepository;
import com.experiencefieldops.api.repositories.TemplateItemRepository;
import com.experiencefieldops.api.repositories.TemplateSectionRepository;
import com.experiencefieldops.api.services.impl.InspectionTemplateServiceImpl;

@ExtendWith(MockitoExtension.class)
class InspectionTemplateServiceTest {

    @Mock
    private InspectionTemplateRepository templateRepository;
    @Mock
    private InspectionTemplateVersionRepository versionRepository;
    @Mock
    private TemplateSectionRepository sectionRepository;
    @Mock
    private TemplateItemRepository itemRepository;

    @InjectMocks
    private InspectionTemplateServiceImpl service;

    private TemplateRequest buildRequest() {
        TemplateRequest r = new TemplateRequest();
        r.setTitle("Inspeção de Compressores");
        r.setDescription("Checklist mecânico");
        r.setCategory("MECANICO");
        return r;
    }

    private InspectionTemplate buildTemplate(Long id, int currentVersion, TemplateStatus status) {
        return InspectionTemplate.builder()
                .id(id)
                .title("Inspeção de Compressores")
                .description("Checklist mecânico")
                .category("MECANICO")
                .status(status)
                .currentVersion(currentVersion)
                .build();
    }

    @Test
    void create_deveSalvarComoDraftEVersaoZero() {
        when(templateRepository.save(any(InspectionTemplate.class)))
                .thenAnswer(inv -> inv.getArgument(0));

        TemplateResponse response = service.create(buildRequest());

        ArgumentCaptor<InspectionTemplate> captor = ArgumentCaptor.forClass(InspectionTemplate.class);
        verify(templateRepository).save(captor.capture());
        assertThat(captor.getValue().getStatus()).isEqualTo(TemplateStatus.DRAFT);
        assertThat(captor.getValue().getCurrentVersion()).isZero();
        assertThat(response.getTitle()).isEqualTo("Inspeção de Compressores");
    }

    @Test
    void findById_deveLancarNotFoundQuandoNaoExiste() {
        when(templateRepository.findByIdAndDeletedAtIsNull(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.findById(99L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void update_deveAtualizarMetadados() {
        InspectionTemplate existing = buildTemplate(1L, 0, TemplateStatus.DRAFT);
        when(templateRepository.findByIdAndDeletedAtIsNull(1L)).thenReturn(Optional.of(existing));
        when(templateRepository.save(any(InspectionTemplate.class)))
                .thenAnswer(inv -> inv.getArgument(0));

        TemplateRequest req = new TemplateRequest();
        req.setTitle("Novo título");
        req.setDescription("Nova descrição");
        req.setCategory("ELETRICO");

        TemplateResponse response = service.update(1L, req);

        assertThat(response.getTitle()).isEqualTo("Novo título");
        assertThat(response.getCategory()).isEqualTo("ELETRICO");
    }

    @Test
    void updateStatus_deveBloquearAtivacaoSemPublicacao() {
        InspectionTemplate existing = buildTemplate(1L, 0, TemplateStatus.DRAFT);
        when(templateRepository.findByIdAndDeletedAtIsNull(1L)).thenReturn(Optional.of(existing));

        TemplateStatusRequest req = new TemplateStatusRequest();
        req.setStatus(TemplateStatus.ACTIVE);

        assertThatThrownBy(() -> service.updateStatus(1L, req))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("publicado");

        verify(templateRepository, never()).save(any());
    }

    @Test
    void updateStatus_devePermitirInativar() {
        InspectionTemplate existing = buildTemplate(1L, 1, TemplateStatus.ACTIVE);
        when(templateRepository.findByIdAndDeletedAtIsNull(1L)).thenReturn(Optional.of(existing));
        when(templateRepository.save(any(InspectionTemplate.class)))
                .thenAnswer(inv -> inv.getArgument(0));

        TemplateStatusRequest req = new TemplateStatusRequest();
        req.setStatus(TemplateStatus.INACTIVE);

        TemplateResponse response = service.updateStatus(1L, req);

        assertThat(response.getStatus()).isEqualTo(TemplateStatus.INACTIVE);
    }

    @Test
    void publish_deveFalharSemSecoes() {
        InspectionTemplate existing = buildTemplate(1L, 0, TemplateStatus.DRAFT);
        when(templateRepository.findByIdAndDeletedAtIsNull(1L)).thenReturn(Optional.of(existing));
        when(versionRepository.save(any(InspectionTemplateVersion.class)))
                .thenAnswer(inv -> {
                    InspectionTemplateVersion v = inv.getArgument(0);
                    v.setId(10L);
                    return v;
                });
        when(sectionRepository.findByTemplateVersionIdOrderByDisplayOrder(10L))
                .thenReturn(List.of());

        assertThatThrownBy(() -> service.publish(1L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("seção");
    }

    @Test
    void publish_deveFalharSemItens() {
        InspectionTemplate existing = buildTemplate(1L, 0, TemplateStatus.DRAFT);
        when(templateRepository.findByIdAndDeletedAtIsNull(1L)).thenReturn(Optional.of(existing));
        when(versionRepository.save(any(InspectionTemplateVersion.class)))
                .thenAnswer(inv -> {
                    InspectionTemplateVersion v = inv.getArgument(0);
                    v.setId(10L);
                    return v;
                });

        TemplateSection section = TemplateSection.builder().id(5L).title("Visual").displayOrder(0).build();
        when(sectionRepository.findByTemplateVersionIdOrderByDisplayOrder(10L))
                .thenReturn(List.of(section));
        when(itemRepository.existsBySectionId(5L)).thenReturn(false);

        assertThatThrownBy(() -> service.publish(1L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("item");
    }

    @Test
    void publish_deveAtivarTemplateQuandoHaSecaoEItem() {
        InspectionTemplate existing = buildTemplate(1L, 0, TemplateStatus.DRAFT);
        when(templateRepository.findByIdAndDeletedAtIsNull(1L)).thenReturn(Optional.of(existing));
        when(versionRepository.save(any(InspectionTemplateVersion.class)))
                .thenAnswer(inv -> {
                    InspectionTemplateVersion v = inv.getArgument(0);
                    v.setId(10L);
                    return v;
                });
        when(templateRepository.save(any(InspectionTemplate.class)))
                .thenAnswer(inv -> inv.getArgument(0));

        InspectionTemplateVersion versionRef = InspectionTemplateVersion.builder()
                .id(10L).template(existing).versionNumber(1).titleSnapshot("T").build();
        TemplateSection section = TemplateSection.builder()
                .id(5L).templateVersion(versionRef).title("Visual").displayOrder(0).build();
        when(sectionRepository.findByTemplateVersionIdOrderByDisplayOrder(10L))
                .thenReturn(List.of(section));
        when(itemRepository.existsBySectionId(5L)).thenReturn(true);
        when(itemRepository.findBySectionIdOrderByDisplayOrder(5L)).thenReturn(List.of());

        service.publish(1L);

        assertThat(existing.getStatus()).isEqualTo(TemplateStatus.ACTIVE);
        assertThat(existing.getCurrentVersion()).isEqualTo(1);
    }

    @Test
    void listVersions_deveRetornarVersoesDoTemplate() {
        InspectionTemplate existing = buildTemplate(1L, 1, TemplateStatus.ACTIVE);
        when(templateRepository.findByIdAndDeletedAtIsNull(1L)).thenReturn(Optional.of(existing));

        InspectionTemplateVersion v1 = InspectionTemplateVersion.builder()
                .id(10L).template(existing).versionNumber(1).titleSnapshot("T").build();
        when(versionRepository.findByTemplateIdOrderByVersionNumberDesc(1L))
                .thenReturn(List.of(v1));

        assertThat(service.listVersions(1L)).hasSize(1);
    }

    @Test
    void getVersionWithSections_deveLancarNotFoundQuandoVersaoNaoExiste() {
        when(versionRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.getVersionWithSections(99L))
                .isInstanceOf(ResourceNotFoundException.class);
    }
}
