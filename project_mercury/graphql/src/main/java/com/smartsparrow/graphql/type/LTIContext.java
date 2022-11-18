package com.smartsparrow.graphql.type;

import com.smartsparrow.plugin.data.PluginSummary;

import io.leangen.graphql.annotations.GraphQLIgnore;
import io.leangen.graphql.annotations.GraphQLQuery;

public class LTIContext {

    private PluginSummary pluginSummary;

    public LTIContext(final PluginSummary pluginSummary) {
        this.pluginSummary = pluginSummary;
    }

    @GraphQLQuery(name = "toolProvider", description = "the tool provider context")
    public LTIToolProviderContext getToolProviderContext() {
        return new LTIToolProviderContext(this);
    }

    @GraphQLIgnore
    public PluginSummary getPluginSummary() {
        return pluginSummary;
    }
}
