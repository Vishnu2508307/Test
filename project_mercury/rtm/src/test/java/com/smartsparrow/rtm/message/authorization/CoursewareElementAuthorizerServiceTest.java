package com.smartsparrow.rtm.message.authorization;

import com.smartsparrow.courseware.data.CoursewareElementType;
import com.smartsparrow.courseware.service.CoursewareService;
import com.smartsparrow.iam.service.AuthenticationContext;
import com.smartsparrow.iam.service.PermissionLevel;
import com.smartsparrow.rtm.message.recv.courseware.CoursewareElementMessage;
import com.smartsparrow.workspace.service.ProjectPermissionService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import reactor.core.publisher.Mono;
import reactor.test.publisher.TestPublisher;

import java.util.UUID;

import static com.smartsparrow.rtm.RTMWebSocketTestUtils.mockAuthenticationContext;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

public class CoursewareElementAuthorizerServiceTest {

    @InjectMocks
    private CoursewareElementAuthorizerService authorizer;

    @Mock
    private CoursewareService coursewareService;
    @Mock
    private ProjectPermissionService projectPermissionService;

    private AuthenticationContext authenticationContext;

    private static final UUID accountId = UUID.randomUUID();
    private static final UUID elementId = UUID.randomUUID();
    private static final CoursewareElementType elementType = CoursewareElementType.ACTIVITY;
    private static final UUID projectId = UUID.randomUUID();

    @Mock
    private CoursewareElementMessage message;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.initMocks(this);
        authenticationContext = mockAuthenticationContext(accountId);
        when(message.getElementId()).thenReturn(elementId);
        when(message.getElementType()).thenReturn(elementType);
        when(coursewareService.getProjectId(elementId, CoursewareElementType.ACTIVITY)).thenReturn(Mono.just(projectId));
    }

    @Test
    void test_success_forContributor() {
        when(projectPermissionService.findHighestPermissionLevel(accountId, projectId)).thenReturn(Mono.just(PermissionLevel.CONTRIBUTOR));

        boolean result = authorizer.authorize(authenticationContext, message.getElementId(),message.getElementType(),PermissionLevel.CONTRIBUTOR);

        assertTrue(result);
    }

    @Test
    void test_success_forOwner() {
        when(projectPermissionService.findHighestPermissionLevel(accountId, projectId)).thenReturn(Mono.just(PermissionLevel.OWNER));

        boolean result = authorizer.authorize(authenticationContext, message.getElementId(),message.getElementType(),PermissionLevel.OWNER);

        assertTrue(result);
    }

    @Test
    void test_workspaceNotFound() {

        boolean result = authorizer.authorize(authenticationContext, message.getElementId(),message.getElementType(),PermissionLevel.REVIEWER);

        assertFalse(result);
    }

    @Test
    void test_noPermission() {
        when(projectPermissionService.findHighestPermissionLevel(accountId, projectId)).thenReturn(Mono.empty());

        boolean result = authorizer.authorize(authenticationContext, message.getElementId(),message.getElementType(),PermissionLevel.REVIEWER);

        assertFalse(result);
    }

    @Test
    void test_exception() {
        TestPublisher<UUID> workspacePublisher = TestPublisher.create();
        workspacePublisher.error(new RuntimeException("any runtime exception"));

        boolean result = authorizer.authorize(authenticationContext, message.getElementId(),message.getElementType(),PermissionLevel.REVIEWER);

        assertFalse(result);
    }
}
