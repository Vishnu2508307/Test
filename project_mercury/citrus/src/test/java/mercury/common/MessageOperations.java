package mercury.common;

import static mercury.glue.wiring.CitrusConfiguration.WEB_SOCKET_CLIENT_REGISTRY;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

import com.consol.citrus.actions.AbstractTestAction;
import com.consol.citrus.actions.ReceiveMessageAction;
import com.consol.citrus.actions.ReceiveTimeoutAction;
import com.consol.citrus.actions.SendMessageAction;
import com.consol.citrus.context.TestContext;
import com.consol.citrus.dsl.builder.BuilderSupport;
import com.consol.citrus.dsl.builder.ReceiveMessageBuilder;
import com.consol.citrus.dsl.runner.TestRunner;
import com.consol.citrus.message.MessageType;

import mercury.glue.wiring.WebSocketClientRegistry;

public class MessageOperations {

    @Autowired
    @Qualifier(WEB_SOCKET_CLIENT_REGISTRY)
    private WebSocketClientRegistry webSocketClientRegistry;

    /**
     * Send a message to the webSocket server.
     * @param runner the citrus {@link TestRunner}
     * @param payload the message payload.
     * @param clientName the name of the client that should send the message.
     * @throws com.consol.citrus.exceptions.CitrusRuntimeException if the client name is not found in the registry.
     * @return a {@link SendMessageAction}
     */
    public SendMessageAction sendJSON(TestRunner runner, final String payload, final String clientName) {
        return runner.send(action -> action.endpoint(webSocketClientRegistry.get(clientName))
                .messageType(MessageType.JSON)
                .payload(payload));
    }

    /**
     * Send a message to the webSocket server using the default {@link WebSocketClientRegistry#DEFAULT_WEB_SOCKET_CLIENT}
     * @param runner the citrus {@link TestRunner}
     * @param payload the message payload.
     * @throws com.consol.citrus.exceptions.CitrusRuntimeException when the client name is not found in the registry.
     * the default should always be available. If exception is thrown here that indicates wrong configuration settings
     * in the wiring package or improper use of the registry.
     * @return a {@link SendMessageAction}
     */
    public SendMessageAction sendJSON(TestRunner runner, String payload) {
        return sendJSON(runner, payload, WebSocketClientRegistry.DEFAULT_WEB_SOCKET_CLIENT);
    }

    /**
     * Send a GraphQL RTM message
     * @param runner the Citrus Runner instance
     * @param query the query
     */
    public SendMessageAction sendGraphQL(TestRunner runner, String query) {
        PayloadBuilder payload = new PayloadBuilder();
        payload.addField("type", "graphql.query");
        payload.addField("query", query);

        return sendJSON(runner, payload.build(), WebSocketClientRegistry.DEFAULT_WEB_SOCKET_CLIENT);
    }

    /**
     * Receive a message from the webSocket server.
     * @param runner the citrus {@link TestRunner}
     * @param configurer the configured action
     * @param clientName the name of the client that should receive the message
     * @throws com.consol.citrus.exceptions.CitrusRuntimeException when the client name is not found in the registry.
     * @return a {@link ReceiveMessageAction}
     */
    public ReceiveMessageAction receiveJSON(TestRunner runner, final BuilderSupport<ReceiveMessageBuilder> configurer,
                                            final String clientName) {
        return runner.receive(action ->
                configurer.configure(action.endpoint(webSocketClientRegistry.get(clientName))
                        .messageType(MessageType.JSON)));
    }

    /**
     * Receive a message from the webSocket server using the default {@link WebSocketClientRegistry#DEFAULT_WEB_SOCKET_CLIENT}
     * @param runner the citrus {@link TestRunner}
     * @param configurer the configured action
     * @throws com.consol.citrus.exceptions.CitrusRuntimeException when the client name is not found in the registry.
     * the default should always be available. If exception is thrown here that indicates wrong configuration settings
     * in the wiring package or improper use of the registry.
     * @return a {@link ReceiveMessageAction}
     */
    public ReceiveMessageAction receiveJSON(TestRunner runner, final BuilderSupport<ReceiveMessageBuilder> configurer) {
        return receiveJSON(runner, configurer, WebSocketClientRegistry.DEFAULT_WEB_SOCKET_CLIENT);
    }

    /**
     * Validate the response message type.
     * @param runner the citrus {@link TestRunner}
     * @param configurer the configured action
     * @param type the type to validate
     * @param clientName the name of the client that should validate the type.
     * @throws com.consol.citrus.exceptions.CitrusRuntimeException when the client name is not found in the registry.
     * @return a {@link ReceiveMessageAction}
     */
    public ReceiveMessageAction validateResponseType(TestRunner runner, final String type, final String clientName,
                                                      final BuilderSupport<ReceiveMessageBuilder> configurer) {
        return receiveJSON(runner, action ->
                configurer.configure(action.jsonPath("$.type", type)), clientName);
    }

    /**
     * Validate the response message type using the default {@link WebSocketClientRegistry#DEFAULT_WEB_SOCKET_CLIENT}
     * @param runner the citrus {@link TestRunner}
     * @param type the type to validate
     * @param configurer the configured action
     * @return a {@link ReceiveMessageAction}
     */
    public ReceiveMessageAction validateResponseType(TestRunner runner, final String type,
                                                     final BuilderSupport<ReceiveMessageBuilder> configurer) {
        return validateResponseType(runner, type, WebSocketClientRegistry.DEFAULT_WEB_SOCKET_CLIENT, configurer);
    }

    /**
     * Validate the response message type using the default {@link WebSocketClientRegistry#DEFAULT_WEB_SOCKET_CLIENT}
     * and no configurer.
     * @param runner the citrus {@link TestRunner}
     * @param type the type to validate
     * @throws com.consol.citrus.exceptions.CitrusRuntimeException when the client name is not found in the registry.
     * the default should always be available. If exception is thrown here that indicates wrong configuration settings
     * in the wiring package or improper use of the registry.
     * @return a {@link ReceiveMessageAction}
     */
    public ReceiveMessageAction validateResponseType(TestRunner runner, final String type) {
        return validateResponseType(runner, type, action ->{});
    }

    /**
     * Validates that there are no messages on endpoint
     * @param runner Citrus runner
     * @param clientName endpoint name
     */
    public ReceiveTimeoutAction receiveTimeout(TestRunner runner, final String clientName) {
        return runner.receiveTimeout(action ->
                action.endpoint(webSocketClientRegistry.get(clientName)));
    }

    /**
     * Validates that there are no messages on endpoint
     * @param runner Citrus runner
     */
    public ReceiveTimeoutAction receiveTimeout(TestRunner runner) {
        return runner.receiveTimeout(action -> {
                    action.endpoint(webSocketClientRegistry.get(WebSocketClientRegistry.DEFAULT_WEB_SOCKET_CLIENT));
                }
        );
    }

    /**
     * Run an empty test action to access the {@link TestContext}
     *
     * @param runner the citrus test runner
     * @return the runner test context
     */
    public TestContext getTestContext(TestRunner runner) {
        final TestContext[] testContext = {null};
        runner.run(new AbstractTestAction() {
            @Override
            public void doExecute(TestContext context) {
                testContext[0] = context;
            }
        });
        return testContext[0];
    }
}
