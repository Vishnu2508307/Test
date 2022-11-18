package com.smartsparrow.rtm;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.verify;

import java.io.IOException;
import java.util.function.Consumer;

import org.eclipse.jetty.websocket.api.Session;
import org.mockito.ArgumentCaptor;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.smartsparrow.rtm.message.send.BasicResponseMessage;

public class MessageHandlerTestUtils {

    public static void verifySentMessage(Session session, String expectedMessage) {
        ArgumentCaptor<String> captor = ArgumentCaptor.forClass(String.class);
        verify(session.getRemote()).sendStringByFuture(captor.capture());

        assertEquals(expectedMessage, captor.getValue());
    }

    public static void verifySentMessage(Session session, Consumer<BasicResponseMessage> assertions) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        ArgumentCaptor<String> captor = ArgumentCaptor.forClass(String.class);
        verify(session.getRemote(), atLeastOnce()).sendStringByFuture(captor.capture());
        String json = captor.getValue();
        BasicResponseMessage response = mapper.readValue(json, BasicResponseMessage.class);

        assertions.accept(response);
    }
}
