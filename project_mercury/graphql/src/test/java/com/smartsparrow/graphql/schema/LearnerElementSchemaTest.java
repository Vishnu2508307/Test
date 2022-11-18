package com.smartsparrow.graphql.schema;

import static com.smartsparrow.courseware.CoursewareDataStubs.buildLearnerActivity;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.smartsparrow.courseware.data.CoursewareElementType;
import com.smartsparrow.learner.data.LearnerActivity;
import com.smartsparrow.learner.data.LearnerActivityPayload;
import com.smartsparrow.learner.data.LearnerCoursewareWalkable;
import com.smartsparrow.learner.data.LearnerWalkablePayload;
import com.smartsparrow.learner.service.LearnerCoursewareElementStructureService;

import graphql.Assert;

import reactor.core.publisher.Mono;

class LearnerElementSchemaTest {

    @InjectMocks
    private LearnerElementSchema learnerElementSchema;

    @Mock
    private LearnerCoursewareElementStructureService elementStructureService;

    private static final UUID deploymentId = UUID.randomUUID();
    private static final UUID elementId = UUID.randomUUID();
    private static final UUID rootElementId = UUID.randomUUID();


    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void getLearnerCoursewareWalkables() {
        LearnerActivity learnerActivity = buildLearnerActivity(elementId, deploymentId, UUID.randomUUID());
        UUID elementId = UUID.randomUUID();
        List<LearnerCoursewareWalkable> learnerCoursewareWalkables = new ArrayList();
        LearnerCoursewareWalkable learnerCoursewareWalkable = new LearnerCoursewareWalkable()
                .setElementId(elementId)
                .setType(CoursewareElementType.ACTIVITY)
                .setLearnerWalkablePayload(new LearnerActivityPayload(learnerActivity));
        learnerCoursewareWalkables.add(learnerCoursewareWalkable);



        when(elementStructureService.getLearnerCoursewareWalkable(deploymentId,
                                                                  elementId)).thenReturn(Mono.just(learnerCoursewareWalkables));

        List<LearnerWalkablePayload> walkablePayloadList = learnerElementSchema.getLearnerElementByDeployment(
                learnerActivity,
                elementId).join();

        Assert.assertNotNull(learnerCoursewareWalkable);
        assertEquals(1, walkablePayloadList.size());

        verify(elementStructureService).getLearnerCoursewareWalkable(deploymentId, elementId);
    }

}
