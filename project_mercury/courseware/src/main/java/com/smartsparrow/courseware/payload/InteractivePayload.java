package com.smartsparrow.courseware.payload;

import java.util.List;
import java.util.Objects;
import java.util.UUID;

import com.smartsparrow.asset.service.AssetPayload;
import com.smartsparrow.competency.payload.DocumentItemPayload;
import com.smartsparrow.courseware.data.CoursewareElementDescription;
import com.smartsparrow.courseware.data.Interactive;
import com.smartsparrow.courseware.data.InteractiveConfig;
import com.smartsparrow.plugin.data.PluginFilter;
import com.smartsparrow.plugin.data.PluginSummary;
import com.smartsparrow.plugin.payload.PluginRefPayload;

public class InteractivePayload {

    private UUID interactiveId;
    private String config;
    private PluginRefPayload plugin;
    private UUID parentPathwayId;
    private List<UUID> components;
    private List<UUID> feedbacks;
    private List<AssetPayload> assets;
    private List<AssetPayload> mathAssets;
    private UUID studentScopeURN;
    private List<DocumentItemPayload> linkedDocumentItems;
    private String description;

    public static InteractivePayload from(Interactive interactive,
                                          PluginSummary pluginSummary,
                                          InteractiveConfig interactiveConfig,
                                          UUID parentPathwayId,
                                          List<UUID> componentIds,
                                          List<UUID> feedbackIds,
                                          CoursewareElementDescription elementDescription,
                                          List<PluginFilter> pluginFilters) {
        InteractivePayload payload = new InteractivePayload();
        payload.interactiveId = interactive.getId();
        payload.config = interactiveConfig.getConfig();
        payload.plugin = PluginRefPayload.from(pluginSummary, interactive.getPluginVersionExpr(), pluginFilters);
        payload.parentPathwayId = parentPathwayId;
        payload.components = componentIds;
        payload.feedbacks = feedbackIds;
        payload.studentScopeURN = interactive.getStudentScopeURN();
        payload.description = elementDescription.getValue();
        return payload;
    }

    public InteractivePayload setAssets(List<AssetPayload> assets) {
        this.assets = assets;
        return this;
    }

    public InteractivePayload setMathAssets(List<AssetPayload> mathAssets) {
        this.mathAssets = mathAssets;
        return this;
    }

    public void setLinkedDocumentItems(List<DocumentItemPayload> linkedDocumentItems) {
        this.linkedDocumentItems = linkedDocumentItems;
    }

    public UUID getInteractiveId() {
        return interactiveId;
    }

    public String getConfig() {
        return config;
    }

    public PluginRefPayload getPlugin() {
        return plugin;
    }

    public UUID getParentPathwayId() {
        return parentPathwayId;
    }

    public List<UUID> getComponents() {
        return components;
    }

    public List<UUID> getFeedbacks() {
        return feedbacks;
    }

    public List<AssetPayload> getAssets() {
        return assets;
    }

    public List<AssetPayload> getMathAssets() {
        return mathAssets;
    }

    public UUID getStudentScopeURN() {
        return studentScopeURN;
    }

    public List<DocumentItemPayload> getLinkedDocumentItems() {
        return linkedDocumentItems;
    }

    public String getDescription() { return description; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        InteractivePayload that = (InteractivePayload) o;
        return Objects.equals(interactiveId, that.interactiveId) &&
                Objects.equals(config, that.config) &&
                Objects.equals(plugin, that.plugin) &&
                Objects.equals(parentPathwayId, that.parentPathwayId) &&
                Objects.equals(components, that.components) &&
                Objects.equals(feedbacks, that.feedbacks) &&
                Objects.equals(assets, that.assets) &&
                Objects.equals(mathAssets, that.mathAssets) &&
                Objects.equals(studentScopeURN, that.studentScopeURN) &&
                Objects.equals(linkedDocumentItems, that.linkedDocumentItems) &&
                Objects.equals(description, that.description);
    }

    @Override
    public int hashCode() {
        return Objects.hash(interactiveId, config, plugin, parentPathwayId, components, feedbacks, assets, mathAssets, studentScopeURN, linkedDocumentItems, description);
    }

    @Override
    public String toString() {
        return "InteractivePayload{" +
                "interactiveId=" + interactiveId +
                ", config='" + config + '\'' +
                ", plugin=" + plugin +
                ", parentPathwayId=" + parentPathwayId +
                ", components=" + components +
                ", feedbacks=" + feedbacks +
                ", assets=" + assets +
                ", mathAssets=" + mathAssets +
                ", studentScopeURN=" + studentScopeURN +
                ", linkedDocumentItems=" + linkedDocumentItems +
                ", description='" + description + '\'' +
                '}';
    }
}
