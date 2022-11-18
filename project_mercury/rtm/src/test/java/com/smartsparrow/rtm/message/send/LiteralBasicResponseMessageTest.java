package com.smartsparrow.rtm.message.send;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.smartsparrow.rtm.util.Responses;

public class LiteralBasicResponseMessageTest {

    @Test
    @DisplayName("Should render null values as part of the response")
    public void nullValuesAreSerialized() throws Exception {
        LiteralBasicResponseMessage message = new LiteralBasicResponseMessage("message.response", null);
        message.addField("foo", null);

        String json = Responses.valueAsString(message);

        assertThat(json).isEqualTo("{\"type\":\"message.response\",\"response\":{\"foo\":null}}");
    }

    @Test
    @DisplayName("Should render an empty response collection as part of the response")
    public void emptyResponseIsSerialized() throws Exception {
        LiteralBasicResponseMessage message = new LiteralBasicResponseMessage("message.response", null);

        // note: it is not this:
        //   message.addField("foo", new ArrayList<>());
        // it is the response collection itself (not the contents of it)

        String json = Responses.valueAsString(message);

        assertThat(json).isEqualTo("{\"type\":\"message.response\",\"response\":{}}");
    }
}
