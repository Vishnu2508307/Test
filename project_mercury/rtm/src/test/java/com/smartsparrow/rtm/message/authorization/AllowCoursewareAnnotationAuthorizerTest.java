package com.smartsparrow.rtm.message.authorization;

import static com.smartsparrow.rtm.RTMWebSocketTestUtils.mockAuthenticationContext;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.smartsparrow.annotation.service.Motivation;
import com.smartsparrow.courseware.data.CoursewareElementType;
import com.smartsparrow.iam.service.AuthenticationContext;
import com.smartsparrow.rtm.message.recv.CoursewareElementMotivationMessage;

class AllowCoursewareAnnotationAuthorizerTest {

    @InjectMocks
    private AllowCoursewareAnnotationAuthorizer authorizer;

    @Mock
    private AllowCoursewareElementContributorOrHigher allowCoursewareElementContributorOrHigher;
    @Mock
    private AllowCoursewareElementReviewerOrHigher allowCoursewareElementReviewerOrHigher;

    private AuthenticationContext authenticationContext;

    private static final UUID accountId = UUID.randomUUID();
    private static final UUID elementId = UUID.randomUUID();
    private static final CoursewareElementType elementType = CoursewareElementType.ACTIVITY;
    private static final UUID workspaceId = UUID.randomUUID();
    private static final Motivation motivation = Motivation.identifying;

    @Mock
    private CoursewareElementMotivationMessage message;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.initMocks(this);
        authenticationContext = mockAuthenticationContext(accountId);
        when(message.getElementId()).thenReturn(elementId);
        when(message.getElementType()).thenReturn(elementType);
        when(message.getMotivation()).thenReturn(motivation);
    }

    @Test
    void test_success_forIdentifyingMotivation() {
        when(allowCoursewareElementContributorOrHigher.test(authenticationContext, message)).thenReturn(true);
        when(allowCoursewareElementReviewerOrHigher.test(authenticationContext, message)).thenReturn(false);

        boolean result = authorizer.test(authenticationContext, message);

        assertTrue(result);
    }

    @Test
    void test_success_forOtherMotivation() {
        when(message.getMotivation()).thenReturn(Motivation.commenting);
        when(allowCoursewareElementContributorOrHigher.test(authenticationContext, message)).thenReturn(false);
        when(allowCoursewareElementReviewerOrHigher.test(authenticationContext, message)).thenReturn(true);


        boolean result = authorizer.test(authenticationContext, message);

        assertTrue(result);
    }
}
