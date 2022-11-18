package com.smartsparrow.courseware.pathway;

import java.util.Objects;
import java.util.UUID;

/**
 * A linear pathway.
 */
public class LinearPathway implements Pathway {

    private UUID id;
    private static PathwayType type = PathwayType.LINEAR;
    private PreloadPathway preloadPathway;

    LinearPathway() {
    }

    @Override
    public UUID getId() {
        return id;
    }

    LinearPathway setId(UUID id) {
        this.id = id;
        return this;
    }

    @Override
    public PathwayType getType() {
        return type;
    }

    public static void setType(final PathwayType type) {
        LinearPathway.type = type;
    }

    @Override
    public PreloadPathway getPreloadPathway() {
        return preloadPathway;
    }

    public LinearPathway setPreloadPathway(final PreloadPathway preloadPathway) {
        this.preloadPathway = preloadPathway;
        return this;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        LinearPathway that = (LinearPathway) o;
        return Objects.equals(id, that.id) &&
                preloadPathway == that.preloadPathway;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, preloadPathway);
    }

    @Override
    public String toString() {
        return "LinearPathway{" +
                "id=" + id +
                ", preloadPathway=" + preloadPathway +
                '}';
    }
}
