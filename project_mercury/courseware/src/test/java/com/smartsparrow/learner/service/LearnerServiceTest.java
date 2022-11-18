package com.smartsparrow.learner.service;

import static com.smartsparrow.courseware.CoursewareDataStubs.buildConfigurationField;
import static com.smartsparrow.courseware.data.CoursewareElementType.ACTIVITY;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.UUID;

import org.assertj.core.util.Lists;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.smartsparrow.competency.data.DocumentItemTag;
import com.smartsparrow.competency.service.DocumentItemLinkService;
import com.smartsparrow.courseware.data.ConfigurationField;
import com.smartsparrow.courseware.data.CoursewareElementType;
import com.smartsparrow.courseware.data.CoursewareGateway;
import com.smartsparrow.courseware.data.LearnerElementConfigurationField;
import com.smartsparrow.courseware.data.ScopeReference;
import com.smartsparrow.learner.data.Deployment;
import com.smartsparrow.learner.data.LearnerCoursewareElement;
import com.smartsparrow.learner.data.LearnerDocumentItemLinkGateway;
import com.smartsparrow.learner.data.LearnerDocumentItemTag;
import com.smartsparrow.learner.data.LearnerGateway;
import com.smartsparrow.learner.data.LearnerScopeReference;
import com.smartsparrow.learner.data.LearnerWalkable;
import com.smartsparrow.plugin.service.PluginService;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

class LearnerServiceTest {

    @InjectMocks
    private LearnerService learnerService;

    @Mock
    private CoursewareGateway coursewareGateway;

    @Mock
    private LearnerGateway learnerGateway;

    @Mock
    private DocumentItemLinkService documentItemLinkService;

    @Mock
    private LearnerDocumentItemLinkGateway learnerDocumentItemLinkGateway;

    @Mock
    private PluginService pluginService;


    private static final UUID elementId = UUID.randomUUID();
    private static final UUID studentScopeURN = UUID.randomUUID();
    private static final UUID pluginId = UUID.randomUUID();
    private static final UUID deploymentId = UUID.randomUUID();
    private static final UUID changeId = UUID.randomUUID();
    private static final String pluginVersion = "1.2.1";

    @BeforeEach
    void setUp() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    void replicateRegisteredStudentScopeElements() {
        ScopeReference scopeReferenceOne = new ScopeReference();
        ScopeReference scopeReferenceTwo = new ScopeReference();

        when(coursewareGateway.findRegisteredElements(studentScopeURN)).thenReturn(Flux.just(
                scopeReferenceOne, scopeReferenceTwo
        ));

        when(pluginService.findLatestVersion(pluginId, pluginVersion)).thenReturn(Mono.just(pluginVersion));

        when(learnerGateway.persist(any(LearnerScopeReference.class))).thenReturn(Flux.just(new Void[]{}));

        LearnerWalkable walkable = mock(LearnerWalkable.class);
        when(walkable.getElementType()).thenReturn(CoursewareElementType.INTERACTIVE);
        when(walkable.getId()).thenReturn(elementId);
        when(walkable.getPluginId()).thenReturn(pluginId);
        when(walkable.getStudentScopeURN()).thenReturn(studentScopeURN);
        when(walkable.getPluginVersionExpr()).thenReturn(pluginVersion);

        Deployment deployment = mock(Deployment.class);
        when(deployment.getId()).thenReturn(deploymentId);
        when(deployment.getChangeId()).thenReturn(changeId);

        learnerService.replicateRegisteredStudentScopeElements(walkable, deployment, true).blockLast();

        verify(learnerGateway, times(2)).persist(any(LearnerScopeReference.class));
    }

    @Test
    void publishDocumentItemLinks() {
        UUID documentId = UUID.randomUUID();
        UUID documentItemId = UUID.randomUUID();

        Deployment deployment = new Deployment()
                .setId(deploymentId)
                .setChangeId(changeId);

        DocumentItemTag documentItemTag = new DocumentItemTag()
                .setDocumentId(documentId)
                .setDocumentItemId(documentItemId)
                .setElementId(elementId)
                .setElementType(CoursewareElementType.INTERACTIVE);

        when(documentItemLinkService.findAll(elementId)).thenReturn(Flux.just(documentItemTag));
        when(learnerDocumentItemLinkGateway.persist(any(LearnerDocumentItemTag.class))).thenReturn(Flux.just(new Void[]{}));

        learnerService.publishDocumentItemLinks(elementId, CoursewareElementType.INTERACTIVE, deployment).subscribe();

        ArgumentCaptor<LearnerDocumentItemTag> captor = ArgumentCaptor.forClass(LearnerDocumentItemTag.class);

        verify(learnerDocumentItemLinkGateway, times(1)).persist(captor.capture());

        LearnerDocumentItemTag persisted = captor.getValue();

        assertNotNull(persisted);
        assertEquals(deploymentId, persisted.getDeploymentId());
        assertEquals(changeId, persisted.getChangeId());
        assertEquals(documentId, persisted.getDocumentId());
        assertEquals(documentItemId, persisted.getDocumentItemId());
        assertEquals(elementId, persisted.getElementId());
        assertEquals(CoursewareElementType.INTERACTIVE, persisted.getElementType());
    }

    @Test
    void publishConfigurationFields_noneFound() {
        Deployment deployment = mock(Deployment.class);

        when(deployment.getId()).thenReturn(deploymentId);
        when(deployment.getChangeId()).thenReturn(changeId);

        when(coursewareGateway.fetchConfigurationFields(elementId)).thenReturn(Flux.empty());

        learnerService.publishConfigurationFields(deployment, elementId).blockLast();

        verify(learnerGateway, never()).persist(any(LearnerElementConfigurationField.class));
    }

