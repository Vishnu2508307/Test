package com.smartsparrow.util;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.publisher.TestPublisher;

public final class ReactorTestUtils {

    /**
     * Convenient method to mock Mono to throw an exception
     * @param t the exception to throw
     * @param <T> the type of Mono
     * @return mono which will throw an exception
     */
    public static <T> Mono<T> monoErrorPublisher(Throwable t) {
        return TestPublisher.<T>create().error(t).mono();
    }

    /**
     * Convenient method to mock Flux to throw an exception
     * @param t the exception to throw
     * @param <T> the type of Flux
     * @return flux which will throw an exception
     */
    public static <T> Flux<T> fluxErrorPublisher(Throwable t) {
        return TestPublisher.<T>create().error(t).flux();
    }


}
