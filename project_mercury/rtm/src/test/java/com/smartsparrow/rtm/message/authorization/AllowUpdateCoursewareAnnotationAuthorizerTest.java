package com.smartsparrow.rtm.message.authorization;

import com.smartsparrow.annotation.service.AnnotationService;
import com.smartsparrow.annotation.service.CoursewareAnnotation;
import com.smartsparrow.annotation.service.Motivation;
import com.smartsparrow.courseware.data.CoursewareElementType;
import com.smartsparrow.iam.service.AuthenticationContext;
import com.smartsparrow.rtm.message.recv.AnnotationMessage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import reactor.core.publisher.Mono;

import java.util.UUID;

import static com.smartsparrow.rtm.RTMWebSocketTestUtils.mockAuthenticationContext;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

class AllowUpdateCoursewareAnnotationAuthorizerTest {

    @InjectMocks
    private AllowUpdateCoursewareAnnotationAuthorizer authorizer;

    @Mock
    private AnnotationService annotationService;
    @Mock
    private CoursewareAnnotation annotation;

    @Mock
    private AllowCoursewareElementContributorOrHigher allowCoursewareElementContributorOrHigher;

    @Mock
    private AllowCoursewareElementReviewerOrHigher coursewareElementReviewerOrHigher;

    private AuthenticationContext authenticationContext;

    private static final UUID accountId = UUID.randomUUID();
    private static final UUID elementId = UUID.randomUUID();
    private static final UUID annotationId = UUID.randomUUID();
    private static final CoursewareElementType elementType = CoursewareElementType.ACTIVITY;

    @Mock
    private AnnotationMessage message;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.initMocks(this);
        authenticationContext = mockAuthenticationContext(accountId);
        when(message.getElementId()).thenReturn(elementId);
        when(message.getElementType()).thenReturn(elementType);
        when(message.getAnnotationId()).thenReturn(annotationId);
        when(annotationService.findCoursewareAnnotation(annotationId)).thenReturn(Mono.just(annotation));
    }

    @Test
    void test_success_forIdentifyingMotivation() {
        when(annotation.getMotivation()).thenReturn(Motivation.identifying);
        when(allowCoursewareElementContributorOrHigher.test(authenticationContext, message)).thenReturn(true);
        boolean result = authorizer.test(authenticationContext, message);
        assertTrue(result);
    }

    @Test
    void test_AnnotationIdNull() {
        when(annotationService.findCoursewareAnnotation(annotationId)).thenReturn(Mono.just(new CoursewareAnnotation()));

        boolean result = authorizer.test(authenticationContext, message);

        assertFalse(result);
    }


    @Test
    void test_success_NotIdentifyingMotivation() {
        when(annotation.getMotivation()).thenReturn(Motivation.commenting);
        when(allowCoursewareElementContributorOrHigher.test(authenticationContext, message)).thenReturn(false);
        when(annotation.getCreatorAccountId()).thenReturn(elementId);
        when(coursewareElementReviewerOrHigher.test(authenticationContext,message)).thenReturn(true);

        boolean result = authorizer.test(authenticationContext, message);

        assertTrue(result);
    }
}
