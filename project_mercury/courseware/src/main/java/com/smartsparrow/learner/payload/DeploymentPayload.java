package com.smartsparrow.learner.payload;

import java.util.List;
import java.util.Objects;
import java.util.UUID;

import javax.annotation.Nonnull;

import com.smartsparrow.learner.data.LearnerActivity;
import com.smartsparrow.plugin.data.PluginFilter;
import com.smartsparrow.plugin.data.PluginSummary;
import com.smartsparrow.plugin.payload.PluginRefPayload;

public class DeploymentPayload {

    private UUID deploymentId;
    private UUID activityId;
    private PluginRefPayload plugin;
    private String config;

    public static DeploymentPayload from(@Nonnull LearnerActivity learnerActivity,
                                         @Nonnull PluginSummary plugin,
                                         @Nonnull List<PluginFilter> pluginFilters) {
        DeploymentPayload deploymentPayload = new DeploymentPayload();
        deploymentPayload.deploymentId = learnerActivity.getDeploymentId();
        deploymentPayload.activityId = learnerActivity.getId();
        deploymentPayload.config = learnerActivity.getConfig();
        deploymentPayload.plugin = PluginRefPayload.from(plugin, learnerActivity.getPluginVersionExpr(), pluginFilters);
        return deploymentPayload;
    }

    public UUID getDeploymentId() {
        return deploymentId;
    }

    public UUID getActivityId() {
        return activityId;
    }

    public PluginRefPayload getPlugin() {
        return plugin;
    }

    public String getConfig() {
        return config;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DeploymentPayload that = (DeploymentPayload) o;
        return Objects.equals(deploymentId, that.deploymentId) &&
                Objects.equals(activityId, that.activityId) &&
                Objects.equals(plugin, that.plugin) &&
                Objects.equals(config, that.config);
    }

    @Override
    public int hashCode() {
        return Objects.hash(deploymentId, activityId, plugin, config);
    }
}
