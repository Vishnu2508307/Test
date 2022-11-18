package com.smartsparrow.rtm.message.recv;

import java.util.Objects;

import com.fasterxml.jackson.databind.JsonNode;
import com.smartsparrow.rtm.message.ReceivedMessage;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

@SuppressFBWarnings(value = "UWF_UNWRITTEN_FIELD", justification = "Hydrated via Jackson on message deserialization")
public class GenericPublishMessage extends ReceivedMessage {

    private String topic;
    private JsonNode payload;

    public GenericPublishMessage() {
    }

    public String getTopic() {
        return topic;
    }

    public GenericPublishMessage setTopic(String topic) {
        this.topic = topic;
        return this;
    }

    public JsonNode getPayload() {
        return payload;
    }

    public GenericPublishMessage setPayload(JsonNode payload) {
        this.payload = payload;
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        GenericPublishMessage that = (GenericPublishMessage) o;
        return Objects.equals(topic, that.topic) && Objects.equals(payload, that.payload);
    }

    @Override
    public int hashCode() {
        return Objects.hash(topic, payload);
    }
}
