package com.smartsparrow.mercury.server;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;

import org.junit.jupiter.api.Test;

import com.google.inject.Module;
import com.smartsparrow.data.InstanceType;

class JettyServerBuilderTest {

    @Test
    void build_default() {
        JettyServer jettyServer = new JettyServerBuilder().withModules(mock(Module.class)).build();
        assertEquals(8080, jettyServer.getPort());
    }

    @Test
    void build_custom() throws JettyServerBuilderException {
        int customPort = 9090;
        JettyServer jettyServer = new JettyServerBuilder()
                .setPort(customPort)
                .setShutdownTimeout(50000)
                .withModules(mock(Module.class))
                .build();
        assertEquals(customPort, jettyServer.getPort());
        assertEquals(50000, jettyServer.getStopTimeout());
    }

    @Test
    void build_noModules() {
        assertThrows(NullPointerException.class, () -> new JettyServerBuilder().build());
    }

    @Test
    void instanceType_unknown() {
        String type = "foo";

        JettyServerBuilderException e = assertThrows(JettyServerBuilderException.class,
                () -> new JettyServerBuilder(type));

        assertEquals("invalid instance type foo", e.getMessage());
    }

    @Test
    void instanceType_null() {
        JettyServerBuilderException e = assertThrows(JettyServerBuilderException.class,
                () -> new JettyServerBuilder(null));

        assertEquals("invalid instance type null", e.getMessage());
    }

    @Test
    void instanceType_valid() {
        assertDoesNotThrow(() -> {
            JettyServerBuilder builder = new JettyServerBuilder("learner");
            assertEquals(InstanceType.LEARNER, builder.getInstanceType());
        });
    }

    @Test
    void instanceType_empty() {
        JettyServerBuilderException e = assertThrows(JettyServerBuilderException.class,
                () -> new JettyServerBuilder(""));
        assertEquals("invalid instance type ", e.getMessage());
    }

    @Test
    void instanceType_definedWhenEmptyConstructor() {
        JettyServerBuilder builder = new JettyServerBuilder();
        assertEquals(InstanceType.DEFAULT, builder.getInstanceType());
    }
}
