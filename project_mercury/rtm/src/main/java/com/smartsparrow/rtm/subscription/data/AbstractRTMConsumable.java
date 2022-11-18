package com.smartsparrow.rtm.subscription.data;

import java.io.Serializable;
import java.util.Objects;
import java.util.UUID;

import com.smartsparrow.dataevent.BroadcastMessage;
import com.smartsparrow.pubsub.data.AbstractConsumable;
import com.smartsparrow.rtm.message.RTMClientContext;

/**
 * Common behaviour for every RTM consumable class
 * @param <T> the type of broadcast message for this consumable
 */
public abstract class AbstractRTMConsumable<T extends BroadcastMessage> extends AbstractConsumable<T>
        implements RTMConsumable<T>, Serializable {

    private static final long serialVersionUID = -1540904524804327793L;
    protected final RTMClientContext rtmClientContext;

    protected AbstractRTMConsumable(final RTMClientContext rtmClientContext, final T content) {
        super(content);
        this.rtmClientContext = rtmClientContext;
    }

    @Override
    public T getContent() {
        return content;
    }

    public RTMClientContext getRTMClientContext() {
        return rtmClientContext;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AbstractRTMConsumable<?> that = (AbstractRTMConsumable<?>) o;
        return Objects.equals(content, that.content)
                && Objects.equals(rtmClientContext, that.rtmClientContext);
    }

    @Override
    public int hashCode() {
        return Objects.hash(content, rtmClientContext);
    }
}
