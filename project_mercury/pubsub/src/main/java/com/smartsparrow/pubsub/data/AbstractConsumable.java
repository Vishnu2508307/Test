package com.smartsparrow.pubsub.data;

import java.io.Serializable;
import java.util.Objects;
import java.util.UUID;

import com.smartsparrow.dataevent.BroadcastMessage;

/**
 * Common behaviour for every RTM consumable class
 * @param <T> the type of broadcast message for this consumable
 */
public abstract class AbstractConsumable<T extends BroadcastMessage> implements EventConsumable<T>, Serializable {

    private static final long serialVersionUID = -1746896393117202352L;
    protected final T content;
    private UUID subscriptionId;
    private String broadcastType;

    protected AbstractConsumable(final T content) {
        this.content = content;
    }

    @Override
    public T getContent() {
        return content;
    }

    @Override
    public UUID getSubscriptionId() {
        return subscriptionId;
    }

    public AbstractConsumable<T> setSubscriptionId(UUID subscriptionId) {
        this.subscriptionId = subscriptionId;
        return this;
    }

    @Override
    public String getBroadcastType() {
        return broadcastType;
    }

    public AbstractConsumable<T> setBroadcastType(String broadcastType) {
        this.broadcastType = broadcastType;
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AbstractConsumable<?> that = (AbstractConsumable<?>) o;
        return Objects.equals(content, that.content)
                && Objects.equals(subscriptionId, that.subscriptionId)
                && Objects.equals(broadcastType, that.broadcastType);
    }

    @Override
    public int hashCode() {
        return Objects.hash(content, subscriptionId, broadcastType);
    }
}
