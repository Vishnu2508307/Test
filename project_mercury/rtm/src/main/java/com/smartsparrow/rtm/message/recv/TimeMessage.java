package com.smartsparrow.rtm.message.recv;

import com.smartsparrow.rtm.message.ReceivedMessage;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

/**
 * An incoming time request, used for both the fetch and subscription use cases.
 */
@SuppressFBWarnings(value = "UWF_UNWRITTEN_FIELD", justification = "Hydrated via Jackson on message deserialization")
public class TimeMessage extends ReceivedMessage {

    // in Subscription mode, the number of seconds used
    private Integer interval;

    public TimeMessage() {
    }

    public Integer getInterval() {
        return interval;
    }

    public TimeMessage setInterval(Integer interval) {
        this.interval = interval;
        return this;
    }
}
