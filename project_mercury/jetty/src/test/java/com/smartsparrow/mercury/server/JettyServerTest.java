package com.smartsparrow.mercury.server;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.concurrent.TimeoutException;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import com.smartsparrow.rtm.wiring.RTMModule;

@Disabled
//todo: this test is failing on GoCD because it needs DB connection to start server. Ignore it and re-do later as Citrus test
class JettyServerTest {

    private JettyServer jettyServer;

    @BeforeEach
    void beforeEach() throws JettyServerBuilderException {
        //hack to load keystore file
        System.setProperty("user.dir", System.getProperty("user.dir") + "/..");

        jettyServer = new JettyServerBuilder()
                .withModules(new RTMModule())
                .build();
        // start up a server
        jettyServer.startNow();
    }

    @Test
    void stop() throws Exception {
        assertTrue(jettyServer.isStarted());
        assertFalse(jettyServer.isStopped());

        jettyServer.stop();
        long timeout = jettyServer.getStopTimeout();

        while (!jettyServer.isStopped()) {
            Thread.sleep(1);
            timeout--;

            if (timeout == 0) {
                throw new TimeoutException("Timeout expired. Could not stop the jetty server");
            }
        }
        assertFalse(jettyServer.isStarted());
        assertTrue(jettyServer.isStopped());
    }
}
