package com.smartsparrow.rtm.subscription.supplier;

import reactor.core.publisher.Mono;

public interface BroadcastContentSupplier<T, R> {

    /**
     * Supply a map of fields from a {@link T} content object
     *
     * @param content the content to extract the data fields from
     * @return a mono of {@link R}
     */
    Mono<R> supplyFrom(T content);
}
