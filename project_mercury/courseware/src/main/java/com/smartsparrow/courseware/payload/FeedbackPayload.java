package com.smartsparrow.courseware.payload;

import java.util.List;
import java.util.Objects;
import java.util.UUID;

import javax.annotation.Nonnull;

import com.smartsparrow.asset.service.AssetPayload;
import com.smartsparrow.courseware.data.Feedback;
import com.smartsparrow.plugin.data.PluginFilter;
import com.smartsparrow.plugin.data.PluginSummary;
import com.smartsparrow.plugin.payload.PluginRefPayload;

public class FeedbackPayload {

    private UUID feedbackId;
    private UUID interactiveId;
    private PluginRefPayload plugin;
    private String config;
    private List<AssetPayload> assets;

    public UUID getFeedbackId() {
        return feedbackId;
    }

    public UUID getInteractiveId() {
        return interactiveId;
    }

    public PluginRefPayload getPlugin() {
        return plugin;
    }

    public String getConfig() {
        return config;
    }

    public List<AssetPayload> getAssets() {
        return assets;
    }

    public static FeedbackPayload from(@Nonnull Feedback feedback, @Nonnull PluginSummary plugin,
                                       @Nonnull UUID interactiveId, String config,
                                       @Nonnull List<PluginFilter> pluginFilters) {
        FeedbackPayload payload = new FeedbackPayload();
        payload.feedbackId = feedback.getId();
        payload.plugin = PluginRefPayload.from(plugin, feedback.getPluginVersionExpr(), pluginFilters);
        payload.interactiveId = interactiveId;
        payload.config = config;
        return payload;
    }

    public void setAssets(List<AssetPayload> assets) {
        this.assets = assets;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        FeedbackPayload that = (FeedbackPayload) o;
        return Objects.equals(feedbackId, that.feedbackId) &&
                Objects.equals(interactiveId, that.interactiveId) &&
                Objects.equals(plugin, that.plugin) &&
                Objects.equals(config, that.config) &&
                Objects.equals(assets, that.assets);
    }

    @Override
    public int hashCode() {
        return Objects.hash(feedbackId, interactiveId, plugin, config, assets);
    }
}
