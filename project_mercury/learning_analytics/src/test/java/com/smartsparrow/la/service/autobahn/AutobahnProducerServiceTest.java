package com.smartsparrow.la.service.autobahn;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.pearson.autobahn.common.domain.ActionType;
import com.pearson.autobahn.common.domain.Environment;
import com.pearson.autobahn.common.domain.OperationalType;
import com.pearson.autobahn.common.domain.StreamType;
import com.pearson.autobahn.common.exception.AutobahnIdentityProviderException;
import com.pearson.autobahn.common.exception.InitializationException;
import com.pearson.autobahn.common.exception.PublishException;
import com.pearson.autobahn.common.exception.SchemaNotFoundException;
import com.pearson.autobahn.common.exception.SchemaValidationException;
import com.pearson.autobahn.common.sdk.auth.impl.MockPiAutobahnIdentityProvider;
import com.pearson.autobahn.producersdk.config.AutobahnProducerConfig;
import com.pearson.autobahn.producersdk.domain.ProducerMessage;
import com.smartsparrow.la.config.ProducerConfig;

public class AutobahnProducerServiceTest {

    @InjectMocks
    AutobahnProducerService autobahnProducerService;

    @Mock
    ProducerConfig producerConfig;

    private MockPiAutobahnIdentityProvider identityProvider;

    private final static String namespace = "namespace";
    private final static String messageVersion = "1.1.0";
    private final static String messageTypeCode = "Bronte";
    private final static StreamType stream = StreamType.ACTIVITY;
    private final static ActionType actionType = ActionType.CREATE;
    private final static String correlationId = UUID.randomUUID().toString();
    private final static String payload = "some payload here";
    private final static Map<String, String> routingTags = new HashMap<>();
    ProducerMessage message = new ProducerMessage(namespace, messageTypeCode, messageVersion,
            stream, correlationId, actionType, payload, routingTags);

    @BeforeEach
    void setUp() throws AutobahnIdentityProviderException {
        MockitoAnnotations.initMocks(this);
        identityProvider = new MockPiAutobahnIdentityProvider("", "", Environment.PRD);
    }

    @Test
    void test_config_pi_env_mismatch() throws IllegalArgumentException {
        String expectedMessage = "Your Producer SDK environment INT doesn't match with the Identity Provider environment PRD. Both MUST be the same";
        AutobahnProducerConfig autobahnProducerConfig = new AutobahnProducerConfig.Builder(Environment.INT, "Bronte", identityProvider).build();
        when(producerConfig.getAutobahnProducerConfig()).thenReturn(autobahnProducerConfig);
        when(producerConfig.getOperationalType()).thenReturn(OperationalType.NON_OPERATIONAL);
        IllegalArgumentException e = assertThrows(IllegalArgumentException.class,
                () -> autobahnProducerService.produceMessage(message));
        assertEquals(expectedMessage, e.getMessage());
    }

    @Test
    void test_publish_exception() throws IllegalArgumentException {
        AutobahnProducerConfig autobahnProducerConfig = new AutobahnProducerConfig.Builder(Environment.PRD, "Bronte", identityProvider).build();
        when(producerConfig.getAutobahnProducerConfig()).thenReturn(autobahnProducerConfig);
        when(producerConfig.getOperationalType()).thenReturn(OperationalType.OPERATIONAL);
        PublishException e = assertThrows(PublishException.class,
                () -> autobahnProducerService.produceMessage(message));
        assertEquals("Failed to publish your message. Schema lookup failed for messageType:Bronte, namespace:namespace, version:1.1.0.\n" +
                "Schema does not exist.", e.getMessage());
    }

    @Test
    void test_publish_success() throws AutobahnIdentityProviderException, SchemaNotFoundException, PublishException, InitializationException, SchemaValidationException {
        ProducerMessage message = new ProducerMessage("autobahn-test", "AutoBahn", "1.0",
                stream, correlationId, actionType, payload, routingTags);
        identityProvider = new MockPiAutobahnIdentityProvider("", "", Environment.STG);
        AutobahnProducerConfig autobahnProducerConfig = new AutobahnProducerConfig.Builder(Environment.STG, "AutobahnTest", identityProvider).build();
        when(producerConfig.getOperationalType()).thenReturn(OperationalType.NON_OPERATIONAL);
        when(producerConfig.getAutobahnProducerConfig()).thenReturn(autobahnProducerConfig);
        UUID trackingId = autobahnProducerService.produceMessage(message);
        assertNotNull(trackingId);
    }

}
