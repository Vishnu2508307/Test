package com.smartsparrow.rtm.message.handler.courseware;

import static com.smartsparrow.rtm.message.handler.courseware.GetCoursewareEvaluableMessageHandler.AUTHOR_EVALUABLE_GET_ERROR;
import static com.smartsparrow.rtm.message.handler.courseware.GetCoursewareEvaluableMessageHandler.AUTHOR_EVALUABLE_GET_OK;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.UUID;

import org.eclipse.jetty.websocket.api.Session;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.smartsparrow.courseware.data.CoursewareElementType;
import com.smartsparrow.courseware.data.EvaluationMode;
import com.smartsparrow.courseware.service.WalkableService;
import com.smartsparrow.exception.IllegalArgumentFault;
import com.smartsparrow.rtm.RTMWebSocketTestUtils;
import com.smartsparrow.rtm.lang.WriteResponseException;
import com.smartsparrow.rtm.message.recv.courseware.GetCoursewareEvaluableMessage;

import reactor.core.publisher.Mono;
import reactor.test.publisher.TestPublisher;

public class GetCoursewareEvaluableMessageHandlerTest {

    private static final Session session = RTMWebSocketTestUtils.mockSession();
    private static final UUID elementId = UUID.randomUUID();
    private static final CoursewareElementType elementType = CoursewareElementType.ACTIVITY;

    @InjectMocks
    private GetCoursewareEvaluableMessageHandler handler;

    @Mock
    private GetCoursewareEvaluableMessage message;

    @Mock
    private WalkableService walkableService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
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
    void handle_success() throws WriteResponseException {
        when(walkableService.fetchEvaluationMode(elementId, elementType))
                .thenReturn(Mono.just(EvaluationMode.DEFAULT));

        handler.handle(session, message);

        String expected = "{" +
                "\"type\":\"" + AUTHOR_EVALUABLE_GET_OK + "\"," +
                "\"response\":{" +
                "\"evaluable\":{" +
                "\"elementId\":\"" + elementId + "\"," +
                "\"elementType\":\"" + CoursewareElementType.ACTIVITY + "\"," +
                "\"evaluationMode\":\"" + EvaluationMode.DEFAULT + "\"}}}";

        verify(session.getRemote(), atLeastOnce()).sendStringByFuture(expected);
    }

    @Test
    void handle_error() throws WriteResponseException {
        TestPublisher<EvaluationMode> publisher = TestPublisher.create();
        publisher.error(new Exception("some error"));
        when(walkableService.fetchEvaluationMode(elementId, elementType))
                .thenReturn(publisher.mono());

        handler.handle(session, message);

        String expected = "{" +
                "\"type\":\"" + AUTHOR_EVALUABLE_GET_ERROR + "\"," +
                "\"code\":422," +
                "\"message\":\"could not fetch evaluable\"}";

        verify(session.getRemote(), atLeastOnce()).sendStringByFuture(expected);
    }
}
