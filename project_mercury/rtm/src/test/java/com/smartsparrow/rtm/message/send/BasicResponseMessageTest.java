package com.smartsparrow.rtm.message.send;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.smartsparrow.rtm.util.Responses;

public class BasicResponseMessageTest {

    @Test
    @DisplayName("Should not render null as part of the response")
    public void nullValuesAreNotSerialized() throws Exception {
        BasicResponseMessage message = new BasicResponseMessage("message.response", null);
        message.addField("foo", null);
        message.addField("bar", "1");

        String json = Responses.valueAsString(message);

        assertThat(json).isEqualTo("{\"type\":\"message.response\",\"response\":{\"bar\":\"1\"}}");
    }

    @Test
    @DisplayName("Should not render an empty response collection as part of the response")
    public void emptyResponseIsNotSerialized() throws Exception {
        BasicResponseMessage message = new BasicResponseMessage("message.response", null);

        String json = Responses.valueAsString(message);

        assertThat(json).isEqualTo("{\"type\":\"message.response\"}");
    }

    @Test
    @DisplayName("Should not render an effectively empty response with null fields as part of the response")
    public void effectivelyEmptyResponseIsNotSerialized() throws Exception {
        BasicResponseMessage message = new BasicResponseMessage("message.response", null);
        message.addField("foo", null);

        // note: it is not this:
        //   message.addField("foo", new ArrayList<>());
        // it is the response collection itself (not the contents of it)

        String json = Responses.valueAsString(message);

        assertThat(json).isEqualTo("{\"type\":\"message.response\"}");
    }

}
