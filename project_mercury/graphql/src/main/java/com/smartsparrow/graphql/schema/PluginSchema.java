package com.smartsparrow.graphql.schema;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.smartsparrow.courseware.data.PluginReference;
import com.smartsparrow.plugin.data.PluginSummary;
import com.smartsparrow.plugin.lang.PluginNotFoundFault;
import com.smartsparrow.plugin.lang.VersionParserFault;
import com.smartsparrow.plugin.payload.PluginPayload;
import com.smartsparrow.plugin.service.PluginService;

import io.leangen.graphql.annotations.GraphQLArgument;
import io.leangen.graphql.annotations.GraphQLContext;
import io.leangen.graphql.annotations.GraphQLNonNull;
import io.leangen.graphql.annotations.GraphQLQuery;

@Singleton
public class PluginSchema {

    private final PluginService pluginService;

    @Inject
    public PluginSchema(PluginService pluginService) {
        this.pluginService = pluginService;
    }

    /**
     * @throws PluginNotFoundFault if plugin not found
     * @throws VersionParserFault if version expression can not be parsed
     * @return {@link CompletableFuture<PluginPayload>} a completable future of plugin payload
     */
    @GraphQLQuery(name = "plugin", description = "Plugin data including manifest schema and entry points")
    public CompletableFuture<PluginPayload> getPlugin(@GraphQLContext PluginReference pluginRef,
                                                      @GraphQLArgument(name = "view", description = "Fetch a specific view") String view) {

        //add permission checks on view
        if (view == null) {
            return pluginService.findPlugin(pluginRef.getPluginId(), pluginRef.getPluginVersionExpr()).toFuture();
        } else {
            return pluginService.findPluginByIdAndView(pluginRef.getPluginId(), view, pluginRef.getPluginVersionExpr()).toFuture();
        }
    }

    @GraphQLQuery(name = "pluginById", description = "find a plugin by id")
    public CompletableFuture<PluginSummary> getPluginById(@GraphQLArgument(name = "id", description = "the plugin id") @GraphQLNonNull UUID pluginId) {
        return pluginService.find(pluginId).toFuture();
    }
}
