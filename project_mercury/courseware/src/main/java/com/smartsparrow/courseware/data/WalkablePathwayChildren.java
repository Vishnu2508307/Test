package com.smartsparrow.courseware.data;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

public class WalkablePathwayChildren {

    private UUID pathwayId;
    private List<UUID> walkableIds;
    private Map<UUID, String> walkableTypes;

    public UUID getPathwayId() {
        return pathwayId;
    }

    public WalkablePathwayChildren setPathwayId(UUID pathwayId) {
        this.pathwayId = pathwayId;
        return this;
    }

    public List<UUID> getWalkableIds() {
        return walkableIds;
    }

    public WalkablePathwayChildren setWalkableIds(List<UUID> walkableIds) {
        this.walkableIds = walkableIds;
        return this;
    }

    public Map<UUID, String> getWalkableTypes() {
        return walkableTypes;
    }

    public WalkablePathwayChildren setWalkableTypes(Map<UUID, String> walkableTypes) {
        this.walkableTypes = walkableTypes;
        return this;
    }

    /**
     * Convenient method to insert walkable at the end of children list
     */
    public WalkablePathwayChildren addWalkable(UUID walkableId, String walkableType) {
        initialise();

        walkableIds.add(walkableId);
        walkableTypes.put(walkableId, walkableType);

        return this;
    }

    /**
     * Convenient method to insert walkable at the specified position of children list
     */
    public WalkablePathwayChildren addWalkable(UUID walkableId, String walkableType, int index) {
        initialise();

        walkableIds.add(index, walkableId);
        walkableTypes.put(walkableId, walkableType);

        return this;
    }

    private void initialise() {
        if (walkableIds == null) {
            walkableIds = Lists.newArrayList();
        }
        if (walkableTypes == null) {
            walkableTypes = Maps.newHashMap();
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        WalkablePathwayChildren that = (WalkablePathwayChildren) o;
        return Objects.equals(pathwayId, that.pathwayId) &&
                Objects.equals(walkableIds, that.walkableIds) &&
                Objects.equals(walkableTypes, that.walkableTypes);
    }

    @Override
    public int hashCode() {
        return Objects.hash(pathwayId, walkableIds, walkableTypes);
    }

    @Override
    public String toString() {
        return "WalkablePathwayChildren{" +
                "pathwayId=" + pathwayId +
                ", walkableIds=" + walkableIds +
                ", walkableTypes=" + walkableTypes +
                '}';
    }
}
