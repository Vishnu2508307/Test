package com.smartsparrow.eval.data;

import java.util.Objects;

/**
 * Describes the options for an action consumer.
 * TODO in the future make this configurable at runtime
 */
public class ActionConsumerOptions {

    private boolean async;

    /**
     * Describe whether the action consumer will be be synchronous or asynchronous
     *
     * @return true if the action consumer is asynchronous
     */
    public boolean isAsync() {
        return async;
    }

    public ActionConsumerOptions setAsync(boolean async) {
        this.async = async;
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ActionConsumerOptions that = (ActionConsumerOptions) o;
        return async == that.async;
    }

    @Override
    public int hashCode() {
        return Objects.hash(async);
    }

    @Override
    public String toString() {
        return "ActionConsumerOptions{" +
                "async=" + async +
                '}';
    }
}
