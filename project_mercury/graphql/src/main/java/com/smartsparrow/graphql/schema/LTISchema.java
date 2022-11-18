package com.smartsparrow.graphql.schema;

import java.util.concurrent.CompletableFuture;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.smartsparrow.graphql.type.LTIContext;
import com.smartsparrow.plugin.data.PluginSummary;

import io.leangen.graphql.annotations.GraphQLContext;
import io.leangen.graphql.annotations.GraphQLQuery;
import reactor.core.publisher.Mono;

@Singleton
public class LTISchema {

    @Inject
    public LTISchema() {
    }

    @GraphQLQuery(name = "lti", description = "lti context for a plugin")
    public CompletableFuture<LTIContext> getLTIContext(@GraphQLContext PluginSummary pluginSummary) {
        //
        return Mono.just(new LTIContext(pluginSummary)).toFuture();
    }
}
