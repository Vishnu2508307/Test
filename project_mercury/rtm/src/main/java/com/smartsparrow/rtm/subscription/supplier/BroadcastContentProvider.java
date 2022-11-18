package com.smartsparrow.rtm.subscription.supplier;

public interface BroadcastContentProvider<T, C> {

    /**
     * Get the broadcast content supplier {@link C} given a type {@link T}
     *
     * @param t the type of content supplier to get
     * @return the content supplier object {@link C}
     */
    C get(T t);
}
