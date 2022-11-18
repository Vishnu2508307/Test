package com.smartsparrow.rtm.message.handler.courseware.scenario;

import static com.smartsparrow.rtm.MessageHandlerTestUtils.verifySentMessage;
import static com.smartsparrow.rtm.message.handler.courseware.scenario.ListScenarioMessageHandler.AUTHOR_SCENARIO_LIST_ERROR;
import static com.smartsparrow.rtm.message.handler.courseware.scenario.ListScenarioMessageHandler.AUTHOR_SCENARIO_LIST_OK;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.eq;
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

import com.smartsparrow.courseware.data.Scenario;
import com.smartsparrow.courseware.data.ScenarioLifecycle;
import com.smartsparrow.courseware.service.ScenarioService;
import com.smartsparrow.rtm.RTMWebSocketTestUtils;
import com.smartsparrow.rtm.lang.RTMValidationException;
import com.smartsparrow.rtm.message.recv.courseware.scenario.ListScenarioMessage;

import reactor.core.publisher.Flux;
import reactor.test.publisher.TestPublisher;

class ListScenarioMessageHandlerTest {

    @Mock
    private ScenarioService scenarioService;

    @InjectMocks
    private ListScenarioMessageHandler handler;

    private Session session;
    @Mock
    private ListScenarioMessage message;

    private static final UUID parentId = UUID.randomUUID();
    private static final ScenarioLifecycle lifecycle = ScenarioLifecycle.ACTIVITY_COMPLETE;


    @BeforeEach
    void setUp() {
        MockitoAnnotations.initMocks(this);
        session = RTMWebSocketTestUtils.mockSession();

        when(message.getParentId()).thenReturn(parentId);
        when(message.getLifecycle()).thenReturn(lifecycle);
    }

    @Test
    void validate_noParentId() {
        when(message.getParentId()).thenReturn(null);

        RTMValidationException error = assertThrows(RTMValidationException.class, () -> handler.validate(message));
        assertEquals("missing parentId", error.getErrorMessage());
        assertEquals(400, error.getStatusCode());
        assertEquals(AUTHOR_SCENARIO_LIST_ERROR, error.getType());
    }

    @Test
    void validate_noLifecycle() {
        when(message.getLifecycle()).thenReturn(null);

        RTMValidationException error = assertThrows(RTMValidationException.class, () -> handler.validate(message));
        assertEquals("missing lifecycle", error.getErrorMessage());
        assertEquals(400, error.getStatusCode());
        assertEquals(AUTHOR_SCENARIO_LIST_ERROR, error.getType());
    }

    @Test
    void validate() throws RTMValidationException {
        handler.validate(message);
    }

    @Test
    void handle() throws IOException {
        Scenario scenario1 = new Scenario().setId(UUID.randomUUID()).setName("scenario 1").setLifecycle(lifecycle);
        Scenario scenario2 = new Scenario().setId(UUID.randomUUID()).setName("scenario 2").setLifecycle(lifecycle);
        when(scenarioService.findAll(eq(parentId), eq(lifecycle))).thenReturn(Flux.just(scenario1, scenario2));

        handler.handle(session, message);

        verifySentMessage(session, response -> assertAll(() -> {
            assertEquals(AUTHOR_SCENARIO_LIST_OK, response.getType());
            List scenarios = ((List) response.getResponse().get("scenarios"));
            assertEquals(2, scenarios.size());
            Map scenario = (Map) (scenarios.get(0));
            assertEquals(scenario1.getId().toString(), scenario.get("id"));
            assertEquals("scenario 1", scenario.get("name"));
            assertEquals(lifecycle.name(), scenario.get("lifecycle"));
            scenario = (Map) (scenarios.get(1));
            assertEquals(scenario2.getId().toString(), scenario.get("id"));
            assertEquals("scenario 2", scenario.get("name"));
            assertEquals(lifecycle.name(), scenario.get("lifecycle"));
        }));
    }

    @Test
    void handle_noScenarios() throws IOException {
        when(scenarioService.findAll(eq(parentId), eq(lifecycle))).thenReturn(Flux.empty());

        handler.handle(session, message);

        verifySentMessage(session, response -> assertAll(() -> {
            assertEquals(AUTHOR_SCENARIO_LIST_OK, response.getType());
            List scenarios = ((List) response.getResponse().get("scenarios"));
            assertEquals(0, scenarios.size());
        }));
    }

    @SuppressWarnings("unchecked")
    @Test
    void handle_exception() throws IOException {
        Flux flux = TestPublisher.create().error(new RuntimeException("someException")).flux();
        when(scenarioService.findAll(eq(parentId), eq(lifecycle))).thenReturn(flux);

        handler.handle(session, message);

        verifySentMessage(session, "{\"type\":\"" + AUTHOR_SCENARIO_LIST_ERROR + "\",\"code\":400," +
                "\"message\":\"error to fetch list of scenarios\"}");
    }
}
