package com.smartsparrow.graphql.schema;

import java.util.concurrent.CompletableFuture;

import javax.inject.Singleton;

import com.smartsparrow.graphql.type.Clock;

import io.leangen.graphql.annotations.GraphQLQuery;
import reactor.core.publisher.Mono;

@Singleton
public class ClockSchema {

    @GraphQLQuery(name = "clock")
    public CompletableFuture<Clock> getCurrentClock() {
        return Mono.just(new Clock()).toFuture();
    }

}

