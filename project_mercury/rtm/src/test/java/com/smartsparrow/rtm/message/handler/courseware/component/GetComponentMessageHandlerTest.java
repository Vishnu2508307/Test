package com.smartsparrow.rtm.message.handler.courseware.component;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.UUID;

import com.smartsparrow.courseware.data.CoursewareElementDescription;
import org.eclipse.jetty.websocket.api.Session;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.smartsparrow.courseware.data.Component;
import com.smartsparrow.courseware.data.ParentByComponent;
import com.smartsparrow.courseware.lang.ComponentNotFoundException;
import com.smartsparrow.courseware.payload.ComponentPayload;
import com.smartsparrow.courseware.service.ComponentService;
import com.smartsparrow.exception.IllegalArgumentFault;
import com.smartsparrow.plugin.data.PluginSummary;
import com.smartsparrow.rtm.RTMWebSocketTestUtils;
import com.smartsparrow.rtm.lang.WriteResponseException;
import com.smartsparrow.rtm.message.recv.courseware.component.ComponentMessage;

import reactor.core.publisher.Mono;
import reactor.test.publisher.TestPublisher;

class GetComponentMessageHandlerTest {

    @InjectMocks
    private GetComponentMessageHandler handler;

    @Mock
    private ComponentService componentService;

    private ComponentMessage message;
    private static final UUID componentId = UUID.randomUUID();
    private static final String messageId = "messageId";
    private static final Session session = RTMWebSocketTestUtils.mockSession();

    @BeforeEach
    void setUp() {
        MockitoAnnotations.initMocks(this);

        message = mock(ComponentMessage.class);

        when(message.getComponentId()).thenReturn(componentId);
        when(message.getId()).thenReturn(messageId);
    }

    @Test
    void validate_noComponentId() {
        when(message.getComponentId()).thenReturn(null);

        IllegalArgumentFault ex = assertThrows(IllegalArgumentFault.class, () -> handler.validate(message));
        assertEquals("componentId is missing", ex.getMessage());
        assertEquals("BAD_REQUEST", ex.getType());
        assertEquals(400, ex.getResponseStatusCode());
    }

    @Test
    void validate_componentNotFound() {
        when(componentService.findById(message.getComponentId())).thenReturn(Mono.empty());

        IllegalArgumentFault t = assertThrows(IllegalArgumentFault.class, () -> handler.validate(message));
        assertEquals("Component not found", t.getMessage());
        assertEquals("BAD_REQUEST", t.getType());
        assertEquals(400, t.getResponseStatusCode());
    }

    @Test
    void handle_error() throws WriteResponseException {
        TestPublisher<ComponentPayload> payloadPublisher = TestPublisher.create();
        payloadPublisher.error(new ComponentNotFoundException(componentId));

        when(componentService.getComponentPayload(componentId)).thenReturn(payloadPublisher.mono());

        handler.handle(session, message);

        String expected = "{" +
                            "\"type\":\"author.component.get.error\"," +
                            "\"code\":422," +
                            "\"message\":\"error fetching component "+componentId+"\"," +
                            "\"replyTo\":\"messageId\"}";

        verify(session.getRemote(), atLeastOnce()).sendStringByFuture(expected);
    }

    @Test
    void handle_success() throws WriteResponseException {
        Component component = new Component()
                .setId(componentId);

        ComponentPayload payload = ComponentPayload.from(component, "config", new PluginSummary(), new ParentByComponent(),
                                                        new CoursewareElementDescription(), new ArrayList<>());

        when(componentService.getComponentPayload(componentId)).thenReturn(Mono.just(payload));

        handler.handle(session, message);

        String expected = "{" +
                            "\"type\":\"author.component.get.ok\"," +
                            "\"response\":{" +
                                "\"component\":{" +
                                    "\"componentId\":\""+componentId+"\"," +
                                    "\"config\":\"config\"," +
                                    "\"plugin\":{}" +
                                "}" +
                            "},\"replyTo\":\"messageId\"}";

        verify(session.getRemote(), atLeastOnce()).sendStringByFuture(expected);
    }
}
