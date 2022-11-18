package com.smartsparrow.courseware.pathway;

import java.util.Objects;
import java.util.UUID;

public class FreePathway implements Pathway {

    private UUID id;
    private static PathwayType type = PathwayType.FREE;
    private PreloadPathway preloadPathway;

    FreePathway() {
    }

    @Override
    public UUID getId() {
        return id;
    }

    FreePathway setId(UUID id) {
        this.id = id;
        return this;
    }

    @Override
    public PathwayType getType() {
        return type;
    }

    @Override
    public PreloadPathway getPreloadPathway() {
        return preloadPathway;
    }

    public FreePathway setPreloadPathway(final PreloadPathway preloadPathway) {
        this.preloadPathway = preloadPathway;
        return this;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        FreePathway that = (FreePathway) o;
        return Objects.equals(id, that.id) &&
                preloadPathway == that.preloadPathway;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, preloadPathway);
    }

    @Override
    public String toString() {
        return "FreePathway{" +
                "id=" + id +
                ", preloadPathway=" + preloadPathway +
                '}';
    }
}
