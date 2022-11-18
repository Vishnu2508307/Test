package com.smartsparrow.graphql.schema;

import static com.smartsparrow.courseware.data.CoursewareElementType.ACTIVITY;
import static graphql.Assert.assertNotNull;
import static graphql.schema.DataFetchingEnvironmentImpl.newDataFetchingEnvironment;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.smartsparrow.courseware.data.CoursewareElementType;
import com.smartsparrow.courseware.data.tree.CoursewareElementNode;
import com.smartsparrow.courseware.service.CoursewareElementStructureNavigateService;
import com.smartsparrow.courseware.service.CoursewareElementStructureService;
import com.smartsparrow.graphql.BronteGQLContext;
import com.smartsparrow.graphql.auth.AllowWorkspaceReviewerOrHigher;
import com.smartsparrow.iam.exception.PermissionFault;
import com.smartsparrow.iam.service.MutableAuthenticationContext;
import com.smartsparrow.iam.wiring.AuthenticationContextProvider;
import com.smartsparrow.workspace.data.Workspace;

import io.leangen.graphql.execution.ResolutionEnvironment;
import reactor.core.publisher.Mono;

class CoursewareElementStructureSchemaTest {

    @InjectMocks
    private CoursewareElementStructureSchema coursewareElementStructureSchema;

    @Mock
    private CoursewareElementStructureService coursewareElementStructureService;

    @Mock
    private CoursewareElementStructureNavigateService coursewareElementStructureNavigateService;

    @Mock
    private AllowWorkspaceReviewerOrHigher allowWorkspaceReviewerOrHigher;

    @Mock
    private MutableAuthenticationContext mutableAuthenticationContext;

    @Mock
    private AuthenticationContextProvider authenticationContextProvider;

    private ResolutionEnvironment resolutionEnvironment;

    private final UUID workspaceId = UUID.randomUUID();
    private final UUID elementId = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        coursewareElementStructureSchema = new CoursewareElementStructureSchema(allowWorkspaceReviewerOrHigher,
                                                                                coursewareElementStructureService,
                                                                                coursewareElementStructureNavigateService);

        resolutionEnvironment= new ResolutionEnvironment(null, newDataFetchingEnvironment()
                .context(new BronteGQLContext()
                                 .setMutableAuthenticationContext(mutableAuthenticationContext)
                                 .setAuthenticationContext(authenticationContextProvider.get())).build(),
                                                         null,
                                                         null,
                                                         null,
                                                         null);
    }

    @Test
    void getCoursewareElementIndex_notAuthorized() {
        when(allowWorkspaceReviewerOrHigher.test(authenticationContextProvider.get(), workspaceId)).thenReturn(false);

        PermissionFault e = assertThrows(PermissionFault.class,
                                         () -> coursewareElementStructureSchema.getCoursewareElementIndex(
                                                 resolutionEnvironment,
                                                 new Workspace().setId(workspaceId),
                                                 elementId,
                                                 ACTIVITY));

        Assertions.assertNotNull(e);
        assertEquals("Not allowed", e.getMessage());
    }

    @Test
    void getCoursewareElementIndex() {
        when(allowWorkspaceReviewerOrHigher.test(authenticationContextProvider.get(), workspaceId)).thenReturn(true);
        when(coursewareElementStructureService.getCoursewareElementStructure(any(UUID.class),
                                                                             any(CoursewareElementType.class),
                                                                             anyList()))
                .thenReturn(Mono.just(new CoursewareElementNode().setElementId(elementId)));

        List<CoursewareElementNode> elementNodeList = coursewareElementStructureSchema.getCoursewareElementIndex(
                resolutionEnvironment,
                new Workspace().setId(workspaceId),
                elementId,
                ACTIVITY).join();

        assertNotNull(elementNodeList);
    }

    @Test
    void getCoursewareElementStructure_notAuthorized() {
        when(allowWorkspaceReviewerOrHigher.test(authenticationContextProvider.get(), workspaceId)).thenReturn(false);

        PermissionFault e = assertThrows(PermissionFault.class,
                                         () -> coursewareElementStructureSchema.getCoursewareElementStructure(
                                                 resolutionEnvironment,
                                                 new Workspace().setId(workspaceId),
                                                 elementId,
                                                 ACTIVITY));

        Assertions.assertNotNull(e);
        assertEquals("Not allowed", e.getMessage());
    }

    @Test
    void getCoursewareElementStructure() {
        when(allowWorkspaceReviewerOrHigher.test(authenticationContextProvider.get(), workspaceId)).thenReturn(true);
        when(coursewareElementStructureService.getCoursewareElementStructure(any(UUID.class),
                                                                             any(CoursewareElementType.class),
                                                                             anyList()))
                .thenReturn(Mono.just(new CoursewareElementNode().setElementId(elementId)));

        CoursewareElementNode element = coursewareElementStructureSchema.getCoursewareElementStructure(
                resolutionEnvironment,
                new Workspace().setId(workspaceId),
                elementId,
                ACTIVITY).join();

        assertNotNull(element);
    }

    @Test
    void navigateCoursewareElementStructure_notAuthorized() {
        when(allowWorkspaceReviewerOrHigher.test(authenticationContextProvider.get(), workspaceId)).thenReturn(false);

        PermissionFault e = assertThrows(PermissionFault.class,
                                         () -> coursewareElementStructureSchema.navigateCoursewareElementStructure(
                                                 resolutionEnvironment,
                                                 new Workspace().setId(workspaceId),
                                                 elementId,
                                                 ACTIVITY));

        Assertions.assertNotNull(e);
        assertEquals("Not allowed", e.getMessage());
    }

    @Test
    void navigateCoursewareElementStructure() {
        when(allowWorkspaceReviewerOrHigher.test(authenticationContextProvider.get(), workspaceId)).thenReturn(true);
        when(coursewareElementStructureNavigateService.getCoursewareElementStructure(any(UUID.class),
                                                                                     any(CoursewareElementType.class),
                                                                                     anyList()))
                .thenReturn(Mono.just(new CoursewareElementNode().setElementId(elementId)));

        CoursewareElementNode element = coursewareElementStructureSchema.navigateCoursewareElementStructure(
                resolutionEnvironment,
                new Workspace().setId(workspaceId),
                elementId,
                ACTIVITY).join();

        assertNotNull(element);
    }
}
