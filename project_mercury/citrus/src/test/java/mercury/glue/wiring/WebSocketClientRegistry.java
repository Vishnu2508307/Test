package mercury.glue.wiring;

import static org.mockito.ArgumentMatchers.anyString;

import java.io.IOException;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

import javax.annotation.Nonnull;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.WebSocketSession;

import com.consol.citrus.dsl.endpoint.CitrusEndpoints;
import com.consol.citrus.dsl.runner.TestRunner;
import com.consol.citrus.exceptions.CitrusRuntimeException;
import com.consol.citrus.message.MessageType;
import com.consol.citrus.websocket.client.WebSocketClient;
import com.consol.citrus.websocket.handler.CitrusWebSocketHandler;
import com.smartsparrow.rtm.ws.RTMWebSocketStatus;

/**
 * A registry that keeps track of all the opened {@link WebSocketClient} connections.
 */
public class WebSocketClientRegistry {

    private static final Logger log = LoggerFactory.getLogger(WebSocketClientRegistry.class);

    public static final String DEFAULT_WEB_SOCKET_CLIENT = "webSocket:client:zero";

    private Map<String, WebSocketClient> registry = new HashMap<>();

    /**
     * Add a new {@link WebSocketClient} to the registry with the supplied name and opens the connection using the
     * {@link TestRunner}.
     * @param runner the test runner object used to check the connection is successfully opened
     * @param name the connection registration name
     * @param requestUrl the connection url
     * @throws CitrusRuntimeException when trying to registering a WebSocketClient with a name that already exists in
     * the registry
     */
    public void add(@Nonnull TestRunner runner, @Nonnull String name, @Nonnull String requestUrl) {
        if (this.registry.containsKey(name)) {
            CitrusRuntimeException c = new CitrusRuntimeException(String.format("WebSocketClient named `%s` already exists", name));
            log.error(c.getMessage(), c);
            throw c;
        }
        this.registry.put(name, buildWebSocketClient(name, runner, requestUrl));
    }

    /**
     * Close all {@link WebSocketClient} connections and remove them from the registry.
     */
    public void removeAll() {

        this.registry.forEach(this::closeConnection);

        this.registry = new HashMap<>();
    }

    /**
     * Build a {@link WebSocketClient} and check that the connection is opened.
     * @param name the name a socket client is registered with
     * @param runner the {@link TestRunner} that checks the connection status
     * @param requestUrl the url a socket should be opened for
     * @return a {@link WebSocketClient}
     */
    private WebSocketClient buildWebSocketClient(String name, TestRunner runner, String requestUrl) {
        //open new web socket before each scenario
        WebSocketClient client = CitrusEndpoints.websocket()
                .client()
                .timeout(10000) //default timeout (5000) is not enough, sometimes messages are handled longer
                // (ex. "iam.instructor.provision") and tests fail before receiving a response
                .requestUrl(requestUrl)
                .build();
        // when a socket connection is open mercury sends an hello message
        // to the client, make sure the hello message is received
        runner.receive(action -> action.endpoint(client)
                .messageType(MessageType.JSON)
                .payload(anyString()));

        log.info("Opening '{}' websocket to {} for {}", name, requestUrl, client.getName());

        return client;
    }

    /**
     * Get the webSocket client given the supplied client name.
     *
     * @param name the webSocket client name
     * @throws CitrusRuntimeException when the name is not found in the registry
     * @return a {@link WebSocketClient}
     */
    public WebSocketClient get(String name) {
        if (this.registry.containsKey(name)) {
            return this.registry.get(name);
        }
        throw new CitrusRuntimeException(String.format("WebSocketClient named `%s` not found in registry", name));
    }

    /**
     * Attempt at closing the {@link WebSocketClient} connection.
     * @param name the connection name
     * @param client the web socket client
     */
    @SuppressWarnings("unchecked")
    private void closeConnection(final String name, final WebSocketClient client) {
        try {
            Field field = CitrusWebSocketHandler.class.getDeclaredField("sessions");
            field.setAccessible(true);
            Map<String, WebSocketSession> sessions =
                    (Map<String, WebSocketSession>) field.get(client.getEndpointConfiguration().getHandler());
            for (WebSocketSession session : sessions.values()) {
                sessions.remove(session.getId());
                session.close(new CloseStatus(RTMWebSocketStatus.NORMAL_CLOSURE.getValue(), "Scenario is finished"));
            }
            log.info("Closing '{}' websocket {}", name, client.getName());
        } catch (IOException | IllegalAccessException | NoSuchFieldException e) {
            log.error(String.format("Could not close connection named `%s`", name), e);
        }
    }
}
