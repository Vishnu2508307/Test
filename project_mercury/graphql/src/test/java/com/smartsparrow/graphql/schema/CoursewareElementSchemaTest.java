package com.smartsparrow.graphql.schema;

import static graphql.schema.DataFetchingEnvironmentImpl.newDataFetchingEnvironment;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.UUID;

import org.assertj.core.util.Lists;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.smartsparrow.courseware.data.ConfigurationField;
import com.smartsparrow.courseware.data.CoursewareElement;
import com.smartsparrow.courseware.data.CoursewareElementType;
import com.smartsparrow.courseware.service.CoursewareService;
import com.smartsparrow.graphql.BronteGQLContext;
import com.smartsparrow.graphql.auth.AllowWorkspaceReviewerOrHigher;
import com.smartsparrow.iam.service.AuthenticationContext;
import com.smartsparrow.iam.service.MutableAuthenticationContext;
import com.smartsparrow.learner.data.Deployment;
import com.smartsparrow.learner.data.LearnerCoursewareElement;
import com.smartsparrow.learner.service.LearnerService;
import com.smartsparrow.workspace.data.Workspace;

import graphql.Assert;
import io.leangen.graphql.execution.ResolutionEnvironment;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

class CoursewareElementSchemaTest {

    @InjectMocks
    private CoursewareElementSchema coursewareElementSchema;

    @Mock
    private CoursewareService coursewareService;

    @Mock
    private LearnerService learnerService;

    @Mock
    private AllowWorkspaceReviewerOrHigher allowWorkspaceReviewerOrHigher;

    @Mock
    private AuthenticationContext authenticationContext;

    @Mock
    private MutableAuthenticationContext mutableAuthenticationContext;

    private ResolutionEnvironment resolutionEnvironment;

    private static final UUID workspaceId = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        resolutionEnvironment= new ResolutionEnvironment(null, newDataFetchingEnvironment()
                .context(new BronteGQLContext()
                                 .setMutableAuthenticationContext(mutableAuthenticationContext)
                                 .setAuthenticationContext(authenticationContext)).build(),
                                                         null,
                                                         null,
                                                         null,
                                                         null);
    }

    @Test
    void getCoursewareElementById() {
        UUID elementId = UUID.randomUUID();
        when(allowWorkspaceReviewerOrHigher.test(authenticationContext,workspaceId)).thenReturn(true);
        when(coursewareService.findCoursewareElement(elementId))
                .thenReturn(Mono.just(new CoursewareElement()));

        CoursewareElement element = coursewareElementSchema.getCoursewareElement(resolutionEnvironment,new Workspace()
                .setId(workspaceId), elementId)
                .join();

        Assert.assertNotNull(element);

        verify(coursewareService).findCoursewareElement(elementId);
    }

    @Test
    void getCoursewareElementFields() {
        CoursewareElement element = CoursewareElement.from(UUID.randomUUID(), CoursewareElementType.ACTIVITY);

        when(coursewareService.fetchConfigurationFields(element.getElementId(), Lists.newArrayList("foo")))
                .thenReturn(Flux.just(new ConfigurationField()));

        List<ConfigurationField> fields = coursewareElementSchema
                .getCoursewareElementFields(element, Lists.newArrayList("foo"))
                .join();

        assertNotNull(fields);
        assertEquals(1, fields.size());
    }

    @Test
    void getLearnerElementByDeployment() {
        UUID elementId = UUID.randomUUID();
        UUID deploymentId = UUID.randomUUID();
        when(learnerService.findElementByDeployment(elementId, deploymentId))
                .thenReturn(Mono.just(new LearnerCoursewareElement()));

        CoursewareElement element = coursewareElementSchema
                .getLearnerElementByDeployment(new Deployment().setId(deploymentId), elementId)
                .join();

        Assert.assertNotNull(element);

        verify(learnerService).findElementByDeployment(elementId, deploymentId);
    }
}
