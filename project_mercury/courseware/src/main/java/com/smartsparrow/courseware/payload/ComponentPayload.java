package com.smartsparrow.courseware.payload;

import java.util.List;
import java.util.Objects;
import java.util.UUID;

import javax.annotation.Nonnull;

import com.smartsparrow.asset.service.AssetPayload;
import com.smartsparrow.courseware.data.Component;
import com.smartsparrow.courseware.data.CoursewareElementDescription;
import com.smartsparrow.courseware.data.CoursewareElementType;
import com.smartsparrow.courseware.data.ParentByComponent;
import com.smartsparrow.plugin.data.PluginFilter;
import com.smartsparrow.plugin.data.PluginSummary;
import com.smartsparrow.plugin.payload.PluginRefPayload;

public class ComponentPayload {

    private UUID componentId;
    private String config;
    private PluginRefPayload plugin;
    private UUID parentId;
    private CoursewareElementType parentType;
    private List<AssetPayload> assets;
    private String description;

    public static ComponentPayload from(@Nonnull Component component,
                                        @Nonnull String config,
                                        @Nonnull PluginSummary plugin,
                                        @Nonnull ParentByComponent parentByComponent,
                                        @Nonnull CoursewareElementDescription elementDescription,
                                        @Nonnull List<PluginFilter> pluginFilters) {
        ComponentPayload payload = new ComponentPayload();
        payload.componentId = component.getId();
        payload.config = config;
        payload.plugin = PluginRefPayload.from(plugin, component.getPluginVersionExpr(), pluginFilters);
        payload.parentId = parentByComponent.getParentId();
        payload.parentType = parentByComponent.getParentType();
        payload.description = elementDescription.getValue();
        return payload;
    }

    public ComponentPayload setAssets(List<AssetPayload> assets) {
        this.assets = assets;
        return this;
    }

    public UUID getComponentId() {
        return componentId;
    }

    public String getConfig() {
        return config;
    }

    public PluginRefPayload getPlugin() {
        return plugin;
    }

    public UUID getParentId() {
        return parentId;
    }

    public CoursewareElementType getParentType() {
        return parentType;
    }

    public List<AssetPayload> getAssets() {
        return assets;
    }

    public String getDescription() { return description; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ComponentPayload that = (ComponentPayload) o;
        return Objects.equals(componentId, that.componentId) &&
                Objects.equals(config, that.config) &&
                Objects.equals(plugin, that.plugin) &&
                Objects.equals(parentId, that.parentId) &&
                parentType == that.parentType &&
                Objects.equals(assets, that.assets) &&
                Objects.equals(description, that.description);
    }

    @Override
    public int hashCode() {
        return Objects.hash(componentId, config, plugin, parentId, parentType, assets, description);
    }

    @Override
    public String toString() {
        return "ComponentPayload{" +
                "componentId=" + componentId +
                ", config='" + config + '\'' +
                ", plugin=" + plugin +
                ", parentId=" + parentId +
                ", parentType=" + parentType +
                ", assets=" + assets +
                ", description='" + description + '\'' +
                '}';
    }
}
