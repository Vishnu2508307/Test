package com.smartsparrow.rtm.diffsync;

import static com.smartsparrow.rtm.diffsync.MessageTypeBridgeConverter.DIFF_SYNC_ACK;
import static com.smartsparrow.rtm.diffsync.MessageTypeBridgeConverter.DIFF_SYNC_END;
import static com.smartsparrow.rtm.diffsync.MessageTypeBridgeConverter.DIFF_SYNC_PATCH;
import static com.smartsparrow.rtm.diffsync.MessageTypeBridgeConverter.DIFF_SYNC_START;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

import java.util.Arrays;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.smartsparrow.exception.IllegalArgumentFault;

import data.Exchangeable;

class MessageTypeBridgeConverterTest {

    @InjectMocks
    private MessageTypeBridgeConverter messageTypeBridgeConverter;

    @Mock
    private Exchangeable exchangeable;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void from_nullType() {
        when(exchangeable.getType()).thenReturn(null);

        IllegalArgumentFault f = assertThrows(IllegalArgumentFault.class,
                () -> messageTypeBridgeConverter.from(exchangeable.getType()));

        assertNotNull(f);
        assertEquals("exchangeable type is required", f.getMessage());
    }

    @Test
    void from_ack() {
        when(exchangeable.getType()).thenReturn(Exchangeable.Type.ACK);

        final String messageType = messageTypeBridgeConverter.from(exchangeable.getType());

        assertNotNull(messageType);
        assertEquals(DIFF_SYNC_ACK, messageType);
    }

    @Test
    void from_patch() {
        when(exchangeable.getType()).thenReturn(Exchangeable.Type.PATCH);

        final String messageType = messageTypeBridgeConverter.from(exchangeable.getType());

        assertNotNull(messageType);
        assertEquals(DIFF_SYNC_PATCH, messageType);
    }

    @Test
    void from_start() {
        when(exchangeable.getType()).thenReturn(Exchangeable.Type.START);

        final String messageType = messageTypeBridgeConverter.from(exchangeable.getType());

        assertNotNull(messageType);
        assertEquals(DIFF_SYNC_START, messageType);
    }

    @Test
    void from_end() {
        when(exchangeable.getType()).thenReturn(Exchangeable.Type.END);

        final String messageType = messageTypeBridgeConverter.from(exchangeable.getType());

        assertNotNull(messageType);
        assertEquals(DIFF_SYNC_END, messageType);
    }

    @Test
    void from_unsupported() {
        // it should never throw an unsupported operation fault
        Arrays.stream(Exchangeable.Type.values())
                .forEach(type -> assertDoesNotThrow(() -> messageTypeBridgeConverter.from(type)));
    }
}