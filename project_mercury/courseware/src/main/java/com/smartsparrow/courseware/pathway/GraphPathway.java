package com.smartsparrow.courseware.pathway;

import java.util.List;
import java.util.Objects;
import java.util.UUID;

import com.google.common.collect.Lists;

public class GraphPathway implements Pathway {

    private static final long serialVersionUID = -4632346949607156883L;

    // name of the configuration fields that defines the starting walkable for the graph pathway
    static final String STARTING_WALKABLE_ID = "startingWalkableId";
    static final String STARTING_WALKABLE_TYPE = "startingWalkableType";

    static final List<String> STARTING_WALKABLE_FIELDS = Lists.newArrayList(
            STARTING_WALKABLE_ID,
            STARTING_WALKABLE_TYPE
    );

    private UUID id;
    private static final PathwayType type = PathwayType.GRAPH;
    private PreloadPathway preloadPathway;

    @Override
    public UUID getId() {
        return id;
    }

    @Override
    public PathwayType getType() {
        return type;
    }

    public GraphPathway setId(UUID id) {
        this.id = id;
        return this;
    }

    @Override
    public PreloadPathway getPreloadPathway() {
        return preloadPathway;
    }

    public GraphPathway setPreloadPathway(final PreloadPathway preloadPathway) {
        this.preloadPathway = preloadPathway;
        return this;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        GraphPathway that = (GraphPathway) o;
        return Objects.equals(id, that.id) &&
                preloadPathway == that.preloadPathway;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, preloadPathway);
    }

    @Override
    public String toString() {
        return "GraphPathway{" +
                "id=" + id +
                ", preloadPathwayType=" + preloadPathway +
                '}';
    }
}
