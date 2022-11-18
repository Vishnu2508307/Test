package com.smartsparrow.learner.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.datastax.driver.core.utils.UUIDs;
import com.smartsparrow.courseware.data.CoursewareElementType;
import com.smartsparrow.exception.IllegalArgumentFault;
import com.smartsparrow.learner.data.CompetencyMet;
import com.smartsparrow.learner.data.CompetencyMetByStudent;
import com.smartsparrow.learner.data.CompetencyMetGateway;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

class CompetencyMetServiceTest {

    @InjectMocks
    private CompetencyMetService learnerCompetencyMetService;

    @Mock
    private CompetencyMetGateway learnerCompetencyMetGateway;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.initMocks(this);
    }

    private UUID metId = UUID.randomUUID();
    private UUID studentId = UUID.randomUUID();
    private UUID attemptId = UUID.randomUUID();
    private UUID changeId = UUID.randomUUID();
    private UUID deploymentId = UUID.randomUUID();
    private UUID documentId = UUID.randomUUID();
    private UUID documentVersionId = UUID.randomUUID();
    private UUID elementId = UUID.randomUUID();
    private UUID evaluationId = UUID.randomUUID();
    private UUID itemId = UUID.randomUUID();
    private Float value = 0.33f;
    private Float confidence = 0.8f;
    private long awardedAt = 1557282841486L;

    private CompetencyMet met = new CompetencyMet()
            .setId(metId)
            .setStudentId(studentId)
            .setAttemptId(attemptId)
            .setChangeId(changeId)
            .setConfidence(confidence)
            .setDeploymentId(deploymentId)
            .setDocumentId(documentId)
            .setDocumentVersionId(documentVersionId)
            .setCoursewareElementId(elementId)
            .setCoursewareElementType(CoursewareElementType.COMPONENT)
            .setEvaluationId(evaluationId)
            .setDocumentItemId(itemId)
            .setValue(value);


    @Test
    void create() {
        when(learnerCompetencyMetGateway.persist(any(CompetencyMet.class))).thenReturn(Flux.empty());

        CompetencyMet competencyMet = learnerCompetencyMetService.create(met.getStudentId(),
                met.getDeploymentId(),
                met.getChangeId(),
                met.getCoursewareElementId(),
                met.getCoursewareElementType(),
                met.getEvaluationId(),
                met.getDocumentId(),
                met.getDocumentVersionId(),
                met.getDocumentItemId(),
                met.getAttemptId(),
                met.getValue(),
                met.getConfidence()).block();

        assertNotNull(competencyMet);
        assertNotNull(competencyMet.getId());
        assertEquals(met.getDeploymentId(), competencyMet.getDeploymentId());
        assertEquals(met.getChangeId(), competencyMet.getChangeId());
        assertEquals(met.getCoursewareElementId(), competencyMet.getCoursewareElementId());
    }

    @Test
    void create_withTimestamp() {
        when(learnerCompetencyMetGateway.persist(any(CompetencyMet.class))).thenReturn(Flux.empty());

        CompetencyMet competencyMet = learnerCompetencyMetService.create(met.getStudentId(),
                null,
                null,
                null,
                null,
                null,
                met.getDocumentId(),
                met.getDocumentVersionId(),
                met.getDocumentItemId(),
                null,
                met.getValue(),
                met.getConfidence(),
                awardedAt).block();

        assertNotNull(competencyMet);
        assertNotNull(competencyMet.getId());
        assertEquals(met.getDocumentId(), competencyMet.getDocumentId());
        assertEquals(met.getDocumentItemId(), competencyMet.getDocumentItemId());
        assertEquals(met.getValue(), competencyMet.getValue());
        assertEquals(met.getConfidence(), competencyMet.getConfidence());
        assertEquals(awardedAt, UUIDs.unixTimestamp(competencyMet.getId()));
    }

    @Test
    void findCompetenciesMet_NullID() {
        IllegalArgumentFault e = assertThrows(IllegalArgumentFault.class, () ->
                learnerCompetencyMetService.findCompetencyMet(null));
        assertEquals("metId is required", e.getMessage());
    }

    @Test
    void findCompetenciesMet_valid() {
        when(learnerCompetencyMetGateway.findById(any(UUID.class)))
                .thenReturn(Mono.just(met));

        CompetencyMet competencyMet = learnerCompetencyMetService
                .findCompetencyMet(UUID.randomUUID())
                .block();

        assertEquals(met, competencyMet);
    }

    @Test
    void findCompetenciesMetByAccount_NullAccountId() {
        IllegalArgumentFault e = assertThrows(IllegalArgumentFault.class, () ->
                learnerCompetencyMetService.findCompetenciesMetByStudent(null));
        assertEquals("studentId is required", e.getMessage());
    }

    @Test
    void findCompetenciesMetByStudent_valid() {
        when(learnerCompetencyMetGateway.findAll(met.getStudentId()))
                .thenReturn(Flux.just(new CompetencyMetByStudent()
                        .setStudentId(studentId)
                        .setMetId(met.getId())));

        when(learnerCompetencyMetGateway.findById(any(UUID.class)))
                .thenReturn(Mono.just(met));

        CompetencyMetByStudent competencyMet = learnerCompetencyMetService
                .findCompetenciesMetByStudent(studentId)
                .blockFirst();

        assertNotNull(competencyMet);
        assertEquals(studentId, competencyMet.getStudentId());
        assertEquals(met.getId(), competencyMet.getMetId());
    }

    @Test
    void findCompetenciesByStudentAndDocumentItem_Invalid() {
        IllegalArgumentFault e = assertThrows(IllegalArgumentFault.class, () ->
                learnerCompetencyMetService.findCompetenciesMetByDocumentItem(null, null, null));
        assertEquals("studentId is required", e.getMessage());

        e = assertThrows(IllegalArgumentFault.class, () ->
                learnerCompetencyMetService.findCompetenciesMetByDocumentItem(studentId, null, null));
        assertEquals("documentId is required", e.getMessage());

        e = assertThrows(IllegalArgumentFault.class, () ->
                learnerCompetencyMetService.findCompetenciesMetByDocumentItem(studentId, documentId, null));
        assertEquals("itemId is required", e.getMessage());
    }

    @Test
    void findCompetenciesByAccountAndDocumentItem_Valid() {
        when(learnerCompetencyMetGateway.findAll(studentId, documentId, itemId))
                .thenReturn(Flux.just(
                        new CompetencyMetByStudent()
                                .setStudentId(studentId)
                                .setDocumentId(documentId)
                                .setDocumentItemId(itemId)
                                .setMetId(metId)));

        CompetencyMetByStudent competencyMet = learnerCompetencyMetService
                .findCompetenciesMetByDocumentItem(studentId, documentId, itemId)
                .blockFirst();

        assertNotNull(competencyMet);
        assertEquals(studentId, competencyMet.getStudentId());
        assertEquals(documentId, competencyMet.getDocumentId());
        assertEquals(itemId, competencyMet.getDocumentItemId());
        assertEquals(metId, competencyMet.getMetId());
    }

    @Test
    void delete() {
        ArgumentCaptor<CompetencyMet> competencyCaptor = ArgumentCaptor.forClass(CompetencyMet.class);
        when(learnerCompetencyMetGateway.delete(any(CompetencyMet.class))).thenReturn(Flux.empty());

        learnerCompetencyMetService.delete(studentId, documentId, itemId, metId);
        verify(learnerCompetencyMetGateway).delete(competencyCaptor.capture());

        CompetencyMet value = competencyCaptor.getValue();
        assertEquals(studentId, value.getStudentId());
        assertEquals(documentId, value.getDocumentId());
        assertEquals(itemId, value.getDocumentItemId());
        assertEquals(metId, value.getId());
    }
}
