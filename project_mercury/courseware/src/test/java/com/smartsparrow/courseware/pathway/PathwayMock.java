package com.smartsparrow.courseware.pathway;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.UUID;

import com.datastax.driver.core.utils.UUIDs;

public class PathwayMock {

    public static Pathway mockPathway() {
        return new Builder().build();
    }

    public static Pathway mockPathway(final UUID id) {
        return new Builder().setId(id).build();
    }

    public static Pathway mockPathway(final UUID id, final PathwayType type) {
        return new Builder().setId(id).setType(type).build();
    }

    private static class Builder {
        private UUID id = UUIDs.timeBased();
        private PathwayType type = PathwayType.LINEAR;

        Pathway build() {
            Pathway p = mock(Pathway.class);
            when(p.getId()).thenReturn(id);
            when(p.getType()).thenReturn(type);
            when(p.getPreloadPathway()).thenReturn(PreloadPathway.NONE);
            return p;
        }

        public Builder setId(UUID id) {
            this.id = id;
            return this;
        }

        public Builder setType(PathwayType type) {
            this.type = type;
            return this;
        }
    }
}
