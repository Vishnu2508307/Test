package com.smartsparrow.courseware.payload;

import java.util.List;
import java.util.Objects;
import java.util.UUID;

import javax.annotation.Nonnull;

import com.smartsparrow.asset.service.AssetPayload;
import com.smartsparrow.competency.payload.DocumentItemPayload;
import com.smartsparrow.courseware.data.Activity;
import com.smartsparrow.courseware.data.ActivityConfig;
import com.smartsparrow.courseware.data.ActivityTheme;
import com.smartsparrow.courseware.data.CoursewareElementDescription;
import com.smartsparrow.iam.payload.AccountPayload;
import com.smartsparrow.plugin.data.PluginFilter;
import com.smartsparrow.plugin.data.PluginSummary;
import com.smartsparrow.plugin.payload.PluginRefPayload;
import com.smartsparrow.util.DateFormat;
import com.smartsparrow.workspace.data.IconLibrary;
import com.smartsparrow.workspace.data.ThemePayload;

public class ActivityPayload {

    private UUID activityId;
    private String config;
    private PluginRefPayload plugin;
    private AccountPayload creator;
    private String createdAt;
    private String updatedAt;
    private String activityTheme;
    private UUID parentPathwayId;
    private List<UUID> childrenPathways;
    private List<UUID> components;
    private List<AssetPayload> assets;
    private List<AssetPayload> mathAssets;
    private UUID studentScopeURN;
    private List<DocumentItemPayload> linkedDocumentItems;
    private String description;
    private ThemePayload themePayload;
    private List<IconLibrary> activityThemeIconLibraries;

    public static ActivityPayload from(@Nonnull Activity activity, @Nonnull ActivityConfig config,
                                       @Nonnull PluginSummary plugin, @Nonnull AccountPayload creator,
                                       @Nonnull ActivityTheme activityTheme,
                                       @Nonnull List<UUID> childrenPathwayIds,
                                       @Nonnull List<UUID> componentIds,
                                       @Nonnull CoursewareElementDescription elementDescription,
                                       @Nonnull List<PluginFilter> pluginFilters,
                                       @Nonnull ThemePayload themePayload,
                                       @Nonnull List<IconLibrary> activityThemeIconLibraries) {
        ActivityPayload result = new ActivityPayload();
        result.activityId = activity.getId();
        result.createdAt = activity.getId() == null ? null : DateFormat.asRFC1123(activity.getId());
        result.updatedAt = config.getId() == null ? null : DateFormat.asRFC1123(config.getId());
        result.config = config.getConfig();
        result.plugin = PluginRefPayload.from(plugin, activity.getPluginVersionExpr(), pluginFilters);
        result.creator = creator;
        result.activityTheme = activityTheme.getConfig();
        result.childrenPathways = childrenPathwayIds;
        result.components = componentIds;
        result.studentScopeURN = activity.getStudentScopeURN();
        result.description = elementDescription.getValue();
        result.themePayload = themePayload;
        result.activityThemeIconLibraries = activityThemeIconLibraries;
        return result;
    }

    public UUID getActivityId() {
        return activityId;
    }

    public PluginRefPayload getPlugin() {
        return plugin;
    }

    public AccountPayload getCreator() {
        return creator;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public String getUpdatedAt() {
        return updatedAt;
    }

    public String getConfig() {
        return config;
    }

    public String getActivityTheme() {
        return activityTheme;
    }

    public UUID getParentPathwayId() {
        return parentPathwayId;
    }

    public ActivityPayload setParentPathwayId(UUID parentPathwayId) {
        this.parentPathwayId = parentPathwayId;
        return this;
    }

    public List<UUID> getChildrenPathways() {
        return childrenPathways;
    }

    public List<UUID> getComponents() {
        return components;
    }

    public List<AssetPayload> getAssets() {
        return assets;
    }

    public List<AssetPayload> getMathAssets() {
        return mathAssets;
    }

    public ActivityPayload setAssets(List<AssetPayload> assets) {
        this.assets = assets;
        return this;
    }

    public ActivityPayload setMathAssets(List<AssetPayload> mathAssets) {
        this.mathAssets = mathAssets;
        return this;
    }

    public UUID getStudentScopeURN() {
        return studentScopeURN;
    }

    public List<DocumentItemPayload> getLinkedDocumentItems() {
        return linkedDocumentItems;
    }

    public void setLinkedDocumentItems(List<DocumentItemPayload> linkedDocumentItems) {
        this.linkedDocumentItems = linkedDocumentItems;
    }

    public String getDescription() {
        return description;
    }

    public ThemePayload getThemePayload() {
        return themePayload;
    }

    public List<IconLibrary> getActivityThemeIconLibraries() {
        return activityThemeIconLibraries;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ActivityPayload that = (ActivityPayload) o;
        return Objects.equals(activityId, that.activityId) &&
                Objects.equals(config, that.config) &&
                Objects.equals(plugin, that.plugin) &&
                Objects.equals(creator, that.creator) &&
                Objects.equals(createdAt, that.createdAt) &&
                Objects.equals(updatedAt, that.updatedAt) &&
                Objects.equals(activityTheme, that.activityTheme) &&
                Objects.equals(parentPathwayId, that.parentPathwayId) &&
                Objects.equals(childrenPathways, that.childrenPathways) &&
                Objects.equals(components, that.components) &&
                Objects.equals(assets, that.assets) &&
                Objects.equals(mathAssets, that.mathAssets) &&
                Objects.equals(studentScopeURN, that.studentScopeURN) &&
                Objects.equals(linkedDocumentItems, that.linkedDocumentItems) &&
                Objects.equals(description, that.description) &&
                Objects.equals(themePayload, that.themePayload) &&
                Objects.equals(activityThemeIconLibraries, that.activityThemeIconLibraries);
    }

    @Override
    public int hashCode() {
        return Objects.hash(activityId, config, plugin, creator, createdAt, updatedAt,
                            activityTheme, parentPathwayId,
                            childrenPathways, components, assets, mathAssets, studentScopeURN, linkedDocumentItems,
                            description, themePayload, activityThemeIconLibraries);
    }

    @Override
    public String toString() {
        return "ActivityPayload{" +
                "activityId=" + activityId +
                ", config='" + config + '\'' +
                ", plugin=" + plugin +
                ", creator=" + creator +
                ", createdAt='" + createdAt + '\'' +
                ", updatedAt='" + updatedAt + '\'' +
                ", activityTheme='" + activityTheme + '\'' +
                ", parentPathwayId=" + parentPathwayId +
                ", childrenPathways=" + childrenPathways +
                ", components=" + components +
                ", assets=" + assets +
                ", mathAssets=" + mathAssets +
                ", studentScopeURN=" + studentScopeURN +
                ", linkedDocumentItems=" + linkedDocumentItems +
                ", description='" + description + '\'' +
                ", themePayload='" + themePayload + '\'' +
                ", activityThemeIconLibraries='" + activityThemeIconLibraries + '\'' +
                '}';
    }
}
