package com.smartsparrow.rtm.message.handler.courseware;

import static com.smartsparrow.rtm.MessageHandlerTestUtils.verifySentMessage;
import static com.smartsparrow.rtm.message.handler.courseware.CoursewareBreadcrumbMessageHandler.AUTHOR_COURSEWARE_BREADCRUMB_ERROR;
import static com.smartsparrow.rtm.message.handler.courseware.CoursewareBreadcrumbMessageHandler.AUTHOR_COURSEWARE_BREADCRUMB_OK;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.eclipse.jetty.websocket.api.Session;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.google.common.collect.Lists;
import com.smartsparrow.courseware.data.CoursewareElement;
import com.smartsparrow.courseware.data.CoursewareElementType;
import com.smartsparrow.courseware.lang.ParentActivityNotFoundException;
import com.smartsparrow.courseware.service.CoursewareService;
import com.smartsparrow.rtm.RTMWebSocketTestUtils;
import com.smartsparrow.rtm.lang.RTMValidationException;
import com.smartsparrow.rtm.message.recv.courseware.CoursewareBreadcrumbMessage;

import reactor.core.publisher.Mono;
import reactor.test.publisher.TestPublisher;

class CoursewareBreadcrumbMessageHandlerTest {

    @InjectMocks
    private CoursewareBreadcrumbMessageHandler handler;

    @Mock
    private CoursewareService coursewareService;

    @Mock
    private CoursewareBreadcrumbMessage message;
    private Session session;

    private static final UUID elementId = UUID.randomUUID();
    private static final CoursewareElementType elementType = CoursewareElementType.ACTIVITY;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.initMocks(this);
        session = RTMWebSocketTestUtils.mockSession();

        when(message.getElementId()).thenReturn(elementId);
        when(message.getElementType()).thenReturn(elementType);
    }

    @Test
    void validate_noElementId() {
        when(message.getElementId()).thenReturn(null);

        RTMValidationException t = assertThrows(RTMValidationException.class, () -> handler.validate(message));
        assertEquals("elementId is required", t.getErrorMessage());
        assertEquals(AUTHOR_COURSEWARE_BREADCRUMB_ERROR, t.getType());
        assertEquals(400, t.getStatusCode());
    }

    @Test
    void validate_noElementType() {
        when(message.getElementType()).thenReturn(null);

        RTMValidationException t = assertThrows(RTMValidationException.class, () -> handler.validate(message));
        assertEquals("elementType is required", t.getErrorMessage());
        assertEquals(AUTHOR_COURSEWARE_BREADCRUMB_ERROR, t.getType());
        assertEquals(400, t.getStatusCode());
    }

    @Test
    void validate() throws RTMValidationException {
        handler.validate(message);
    }

    @SuppressWarnings("unchecked")
    @Test
    void handle() throws IOException {
        UUID workspaceId = UUID.randomUUID();
        UUID projectId = UUID.randomUUID();
        CoursewareElement el1 = new CoursewareElement(UUID.randomUUID(), CoursewareElementType.ACTIVITY);
        CoursewareElement el2 = new CoursewareElement(UUID.randomUUID(), CoursewareElementType.PATHWAY);
        CoursewareElement el3 = new CoursewareElement(UUID.randomUUID(), CoursewareElementType.INTERACTIVE);
        when(coursewareService.getPath(elementId, elementType)).thenReturn(Mono.just(Lists.newArrayList(el1, el2, el3)));
        when(coursewareService.getWorkspaceIdByProject(elementId, elementType)).thenReturn(Mono.just(workspaceId));
        when(coursewareService.getProjectId(elementId, elementType)).thenReturn(Mono.just(projectId));

        handler.handle(session, message);

        verifySentMessage(session, response -> {
            assertEquals(AUTHOR_COURSEWARE_BREADCRUMB_OK, response.getType());
            assertEquals(workspaceId.toString(), response.getResponse().get("workspaceId"));
            assertEquals(projectId.toString(), response.getResponse().get("projectId"));
            List<Map> breadcrumb = (List<Map>) response.getResponse().get("breadcrumb");
            assertEquals(3, breadcrumb.size());
            assertEquals(el1.getElementId().toString(), breadcrumb.get(0).get("elementId"));
            assertEquals(el1.getElementType().name(), breadcrumb.get(0).get("elementType"));
            assertEquals(el2.getElementId().toString(), breadcrumb.get(1).get("elementId"));
            assertEquals(el2.getElementType().name(), breadcrumb.get(1).get("elementType"));
            assertEquals(el3.getElementId().toString(), breadcrumb.get(2).get("elementId"));
            assertEquals(el3.getElementType().name(), breadcrumb.get(2).get("elementType"));
        });
    }

    @Test
    void handle_parentNotFoundError() {
        UUID pathwayId = UUID.randomUUID();
        TestPublisher<List<CoursewareElement>> publisher = TestPublisher.create();
        publisher.error(new ParentActivityNotFoundException(pathwayId));
        when(coursewareService.getPath(elementId, elementType)).thenReturn(publisher.mono());
        when(coursewareService.getWorkspaceIdByProject(elementId, elementType)).thenReturn(Mono.just(UUID.randomUUID()));
        when(coursewareService.getProjectId(elementId, elementType)).thenReturn(Mono.just(UUID.randomUUID()));

        handler.handle(session, message);

        verifySentMessage(session, "{\"type\":\"" + AUTHOR_COURSEWARE_BREADCRUMB_ERROR + "\",\"code\":422," +
                "\"message\":\"parent activity not found for pathway " + pathwayId + "\"}");
    }

    @Test
    void handle_error() {
        TestPublisher<List<CoursewareElement>> publisher = TestPublisher.create();
        publisher.error(new RuntimeException("some exception"));
        when(coursewareService.getPath(elementId, elementType)).thenReturn(publisher.mono());
        when(coursewareService.getWorkspaceIdByProject(elementId, elementType)).thenReturn(Mono.just(UUID.randomUUID()));
        when(coursewareService.getProjectId(elementId, elementType)).thenReturn(Mono.just(UUID.randomUUID()));

        handler.handle(session, message);

        verifySentMessage(session, "{\"type\":\"" + AUTHOR_COURSEWARE_BREADCRUMB_ERROR + "\",\"code\":422," +
                "\"message\":\"Unable to fetch breadcrumb\"}");
    }
}
