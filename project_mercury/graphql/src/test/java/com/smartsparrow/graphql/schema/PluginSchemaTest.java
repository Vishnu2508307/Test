package com.smartsparrow.graphql.schema;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.smartsparrow.courseware.data.Activity;
import com.smartsparrow.courseware.data.PluginReference;
import com.smartsparrow.plugin.lang.PluginNotFoundFault;
import com.smartsparrow.plugin.lang.VersionParserFault;
import com.smartsparrow.plugin.payload.PluginPayload;
import com.smartsparrow.plugin.service.PluginService;

import reactor.core.publisher.Mono;
import reactor.test.publisher.TestPublisher;

class PluginSchemaTest {

    @InjectMocks
    private PluginSchema pluginSchema;

    @Mock
    private PluginService pluginService;

    private static final UUID pluginId = UUID.randomUUID();
    private static final PluginReference pluginRef = new Activity().setPluginId(pluginId).setPluginVersionExpr("1.0.0");

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void getPlugin_noView() {
        when(pluginService.findPlugin(pluginId, "1.0.0")).thenReturn(Mono.just(new PluginPayload()));

        PluginPayload result = pluginSchema.getPlugin(pluginRef, null).join();

        assertNotNull(result);
    }

    @Test
    void getPlugin_withView() {
        when(pluginService.findPluginByIdAndView(pluginId, "LEARNER", "1.0.0")).thenReturn(Mono.just(new PluginPayload()));

        PluginPayload result = pluginSchema.getPlugin(pluginRef, "LEARNER").join();

        assertNotNull(result);
    }

    @Test
    void getPlugin_withView_notFound() {
        Mono<PluginPayload> pluginMono = TestPublisher.<PluginPayload>create().error(new PluginNotFoundFault(pluginId)).mono();
        when(pluginService.findPluginByIdAndView(pluginId, "LEARNER", "1.0.0")).thenReturn(pluginMono);

        pluginSchema.getPlugin(pluginRef, "LEARNER").handle((pluginPayload, throwable) -> {
            assertEquals(PluginNotFoundFault.class, throwable.getClass());
            return pluginPayload;
        }).join();
    }

    @Test
    void getPlugin_noViewView_InvalidVersion() {
        Mono<PluginPayload> pluginMono = TestPublisher.<PluginPayload>create().error(new VersionParserFault("unable to parse plugin version")).mono();
        when(pluginService.findPlugin(pluginId, "1.0.0")).thenReturn(pluginMono);

        pluginSchema.getPlugin(pluginRef, null).handle((pluginPayload, throwable) -> {
            assertEquals(VersionParserFault.class, throwable.getClass());
            return pluginPayload;
        }).join();
    }
}
