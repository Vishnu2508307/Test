package com.smartsparrow.graphql.type;

import javax.inject.Inject;

import io.leangen.graphql.annotations.GraphQLQuery;

public class Ping {

    @Inject
    public Ping() {
    }

    @GraphQLQuery(name = "result", description = "returns pong")
    public String result() {
        return "pong";
    }
}
