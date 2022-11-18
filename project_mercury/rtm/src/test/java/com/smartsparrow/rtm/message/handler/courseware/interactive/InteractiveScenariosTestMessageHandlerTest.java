package com.smartsparrow.rtm.message.handler.courseware.interactive;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.UUID;

import org.eclipse.jetty.websocket.api.Session;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.smartsparrow.courseware.data.CoursewareElementType;
import com.smartsparrow.courseware.service.WalkableService;
import com.smartsparrow.eval.data.TestEvaluationResponse;
import com.smartsparrow.exception.IllegalArgumentFault;
import com.smartsparrow.rtm.RTMWebSocketTestUtils;
import com.smartsparrow.rtm.lang.WriteResponseException;
import com.smartsparrow.rtm.message.recv.courseware.interactive.InteractiveScenariosTestMessage;

import reactor.core.publisher.Mono;
import reactor.test.publisher.TestPublisher;

class InteractiveScenariosTestMessageHandlerTest {

    @InjectMocks
    private InteractiveScenariosTestMessageHandler handler;

    @Mock
    private InteractiveScenariosTestMessage message;

    @Mock
    private WalkableService walkableService;

    private static final UUID interactiveId = UUID.randomUUID();
    private static final String data = "{\"scope\":\"data\"}";
    private static final Session session = RTMWebSocketTestUtils.mockSession();

    @BeforeEach
    void setUp() {
        MockitoAnnotations.initMocks(this);

        when(message.getInteractiveId()).thenReturn(interactiveId);
        when(message.getScopeData()).thenReturn(data);
    }

    @Test
    void validate_nullInteractiveId() {
        when(message.getInteractiveId()).thenReturn(null);

        IllegalArgumentFault f = assertThrows(IllegalArgumentFault.class, () -> handler.validate(message));

        assertNotNull(f);
        assertEquals("interactiveId is required", f.getMessage());
    }

    @Test
    void validate_nullData() {
        when(message.getScopeData()).thenReturn(null);

        IllegalArgumentFault f = assertThrows(IllegalArgumentFault.class, () -> handler.validate(message));

        assertNotNull(f);
        assertEquals("scopeData is required", f.getMessage());
    }

    @Test
    void validate_emptyData() {
        when(message.getScopeData()).thenReturn("");

        IllegalArgumentFault f = assertThrows(IllegalArgumentFault.class, () -> handler.validate(message));

        assertNotNull(f);
        assertEquals("scopeData is required", f.getMessage());
    }

    @Test
    void validate_success() {
        assertDoesNotThrow(() -> {
            handler.validate(message);
        });
    }

    @Test
    void handle_fail() throws WriteResponseException {
        when(walkableService.evaluate(interactiveId, CoursewareElementType.INTERACTIVE, data))
                .thenReturn(Mono.just(new TestEvaluationResponse()
                        .setScenarioEvaluationResults(new ArrayList<>())));

        handler.handle(session, message);

        String expected = "{" +
                "\"type\":\"author.interactive.scenarios.test.ok\"," +
                "\"response\":{\"results\":[]}}";

        verify(session.getRemote(), atLeastOnce()).sendStringByFuture(expected);
        verify(walkableService).evaluate(interactiveId, CoursewareElementType.INTERACTIVE, data);
    }

    @Test
    void handle_success() throws WriteResponseException {
        TestPublisher<TestEvaluationResponse> publisher = TestPublisher.create();
        publisher.error(new RuntimeException("error!"));

        when(walkableService.evaluate(interactiveId, CoursewareElementType.INTERACTIVE, data))
                .thenReturn(publisher.mono());

        handler.handle(session, message);

        String expected = "{" +
                "\"type\":\"author.interactive.scenarios.test.error\"," +
                "\"code\":422," +
                "\"message\":\"could not evaluate test scenarios\"}";

        verify(session.getRemote(), atLeastOnce()).sendStringByFuture(expected);
        verify(walkableService).evaluate(interactiveId, CoursewareElementType.INTERACTIVE, data);
    }
}