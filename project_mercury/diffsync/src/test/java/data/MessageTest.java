package data;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.util.UUID;

import org.junit.jupiter.api.Test;

import com.smartsparrow.util.UUIDs;

class MessageTest {

    private static final UUID messageId = UUIDs.timeBased();

    @Test
    void createPatchMessage() {
        Message<Patch> patchMessage = Message.build(new Patch()
                .setId(messageId));

        assertNotNull(patchMessage);
        assertNotNull(patchMessage.getBody());
        assertEquals(Patch.class, patchMessage.getBody().getClass());
        assertEquals(messageId, patchMessage.getBody().getId());
    }

    @Test
    void createAckMessage() {
        Message<Ack> ackMessage = Message.build(new Ack()
                .setId(messageId));

        assertNotNull(ackMessage);
        assertNotNull(ackMessage.getBody());
        assertEquals(Ack.class, ackMessage.getBody().getClass());
        assertEquals(messageId, ackMessage.getBody().getId());
    }
}