package com.smartsparrow.rtm.message.recv;

import java.util.Objects;

import com.google.common.base.MoreObjects;
import com.smartsparrow.rtm.message.ReceivedMessage;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

@SuppressFBWarnings(value = "UWF_UNWRITTEN_FIELD", justification = "Hydrated via Jackson on message deserialization")
public class GenericSubscribeMessage extends ReceivedMessage {

    private String topic;

    public GenericSubscribeMessage() {
    }

    public String getTopic() {
        return topic;
    }

    public GenericSubscribeMessage setTopic(String topic) {
        this.topic = topic;
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        GenericSubscribeMessage that = (GenericSubscribeMessage) o;
        return Objects.equals(topic, that.topic);
    }

    @Override
    public int hashCode() {
        return Objects.hash(topic);
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this).add("topic", topic).toString();
    }
}
