package com.smartsparrow.rtm.message;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.util.Map;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.fasterxml.jackson.annotation.JacksonInject;
import com.google.inject.Provider;
import com.smartsparrow.config.service.ConfigurationService;
import com.smartsparrow.eval.wiring.EvaluationFeatureMode;
import com.smartsparrow.rtm.lang.InvalidMessageFormat;
import com.smartsparrow.rtm.lang.UnsupportedMessageType;
import com.smartsparrow.util.Enums;

class ReceivedMessageDeserializerTest {

    @InjectMocks
    private ReceivedMessageDeserializer receivedMessageDeserializer;

    @Mock
    private Map<String, Provider<Class<? extends MessageType>>> messageTypes;

    @Mock
    private ConfigurationService configurationService;

    private String type = "type";
    private String id = "some-id";
    @Mock
    private Provider<Class<? extends MessageType>> classProvider;

    @SuppressWarnings("unchecked")
    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @SuppressWarnings("unchecked")
    @Test
    void deserialize_idNotSupplied() throws IOException, ClassNotFoundException {
        String message = String.format( "{\"type\":\"%s\"}", type);
        when(messageTypes.containsKey(type)).thenReturn(true);
        when(messageTypes.get(type)).thenReturn(classProvider);
        Class classType = Class.forName(TestEntity.class.getTypeName());
        when(classProvider.get()).thenReturn(classType);
        ReceivedMessage deserialized = receivedMessageDeserializer.deserialize(message);
        assertNotNull(deserialized);
    }

    @Test
    void deserialize_emptyType() {
        String message = String.format("{\"id\":\"%s\"}", id);
        Throwable t = assertThrows(UnsupportedMessageType.class, ()-> receivedMessageDeserializer.deserialize(message));
        assertEquals(id, ((UnsupportedMessageType) t).getReplyTo());
    }

    //testing the invalid json input of an array or raw string
    @Test
    void deserialize_unsupportedType() {
        String[] messages = {"[\"type\",\"id\"]", "\"test json string input\""};
        for(String message : messages) {
            Throwable t = assertThrows(UnsupportedMessageType.class, ()-> receivedMessageDeserializer.deserialize(message));
            assertEquals("Unsupported message type ", ((UnsupportedMessageType) t).getMessage());
        }
    }

    @Test
    void deserialize_typeNotFound() {
        String message = String.format("{\"type\": \"%s\", \"id\":\"%s\"}", type, id);
        when(messageTypes.containsKey(type)).thenReturn(false);
        Throwable t = assertThrows(UnsupportedMessageType.class, ()-> receivedMessageDeserializer.deserialize(message));
        assertEquals(id, ((UnsupportedMessageType) t).getReplyTo());
    }

    @SuppressWarnings("unchecked")
    @Test
    void deserialize_invalidFormat() throws ClassNotFoundException {
        String message = String.format("{\"type\": \"%s\", \"id\":\"%s\", \"entityId\":\"should-be-a-uuid\"}", type, id);
        when(messageTypes.containsKey(type)).thenReturn(true);
        when(messageTypes.get(type)).thenReturn(classProvider);
        Class classType = Class.forName(TestEntity.class.getTypeName());
        when(classProvider.get()).thenReturn(classType);
        Throwable t = assertThrows(InvalidMessageFormat.class, ()-> receivedMessageDeserializer.deserialize(message));
        assertEquals(id, ((InvalidMessageFormat) t).getReplyTo());
    }

    @Test
    void deserialize_evaluationFeatureModeDefault() throws IOException, ClassNotFoundException {
        EvaluationFeatureMode defaultMode = new EvaluationFeatureMode();
        defaultMode.setProcessingMode("DEFAULT");
        String message = String.format( "{\"type\":\"%s\"}", type);
        when(messageTypes.containsKey(type)).thenReturn(true);
        when(messageTypes.get(type)).thenReturn(classProvider);
        Class classType = Class.forName(TestEntity.class.getTypeName());
        when(classProvider.get()).thenReturn(classType);
        when(configurationService.get(EvaluationFeatureMode.class, "evaluation.mode")).thenReturn(defaultMode);
        ReceivedMessage deserialized = receivedMessageDeserializer.deserialize(message);
        assertNotNull(deserialized.getMode());
        assertEquals(ReceivedMessage.Mode.DEFAULT, deserialized.getMode());
    }

    @Test
    void deserialize_evaluationFeatureModeWaitPending() throws IOException, ClassNotFoundException {
        EvaluationFeatureMode waitPendingMode = new EvaluationFeatureMode();
        waitPendingMode.setProcessingMode("WAIT_PENDING");
        String message = String.format( "{\"type\":\"%s\"}", type);
        when(messageTypes.containsKey(type)).thenReturn(true);
        when(messageTypes.get(type)).thenReturn(classProvider);
        Class classType = Class.forName(TestEntity.class.getTypeName());
        when(classProvider.get()).thenReturn(classType);
        when(configurationService.get(EvaluationFeatureMode.class, "evaluation.mode")).thenReturn(waitPendingMode);
        ReceivedMessage deserialized = receivedMessageDeserializer.deserialize(message);
        assertNotNull(deserialized.getMode());
        assertEquals(ReceivedMessage.Mode.WAIT_PENDING, deserialized.getMode());
    }

    @Test
    void deserialize_evaluationFeatureModeMissing() throws IOException, ClassNotFoundException {
        String message = String.format( "{\"type\":\"%s\"}", type);
        when(messageTypes.containsKey(type)).thenReturn(true);
        when(messageTypes.get(type)).thenReturn(classProvider);
        Class classType = Class.forName(TestEntity.class.getTypeName());
        when(classProvider.get()).thenReturn(classType);
        when(configurationService.get(EvaluationFeatureMode.class, "evaluation.mode")).thenReturn(null);
        ReceivedMessage deserialized = receivedMessageDeserializer.deserialize(message);
        assertNotNull(deserialized.getMode());
        assertEquals(ReceivedMessage.Mode.DEFAULT, deserialized.getMode());
    }

    private static class TestEntity extends ReceivedMessage {
        private UUID entityId;

        @JacksonInject("configurationService")
        private ConfigurationService configurationService;

        private TestEntity() {

        }

        public UUID getEntityId() {
            return entityId;
        }

        public TestEntity setEntityId(UUID entityId) {
            this.entityId = entityId;
            return this;
        }

        @Override
        public Mode getMode() {
            EvaluationFeatureMode mode = configurationService.get(EvaluationFeatureMode.class, "evaluation.mode");
            // make this safe, if no mode is found then use DEFAULT
            if (mode == null) {
                mode = new EvaluationFeatureMode().setProcessingMode("DEFAULT");
            }
            return Enums.of(Mode.class, mode.getProcessingMode());
        }
    }

}
