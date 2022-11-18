package mercury.glue.hook;

import static mercury.glue.wiring.CitrusConfiguration.LEARN_REQUEST_URL;
import static mercury.glue.wiring.CitrusConfiguration.WEB_SOCKET_CLIENT_REGISTRY;
import static mercury.glue.wiring.CitrusConfiguration.WEB_SOCKET_REQUEST_URL;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.test.context.ContextConfiguration;

import com.consol.citrus.Citrus;
import com.consol.citrus.annotations.CitrusFramework;
import com.consol.citrus.annotations.CitrusResource;
import com.consol.citrus.config.CitrusSpringConfig;
import com.consol.citrus.cucumber.CitrusLifecycleHooks;
import com.consol.citrus.dsl.runner.TestRunner;
import com.consol.citrus.websocket.client.WebSocketClient;

import cucumber.api.java.After;
import cucumber.api.java.Before;
import mercury.glue.wiring.CitrusConfiguration;
import mercury.glue.wiring.WebSocketClientRegistry;

/**
 * Add a new web socket connection to the registry before each scenario and remove all connections after each scenario.
 * <br/>
 * Execution order is defined by {@link HooksOrder#WEB_SOCKET_CLIENT_BEFORE} and {@link HooksOrder#WEB_SOCKET_CLIENT_AFTER}
 */
@ContextConfiguration(classes = {CitrusSpringConfig.class, CitrusConfiguration.class})
public class WebSocketClientHook {

    @CitrusResource
    private TestRunner runner;
    @CitrusFramework
    private Citrus citrus;

    @Autowired
    @Qualifier(WEB_SOCKET_REQUEST_URL)
    private String requestUrl;

    @Autowired
    @Qualifier(LEARN_REQUEST_URL)
    private String learnUrl;

    @Autowired
    @Qualifier(WEB_SOCKET_CLIENT_REGISTRY)
    private WebSocketClientRegistry webSocketClientRegistry;

    /**
     * Returns a web socket connection endpoint for the current scenario.
     */
    public WebSocketClient getEndpoint() {
        return webSocketClientRegistry.get(WebSocketClientRegistry.DEFAULT_WEB_SOCKET_CLIENT);
    }

    /**
     * Add a new web socket client to the registry and opens the connection
     * <br/>
     * Should be run after Jetty server is started and before {@link CitrusLifecycleHooks}
     * <br/>
     * Execution order is defined by {@link HooksOrder#WEB_SOCKET_CLIENT_BEFORE}
     */
    @Before(order = HooksOrder.WEB_SOCKET_CLIENT_BEFORE, value = {"not @noSocket", "not @Learn"})
    public void open() {
        webSocketClientRegistry.add(runner, WebSocketClientRegistry.DEFAULT_WEB_SOCKET_CLIENT, requestUrl);
    }

    /**
     * Open a new web socket connection for learn flow. It opens for scenarios marked with @Learn annotation.
     */
    @Before(order = HooksOrder.WEB_SOCKET_CLIENT_BEFORE, value = {"not @noSocket", "@Learn"})
    public void openLearn() {
        webSocketClientRegistry.add(runner, WebSocketClientRegistry.DEFAULT_WEB_SOCKET_CLIENT, learnUrl);
    }

    /**
     * Remove all registered connection from the registry
     * <br/>
     * Should be run after {@link CitrusLifecycleHooks}.
     * Execution order is defined by {@link HooksOrder#WEB_SOCKET_CLIENT_AFTER}
     */
    @SuppressWarnings("unchecked")
    @After(order = HooksOrder.WEB_SOCKET_CLIENT_AFTER, value = "not @noSocket")
    public void close() {
        webSocketClientRegistry.removeAll();
    }
}