    @Test
    void publishConfigurationFields() {
        Deployment deployment = mock(Deployment.class);

        when(deployment.getId()).thenReturn(deploymentId);
        when(deployment.getChangeId()).thenReturn(changeId);

        when(learnerGateway.persist(any(LearnerElementConfigurationField.class))).thenReturn(Flux.just(new Void[]{}));
        when(coursewareGateway.fetchConfigurationFields(elementId)).thenReturn(Flux.just(
                buildConfigurationField("foo","{\"prop\":\"bar\"}"),
                buildConfigurationField("mind","balance")
        ));

        ArgumentCaptor<LearnerElementConfigurationField> captor = ArgumentCaptor.forClass(LearnerElementConfigurationField.class);

        learnerService.publishConfigurationFields(deployment, elementId).blockLast();

        verify(learnerGateway, times(2)).persist(captor.capture());

        List<LearnerElementConfigurationField> published = captor.getAllValues();

        assertNotNull(published);
        assertEquals(2, published.size());

        LearnerElementConfigurationField one = published.get(0);

        assertNotNull(one);
        assertEquals(elementId, one.getElementId());
        assertEquals(deploymentId, one.getDeploymentId());
        assertEquals(changeId, one.getChangeId());
        assertEquals("foo", one.getFieldName());
        assertEquals("{\"prop\":\"bar\"}", one.getFieldValue());

        LearnerElementConfigurationField two = published.get(1);

        assertNotNull(two);
        assertEquals(elementId, two.getElementId());
        assertEquals(deploymentId, two.getDeploymentId());
        assertEquals(changeId, two.getChangeId());
        assertEquals("mind", two.getFieldName());
        assertEquals("balance", two.getFieldValue());
    }

    @Test
    void fetchFields_noneFound() {
        List<String> fields = Lists.newArrayList("foo", "bar");

        when(learnerGateway.findConfigurationField(eq(deploymentId), eq(changeId), eq(elementId), anyString()))
                .thenReturn(Mono.empty());

        List<ConfigurationField> found = learnerService.fetchFields(deploymentId, changeId, elementId, fields)
                .collectList()
                .block();

        assertNotNull(found);
        assertFalse(found.isEmpty());
        assertEquals(2, found.size());

        found.forEach(one -> {
            assertNotNull(one.getFieldName());
            assertNull(one.getFieldValue());
        });
    }

    @Test
    void fetchFields_someFound() {
        List<String> fields = Lists.newArrayList("foo", "bar");

        when(learnerGateway.findConfigurationField(deploymentId, changeId, elementId, "foo"))
                .thenReturn(Mono.empty());
        when(learnerGateway.findConfigurationField(deploymentId, changeId, elementId, "bar"))
                .thenReturn(Mono.just(new ConfigurationField()
                        .setFieldName("bar")
                        .setFieldValue("yay!")));

        List<ConfigurationField> found = learnerService.fetchFields(deploymentId, changeId, elementId, fields)
                .collectList()
                .block();

        assertNotNull(found);
        assertFalse(found.isEmpty());
        assertEquals(2, found.size());

        ConfigurationField foo = found.get(0);
        assertNotNull(foo);
        assertNull(foo.getFieldValue());
        assertEquals("foo", foo.getFieldName());

        ConfigurationField bar = found.get(1);
        assertNotNull(bar);
        assertEquals("bar", bar.getFieldName());
        assertEquals("yay!", bar.getFieldValue());
    }

    @Test
    void fetchFields_allFound() {
        List<String> fields = Lists.newArrayList("foo", "bar");

        when(learnerGateway.findConfigurationField(deploymentId, changeId, elementId, "foo"))
                .thenReturn(Mono.just(new ConfigurationField()
                        .setFieldName("foo")
                        .setFieldValue("yay!")));
        when(learnerGateway.findConfigurationField(deploymentId, changeId, elementId, "bar"))
                .thenReturn(Mono.just(new ConfigurationField()
                        .setFieldName("bar")
                        .setFieldValue("me too")));

        List<ConfigurationField> found = learnerService.fetchFields(deploymentId, changeId, elementId, fields)
                .collectList()
                .block();

        assertNotNull(found);
        assertFalse(found.isEmpty());
        assertEquals(2, found.size());

        assertNotNull(found);
        assertFalse(found.isEmpty());
        assertEquals(2, found.size());

        ConfigurationField foo = found.get(0);
        assertNotNull(foo);
        assertEquals("foo", foo.getFieldName());
        assertEquals("yay!", foo.getFieldValue());

        ConfigurationField bar = found.get(1);
        assertNotNull(bar);
        assertEquals("bar", bar.getFieldName());
        assertEquals("me too", bar.getFieldValue());

    }

    @Test
    void findElementByDeployment_notFound() {
        when(learnerGateway.fetchElementByDeployment(eq(elementId), eq(deploymentId)))
                .thenReturn(Mono.empty());

        LearnerCoursewareElement found = learnerService.findElementByDeployment(elementId, deploymentId)
                .block();

        assertNull(found);
    }

    @Test
    void findElementByDeployment() {
        when(learnerGateway.fetchElementByDeployment(eq(elementId), eq(deploymentId)))
                .thenReturn(Mono.just(new LearnerCoursewareElement()
                                              .setId(elementId)
                                              .setDeploymentId(deploymentId)
                                              .setChangeId(changeId)
                                              .setElementType(ACTIVITY)));

        LearnerCoursewareElement found = learnerService.findElementByDeployment(elementId, deploymentId)
                .block();

        assertNotNull(found);
        assertEquals(elementId, found.getId());
        assertEquals(deploymentId, found.getDeploymentId());
        assertEquals(changeId, found.getChangeId());
        assertEquals(ACTIVITY, found.getElementType());
    }

}
