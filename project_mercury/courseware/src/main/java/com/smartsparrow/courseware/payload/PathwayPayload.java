package com.smartsparrow.courseware.payload;

import java.util.List;
import java.util.Objects;
import java.util.UUID;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.smartsparrow.asset.service.AssetPayload;
import com.smartsparrow.courseware.data.CoursewareElementDescription;
import com.smartsparrow.courseware.data.WalkableChild;
import com.smartsparrow.courseware.pathway.Pathway;
import com.smartsparrow.courseware.pathway.PathwayType;

public class PathwayPayload {

    private UUID pathwayId;
    private PathwayType pathwayType;
    private UUID parentActivityId;
    private List<WalkableChild> children;
    private List<AssetPayload> assets;
    private String config;
    private String description;

    public static PathwayPayload from(@Nonnull Pathway pathway,
                                      @Nonnull UUID parentActivityId,
                                      @Nonnull List<WalkableChild> childrenWalkableIds,
                                      final String config,
                                      @Nonnull CoursewareElementDescription elementDescription) {
        PathwayPayload payload = new PathwayPayload();
        payload.pathwayId = pathway.getId();
        payload.pathwayType = pathway.getType();
        payload.parentActivityId = parentActivityId;
        payload.children = childrenWalkableIds;
        payload.config = config;
        payload.description = elementDescription.getValue();
        return payload;
    }

    public void setAssets(List<AssetPayload> assets) {
        this.assets = assets;
    }

    public UUID getPathwayId() {
        return pathwayId;
    }

    public PathwayType getPathwayType() {
        return pathwayType;
    }

    public UUID getParentActivityId() {
        return parentActivityId;
    }

    public List<WalkableChild> getChildren() {
        return children;
    }

    public List<AssetPayload> getAssets() {
        return assets;
    }

    public String getDescription() { return description; }

    @Nullable
    public String getConfig() {
        return config;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PathwayPayload that = (PathwayPayload) o;
        return Objects.equals(pathwayId, that.pathwayId) &&
                pathwayType == that.pathwayType &&
                Objects.equals(parentActivityId, that.parentActivityId) &&
                Objects.equals(children, that.children) &&
                Objects.equals(assets, that.assets) &&
                Objects.equals(config, that.config) &&
                Objects.equals(description, that.description);
    }

    @Override
    public int hashCode() {
        return Objects.hash(pathwayId, pathwayType, parentActivityId, children, assets, config, description);
    }

    @Override
    public String toString() {
        return "PathwayPayload{" +
                "pathwayId=" + pathwayId +
                ", pathwayType=" + pathwayType +
                ", parentActivityId=" + parentActivityId +
                ", children=" + children +
                ", assets=" + assets +
                ", config='" + config + '\'' +
                ", description='" + description + '\'' +
                '}';
    }
}
