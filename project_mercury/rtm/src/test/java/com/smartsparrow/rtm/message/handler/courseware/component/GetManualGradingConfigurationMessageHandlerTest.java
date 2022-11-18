package com.smartsparrow.rtm.message.handler.courseware.component;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
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

import com.smartsparrow.courseware.data.ManualGradingConfiguration;
import com.smartsparrow.courseware.service.ComponentService;
import com.smartsparrow.exception.IllegalArgumentFault;
import com.smartsparrow.rtm.RTMWebSocketTestUtils;
import com.smartsparrow.rtm.lang.WriteResponseException;
import com.smartsparrow.rtm.message.recv.courseware.component.ComponentMessage;

import reactor.core.publisher.Mono;
import reactor.test.publisher.TestPublisher;

class GetManualGradingConfigurationMessageHandlerTest {

    @InjectMocks
    private GetManualGradingConfigurationMessageHandler handler;

    @Mock
    private ComponentService componentService;

    @Mock
    private ComponentMessage componentMessage;

    private static final UUID componentId = UUID.randomUUID();
    private static final Session session = RTMWebSocketTestUtils.mockSession();

    @BeforeEach
    void setUp() {
        MockitoAnnotations.initMocks(this);

        when(componentMessage.getComponentId()).thenReturn(componentId);
        when(componentService.findManualGradingConfiguration(componentId)).thenReturn(Mono.just(new ManualGradingConfiguration()
                .setComponentId(componentId)));

    }

    @Test
    void validate_nullComponentId() {
        when(componentMessage.getComponentId()).thenReturn(null);
        IllegalArgumentFault e = assertThrows(IllegalArgumentFault.class, () -> handler.validate(componentMessage));

        assertNotNull(e);
        assertEquals("componentId is required", e.getMessage());
    }

    @Test
    void handle_error() throws WriteResponseException {
        TestPublisher<ManualGradingConfiguration> publisher = TestPublisher.create();
        publisher.error(new RuntimeException("wow"));
        when(componentService.findManualGradingConfiguration(componentId)).thenReturn(publisher.mono());

        handler.handle(session, componentMessage);

        String expected = "{" +
                            "\"type\":\"author.component.manual.grading.configuration.get.error\"," +
                            "\"code\":422," +
                            "\"message\":\"wow\"}";

        verify(session.getRemote(), atLeastOnce()).sendStringByFuture(expected);
    }

    @Test
    void handle_empty() throws WriteResponseException {
        when(componentService.findManualGradingConfiguration(componentId)).thenReturn(Mono.empty());

        handler.handle(session, componentMessage);

        String expected = "{" +
                            "\"type\":\"author.component.manual.grading.configuration.get.ok\"," +
                            "\"response\":{" +
                                "\"manualGradingConfiguration\":{}" +
                            "}}";

        verify(session.getRemote(), atLeastOnce()).sendStringByFuture(expected);
    }

    @Test
    void handle_found() throws WriteResponseException {
        handler.handle(session, componentMessage);

        String expected = "{" +
                            "\"type\":\"author.component.manual.grading.configuration.get.ok\"," +
                            "\"response\":{" +
                                "\"manualGradingConfiguration\":{" +
                                    "\"componentId\":\"" + componentId + "\"" +
                                "}" +
                            "}}";

        verify(session.getRemote(), atLeastOnce()).sendStringByFuture(expected);
    }

}