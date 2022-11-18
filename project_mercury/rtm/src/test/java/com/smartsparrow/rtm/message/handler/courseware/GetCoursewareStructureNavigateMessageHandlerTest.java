package com.smartsparrow.rtm.message.handler.courseware;

import com.smartsparrow.courseware.data.CoursewareElementType;
import com.smartsparrow.courseware.data.tree.CoursewareElementNode;
import com.smartsparrow.courseware.service.CoursewareElementStructureNavigateService;
import com.smartsparrow.courseware.service.CoursewareElementStructureService;
import com.smartsparrow.exception.IllegalArgumentFault;
import com.smartsparrow.rtm.MessageHandlerTestUtils;
import com.smartsparrow.rtm.message.recv.courseware.GetCoursewareElementStructureMessage;
import com.smartsparrow.rtm.message.recv.courseware.GetCoursewareElementStructureNavigateMessage;
import org.eclipse.jetty.websocket.api.Session;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import reactor.core.publisher.Mono;
import reactor.test.publisher.TestPublisher;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import static com.smartsparrow.rtm.RTMWebSocketTestUtils.mockSession;
import static com.smartsparrow.rtm.message.handler.courseware.GetCoursewareStructureNavigateMessageHandler.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

public class GetCoursewareStructureNavigateMessageHandlerTest {

    @InjectMocks
    private GetCoursewareStructureNavigateMessageHandler handler;
    @Mock
    private CoursewareElementStructureNavigateService structureService;
    private Session session;
    @Mock
    private GetCoursewareElementStructureNavigateMessage message;

    private static final UUID elementId = UUID.randomUUID();
    private static final CoursewareElementType elementType = CoursewareElementType.ACTIVITY;
    private static final List<String> fieldNames = Arrays.asList("title", "description");

    @BeforeEach
    void setUp() {
        MockitoAnnotations.initMocks(this);
        session = mockSession();

        when(message.getElementId()).thenReturn(elementId);
        when(message.getElementType()).thenReturn(elementType);
        when(message.getFieldNames()).thenReturn(fieldNames);
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
        when(structureService.getCoursewareElementStructure(eq(elementId), eq(elementType), eq(fieldNames)))
                .thenReturn(Mono.just(elementNode));

        handler.handle(session, message);

        MessageHandlerTestUtils.verifySentMessage(session, response -> {
            assertAll(() -> {
                assertEquals(COURSEWARE_STRUCTURE_NAVIGATE_OK, response.getType());
            });
        });
    }

    @Test
    void handle_exception() throws IOException {
        TestPublisher<CoursewareElementNode> error = TestPublisher.create();
        error.error(new RuntimeException("can't fetch structure"));
        when(structureService.getCoursewareElementStructure(eq(elementId), eq(elementType), eq(fieldNames)))
                .thenReturn(error.mono());

        handler.handle(session, message);

        MessageHandlerTestUtils.verifySentMessage(session, "{\"type\":\"" + COURSEWARE_STRUCTURE_NAVIGATE_ERROR + "\",\"code\":422," +
                "\"message\":\"error fetching courseware element navigate structure\"}");
    }
}

