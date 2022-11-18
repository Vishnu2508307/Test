package com.smartsparrow.courseware.pathway;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.UUID;

import com.datastax.driver.core.utils.UUIDs;

public class LearnerPathwayMock {

    public static LearnerPathway mockLearnerPathway() {
        return new Builder().build();
    }

    public static LearnerPathway mockLearnerPathway(final UUID id) {
        return new Builder().setId(id).build();
    }

    public static LearnerPathway mockLearnerPathway(final UUID id, final UUID deploymentId) {
        return new Builder().setId(id).setDeploymentId(deploymentId).build();
    }

    public static LearnerPathway mockLearnerPathway(final UUID id,
                                                    final PathwayType type,
                                                    final UUID deploymentId,
                                                    final UUID changeId,
                                                    String config,
                                                    PreloadPathway preloadPathway) {
        return new Builder()
                .setId(id)
                .setType(type)
                .setDeploymentId(deploymentId)
                .setChangeId(changeId)
                .setConfig(config)
                .setPreloadPathway(preloadPathway)
                .build();
    }

    private static class Builder {
        private UUID id = UUIDs.timeBased();
        private PathwayType type = PathwayType.LINEAR;
        private UUID deploymentId = UUIDs.timeBased();
        private UUID changeId = UUIDs.timeBased();
        private String config = "{\"foo\":\"bar\"}";
        private PreloadPathway preloadPathway = PreloadPathway.ALL;

        LearnerPathway build() {
            LearnerPathway p = mock(LearnerPathway.class);
            when(p.getId()).thenReturn(id);
            when(p.getType()).thenReturn(type);
            when(p.getDeploymentId()).thenReturn(deploymentId);
            when(p.getChangeId()).thenReturn(changeId);
            when(p.getConfig()).thenReturn(config);
            when(p.getPreloadPathway()).thenReturn(preloadPathway);
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

        public Builder setDeploymentId(UUID deploymentId) {
            this.deploymentId = deploymentId;
            return this;
        }

        public Builder setChangeId(UUID changeId) {
            this.changeId = changeId;
            return this;
        }

        public Builder setConfig(String config) {
            this.config = config;
            return this;
        }

        public Builder setPreloadPathway(final PreloadPathway preloadPathway) {
            this.preloadPathway = preloadPathway;
            return this;
        }
    }
}
