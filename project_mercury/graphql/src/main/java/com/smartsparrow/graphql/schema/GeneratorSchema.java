package com.smartsparrow.graphql.schema;

import java.util.concurrent.CompletableFuture;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.smartsparrow.graphql.type.Generator;

import io.leangen.graphql.annotations.GraphQLQuery;
import reactor.core.publisher.Mono;

@Singleton
public class GeneratorSchema {

    private final Generator generator;

    @Inject
    public GeneratorSchema(Generator generator) {
        this.generator = generator;
    }

    @GraphQLQuery(name = "generator")
    public CompletableFuture<Generator> getGenerator() {
        return Mono.just(generator).toFuture();
    }

}
