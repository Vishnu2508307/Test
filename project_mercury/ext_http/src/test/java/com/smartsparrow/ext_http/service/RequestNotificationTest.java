package com.smartsparrow.ext_http.service;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableMap;
import com.smartsparrow.util.UUIDs;

class RequestNotificationTest {

    ObjectMapper mapper = new ObjectMapper();

    @Test
    void generatedJson() throws JsonProcessingException {
        Request request = new Request() //
                .setUri("https://images.google.com") //
                .addField("qs", ImmutableMap.of("q", "homer+simpson"));

        RequestNotification notification = new RequestNotification() //
                .setParams(request.getParams())
                .setState(new NotificationState()
                                  .setPurpose(RequestPurpose.GENERAL)
                                  .setNotificationId(UUIDs.fromString("36716700-8e45-11e9-a903-397d1778a801"))
                                  .setReferenceId(UUIDs.fromString("90441f0c-9c73-11e9-a2a3-2a2ae2dbcce4")));

        String actual = mapper.writeValueAsString(notification);

        // @formatter:off
        String expected = "{"
                +  "\"state\":{"
                +    "\"notificationId\":\"36716700-8e45-11e9-a903-397d1778a801\","
                +    "\"purpose\":\"GENERAL\","
                +    "\"referenceId\":\"90441f0c-9c73-11e9-a2a3-2a2ae2dbcce4\""
                +  "},"
                +  "\"params\":{"
                +    "\"uri\":\"https://images.google.com\","
                +    "\"qs\":{"
                +      "\"q\":\"homer+simpson\""
                +    "}"
                +  "}"
                +"}";
        // @formatter:on

        assertEquals(expected, actual);
    }
}