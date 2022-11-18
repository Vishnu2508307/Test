package com.smartsparrow.graphql.schema;

import java.util.concurrent.CompletableFuture;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.smartsparrow.graphql.type.Ping;

import io.leangen.graphql.annotations.GraphQLQuery;
import reactor.core.publisher.Mono;

@Singleton
public class PingSchema {

    private final Ping ping;

    @Inject
    public PingSchema(Ping ping) {
        this.ping = ping;
    }

    @GraphQLQuery(name = "ping")
    public CompletableFuture<Ping> getPing() {
        return Mono.just(ping).toFuture();
    }
}
