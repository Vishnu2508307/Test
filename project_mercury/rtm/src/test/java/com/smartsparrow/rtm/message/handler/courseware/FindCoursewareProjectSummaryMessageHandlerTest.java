package com.smartsparrow.rtm.message.handler.courseware;


import static com.smartsparrow.rtm.message.handler.courseware.FindCoursewareProjectSummaryMessageHandler.AUTHOR_COURSEWARE_PROJECT_FIND_OK;
import static com.smartsparrow.rtm.message.handler.courseware.FindCoursewareProjectSummaryMessageHandler.AUTHOR_COURSEWARE_PROJECT_FIND_ERROR;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.util.Map;
import java.util.UUID;

import org.eclipse.jetty.websocket.api.Session;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.smartsparrow.courseware.data.CoursewareElementType;
import com.smartsparrow.courseware.service.CoursewareService;
import com.smartsparrow.exception.IllegalArgumentFault;
import com.smartsparrow.export.data.ExportResultNotification;
import com.smartsparrow.rtm.MessageHandlerTestUtils;
import com.smartsparrow.rtm.RTMWebSocketTestUtils;
import com.smartsparrow.rtm.lang.WriteResponseException;
import com.smartsparrow.rtm.message.recv.courseware.FindCoursewareProjectMessage;
import com.smartsparrow.workspace.data.Project;

import reactor.core.publisher.Mono;
import reactor.test.publisher.TestPublisher;

public class FindCoursewareProjectSummaryMessageHandlerTest {

    private static final Session session = RTMWebSocketTestUtils.mockSession();
    private static final UUID elementId = UUID.randomUUID();
    private static final CoursewareElementType elementType = CoursewareElementType.ACTIVITY;
    private static final UUID projectId = UUID.randomUUID();
    ;
    private static final UUID workspaceId = UUID.randomUUID();

    @InjectMocks
    private FindCoursewareProjectSummaryMessageHandler handler;
    @Mock
    private FindCoursewareProjectMessage message;
    @Mock
    private CoursewareService coursewareService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.initMocks(this);
        when(message.getElementId()).thenReturn(elementId);
        when(message.getElementType()).thenReturn(elementType);
    }

    @Test
    void validate_missingElementId() {
        when(message.getElementId()).thenReturn(null);

        IllegalArgumentFault f = assertThrows(IllegalArgumentFault.class, () -> handler.validate(message));

        assertEquals("missing elementId", f.getMessage());
    }

    @Test
    void validate_missingElementType() {
        when(message.getElementType()).thenReturn(null);

        IllegalArgumentFault f = assertThrows(IllegalArgumentFault.class, () -> handler.validate(message));

        assertEquals("missing elementType", f.getMessage());
    }

    @Test
    void handle_success() throws IOException {
        when(coursewareService.findProjectSummary(elementId, elementType))
                .thenReturn(Mono.just(new Project()
                                              .setId(projectId)
                                              .setName("Project_TRO")
                                              .setWorkspaceId(workspaceId)));

        handler.handle(session, message);

        MessageHandlerTestUtils.verifySentMessage(session, response -> {
            assertAll(() -> {
                assertEquals(AUTHOR_COURSEWARE_PROJECT_FIND_OK, response.getType());
                Map responseMap = ((Map) response.getResponse().get("projectSummary"));
                assertEquals(projectId.toString(), responseMap.get("id"));
                assertEquals(workspaceId.toString(), responseMap.get("workspaceId"));
            });
        });
    }

    @Test
    void handle_error() throws WriteResponseException {
        TestPublisher<ExportResultNotification> error = TestPublisher.create();
        error.error(new RuntimeException("can't create"));
        Mono mono = TestPublisher.create().error(new RuntimeException("someException")).mono();
        when(coursewareService.findProjectSummary(elementId, elementType))
                .thenReturn(mono);

        handler.handle(session, message);

        MessageHandlerTestUtils.verifySentMessage(session,
                                                  "{\"type\":\"" + AUTHOR_COURSEWARE_PROJECT_FIND_ERROR + "\",\"code\":422," +
                                                          "\"message\":\"Unable to find courseware project summary\"}");
    }
}
