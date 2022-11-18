package com.smartsparrow.rtm.message.handler.courseware;

import static com.smartsparrow.rtm.RTMWebSocketTestUtils.mockSession;
import static com.smartsparrow.rtm.message.handler.courseware.GetCoursewareSupportStructureMessageHandler.SUPPORT_COURSEWARE_STRUCTURE_GET_ERROR;
import static com.smartsparrow.rtm.message.handler.courseware.GetCoursewareSupportStructureMessageHandler.SUPPORT_COURSEWARE_STRUCTURE_GET_OK;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.util.Collections;
import java.util.UUID;

import org.eclipse.jetty.websocket.api.Session;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.smartsparrow.courseware.data.CoursewareElementType;
import com.smartsparrow.courseware.data.tree.CoursewareElementNode;
import com.smartsparrow.courseware.service.CoursewareElementStructureService;
import com.smartsparrow.exception.IllegalArgumentFault;
import com.smartsparrow.rtm.MessageHandlerTestUtils;
import com.smartsparrow.rtm.message.recv.courseware.GetCoursewareElementStructureMessage;

import reactor.core.publisher.Mono;
import reactor.test.publisher.TestPublisher;

public class GetCoursewareSupportStructureMessageHandlerTest {

    @InjectMocks
    private GetCoursewareSupportStructureMessageHandler handler;
    @Mock
    private CoursewareElementStructureService structureService;
    private Session session;
    @Mock
    private GetCoursewareElementStructureMessage message;

    private static final UUID elementId = UUID.randomUUID();
    private static final CoursewareElementType elementType = CoursewareElementType.ACTIVITY;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.initMocks(this);
        session = mockSession();

        when(message.getElementId()).thenReturn(elementId);
        when(message.getElementType()).thenReturn(elementType);
        when(message.getFieldNames()).thenReturn(Collections.emptyList());
    }

    @Test
    void validate_noElementId() {
        when(message.getElementId()).thenReturn(null);

        IllegalArgumentFault ex = assertThrows(IllegalArgumentFault.class, () -> handler.validate(message));
        assertEquals("elementId is required", ex.getMessage());
    }

    @Test
    void validate_noElementType() {
        when(message.getElementType()).thenReturn(null);

        IllegalArgumentFault ex = assertThrows(IllegalArgumentFault.class, () -> handler.validate(message));
        assertEquals("elementType is required", ex.getMessage());
    }

    @Test
    void handle() throws IOException {
        CoursewareElementNode elementNode = new CoursewareElementNode()
                .setElementId(elementId)
                .setType(elementType)
                .setParentId(null)
                .setTopParentId(elementId);
        when(structureService.getCoursewareElementStructure(eq(elementId), eq(elementType), eq(Collections.emptyList())))
                .thenReturn(Mono.just(elementNode));

        handler.handle(session, message);

        MessageHandlerTestUtils.verifySentMessage(session, response -> {
            assertAll(() -> {
                assertEquals(SUPPORT_COURSEWARE_STRUCTURE_GET_OK, response.getType());
            });
        });
    }

    @Test
    void handle_exception() throws IOException {
        TestPublisher<CoursewareElementNode> error = TestPublisher.create();
        error.error(new RuntimeException("can't fetch structure"));
        when(structureService.getCoursewareElementStructure(eq(elementId), eq(elementType), eq(Collections.emptyList())))
                .thenReturn(error.mono());

        handler.handle(session, message);

        MessageHandlerTestUtils.verifySentMessage(session, "{\"type\":\"" + SUPPORT_COURSEWARE_STRUCTURE_GET_ERROR + "\",\"code\":422," +
                "\"message\":\"error fetching support courseware element structure\"}");
    }
}

