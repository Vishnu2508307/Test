package com.smartsparrow.courseware.pathway;

import java.util.Objects;
import java.util.UUID;

public class RandomPathway implements Pathway {

    // name of the required configuration field that defines when the random pathway is completed
    static final String EXIT_AFTER = "exitAfter";
    private static final long serialVersionUID = -6390900587888637177L;

    private UUID id;
    private PathwayType type = PathwayType.RANDOM;
    private PreloadPathway preloadPathway;

    @Override
    public UUID getId() {
        return id;
    }

    @Override
    public PathwayType getType() {
        return type;
    }

    public RandomPathway setId(final UUID id) {
        this.id = id;
        return this;
    }

    @Override
    public PreloadPathway getPreloadPathway() {
        return preloadPathway;
    }

    public RandomPathway setPreloadPathway(final PreloadPathway preloadPathway) {
        this.preloadPathway = preloadPathway;
        return this;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        RandomPathway that = (RandomPathway) o;
        return Objects.equals(id, that.id) &&
                type == that.type &&
                preloadPathway == that.preloadPathway;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, type, preloadPathway);
    }

    @Override
    public String toString() {
        return "RandomPathway{" +
                "id=" + id +
                ", type=" + type +
                ", preloadPathway=" + preloadPathway +
                '}';
    }
}
