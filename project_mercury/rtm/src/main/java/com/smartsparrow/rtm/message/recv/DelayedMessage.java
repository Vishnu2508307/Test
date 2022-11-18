package com.smartsparrow.rtm.message.recv;

import java.util.Objects;

import com.smartsparrow.rtm.message.ReceivedMessage;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

@SuppressFBWarnings(value = "UWF_UNWRITTEN_FIELD", justification = "Hydrated via Jackson on message deserialization")
public class DelayedMessage extends ReceivedMessage {

    private Long delay;

    public Long getDelay() {
        return delay;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DelayedMessage that = (DelayedMessage) o;
        return Objects.equals(delay, that.delay);
    }

    @Override
    public int hashCode() {
        return Objects.hash(delay);
    }

    @Override
    public String toString() {
        return "DelayedMessage{" +
                "delay=" + delay +
                '}';
    }
}
