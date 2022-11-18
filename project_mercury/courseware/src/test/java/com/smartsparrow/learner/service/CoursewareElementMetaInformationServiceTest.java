package com.smartsparrow.learner.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.smartsparrow.courseware.data.CoursewareElementMetaInformation;
import com.smartsparrow.courseware.data.CoursewareGateway;
import com.smartsparrow.learner.data.Deployment;
import com.smartsparrow.learner.data.LearnerElementMetaInformation;
import com.smartsparrow.learner.data.LearnerGateway;

import reactor.core.publisher.Flux;

class CoursewareElementMetaInformationServiceTest {

    @InjectMocks
    private CoursewareElementMetaInformationService coursewareElementMetaInformationService;

    @Mock
    private CoursewareGateway coursewareGateway;

    @Mock
    private LearnerGateway learnerGateway;

    private static final UUID elementId = UUID.randomUUID();
    private static final UUID deploymentId = UUID.randomUUID();
    private static final UUID changeId = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    void createMetaInfo() {
        when(coursewareGateway.persist(any(CoursewareElementMetaInformation.class)))
                .thenReturn(Flux.just(new Void[]{}));

        CoursewareElementMetaInformation created = coursewareElementMetaInformationService
                .createMetaInfo(elementId, "key", "value")
                .block();

        assertNotNull(created);
        assertEquals(elementId, created.getElementId());
        assertEquals("key", created.getKey());
        assertEquals("value", created.getValue());
    }

    @Test
    void publish() {
        Deployment deployment = mock(Deployment.class);
        when(deployment.getChangeId()).thenReturn(changeId);
        when(deployment.getId()).thenReturn(deploymentId);
        when(coursewareGateway.fetchAllMetaInformation(elementId))
                .thenReturn(Flux.just(
                        buildCoursewareElementMetaInformation(elementId, "foo", "bar")
                ));
        when(learnerGateway.persist(any(LearnerElementMetaInformation.class)))
                .thenReturn(Flux.just(new Void[]{}));

        List<LearnerElementMetaInformation> published = coursewareElementMetaInformationService.publish(elementId, deployment)
                .collectList()
                .block();

        assertNotNull(published);
        assertEquals(1, published.size());

        LearnerElementMetaInformation learnerMetaInfo = published.get(0);

        assertEquals(deploymentId, learnerMetaInfo.getDeploymentId());
        assertEquals(changeId, learnerMetaInfo.getChangeId());
        assertEquals(elementId, learnerMetaInfo.getElementId());
        assertEquals("foo", learnerMetaInfo.getKey());
        assertEquals("bar", learnerMetaInfo.getValue());

    }

    @Test
    void duplicate() {
        final UUID newElementId = UUID.randomUUID();
        when(coursewareGateway.fetchAllMetaInformation(elementId))
                .thenReturn(Flux.just(
                        buildCoursewareElementMetaInformation(elementId, "foo", "bar")
                ));
        when(coursewareGateway.persist(any(CoursewareElementMetaInformation.class)))
                .thenReturn(Flux.just(new Void[]{}));

        List<CoursewareElementMetaInformation> duplicates = coursewareElementMetaInformationService.duplicate(elementId, newElementId)
                .collectList()
                .block();

        assertNotNull(duplicates);
        assertEquals(1, duplicates.size());

        CoursewareElementMetaInformation duplicate = duplicates.get(0);

        assertEquals("foo", duplicate.getKey());
        assertEquals("bar", duplicate.getValue());
        assertEquals(newElementId, duplicate.getElementId());
    }

    private CoursewareElementMetaInformation buildCoursewareElementMetaInformation(final UUID elementId,
                                                                                   final String key,
                                                                                   final String value) {
        return new CoursewareElementMetaInformation()
                .setElementId(elementId)
                .setValue(value)
                .setKey(key);
    }

}