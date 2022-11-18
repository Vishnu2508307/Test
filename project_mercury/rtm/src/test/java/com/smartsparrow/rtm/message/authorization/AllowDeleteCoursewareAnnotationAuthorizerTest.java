package com.smartsparrow.rtm.message.authorization;

import static com.smartsparrow.rtm.RTMWebSocketTestUtils.mockAuthenticationContext;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

import java.util.UUID;

import com.smartsparrow.rtm.message.recv.AnnotationMessage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.smartsparrow.annotation.service.AnnotationService;
import com.smartsparrow.annotation.service.CoursewareAnnotation;
import com.smartsparrow.annotation.service.Motivation;
import com.smartsparrow.courseware.data.CoursewareElementType;
import com.smartsparrow.iam.service.AuthenticationContext;

import reactor.core.publisher.Mono;

class AllowDeleteCoursewareAnnotationAuthorizerTest {

    @InjectMocks
    private AllowDeleteCoursewareAnnotationAuthorizer authorizer;

    @Mock
    private AllowCoursewareElementContributorOrHigher allowCoursewareElementContributorOrHigher;
    @Mock
    private AnnotationService annotationService;
    @Mock
    private CoursewareAnnotation annotation;

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
    void test_success_forCreator() {
        when(annotation.getMotivation()).thenReturn(Motivation.commenting);
        when(allowCoursewareElementContributorOrHigher.test(authenticationContext, message)).thenReturn(false);
        when(annotation.getCreatorAccountId()).thenReturn(accountId);

        boolean result = authorizer.test(authenticationContext, message);

        assertTrue(result);
    }


    @Test
    void test_success_forNotCreator() {
        when(annotation.getMotivation()).thenReturn(Motivation.commenting);
        when(allowCoursewareElementContributorOrHigher.test(authenticationContext, message)).thenReturn(false);
        when(annotation.getCreatorAccountId()).thenReturn(elementId);

        boolean result = authorizer.test(authenticationContext, message);

        assertFalse(result);
    }
}
