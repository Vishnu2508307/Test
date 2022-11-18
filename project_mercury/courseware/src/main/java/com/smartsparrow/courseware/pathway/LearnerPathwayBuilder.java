package com.smartsparrow.courseware.pathway;

import java.util.Map;
import java.util.UUID;

import javax.annotation.concurrent.NotThreadSafe;
import javax.inject.Inject;
import javax.inject.Provider;

import com.smartsparrow.exception.IllegalArgumentFault;

public class LearnerPathwayBuilder {

    private final Map<PathwayType, Provider<LearnerPathway>> learnerPathwayProvider;

    @Inject
    public LearnerPathwayBuilder(Map<PathwayType, Provider<LearnerPathway>> learnerPathwayProvider) {
        this.learnerPathwayProvider = learnerPathwayProvider;
    }

    public LearnerPathway build(final PathwayType type,
                                final UUID id,
                                final UUID deploymentId,
                                final UUID changeId,
                                final String config,
                                final PreloadPathway preloadPathwayType) {

        Provider<LearnerPathway> provider = learnerPathwayProvider.get(type);
        PreloadPathway preloadPathway = preloadPathwayType != null ? preloadPathwayType : PreloadPathway.NONE;

        // FIXME: affirm it is a valid registered type (i.e. not null)

        switch (type) {
        case LINEAR:
            LinearLearnerPathway linearLearnerPathway = (LinearLearnerPathway) provider.get();
            return linearLearnerPathway
                    .setId(id)
                    .setDeploymentId(deploymentId) //
                    .setChangeId(changeId)
                    .setConfig(config)
                    .setPreloadPathway(preloadPathway);
        case FREE:
            FreeLearnerPathway freeLearnerPathway = (FreeLearnerPathway) provider.get();
            return freeLearnerPathway //
                    .setId(id) //
                    .setDeploymentId(deploymentId) //
                    .setChangeId(changeId)
                    .setConfig(config)
                    .setPreloadPathway(preloadPathway);
        case GRAPH:
            LearnerGraphPathway learnerGraphPathway = (LearnerGraphPathway) provider.get();
            return learnerGraphPathway//
                    .setId(id) //
                    .setDeploymentId(deploymentId) //
                    .setChangeId(changeId)
                    .setConfig(config)
                    .setPreloadPathway(preloadPathway);

        case RANDOM:
            LearnerRandomPathway learnerRandomPathway = (LearnerRandomPathway) provider.get();
            return learnerRandomPathway
                    .setId(id)
                    .setDeploymentId(deploymentId)
                    .setChangeId(changeId)
                    .setConfig(config)
                    .setPreloadPathway(preloadPathway);
        case ALGO_BKT:
            LearnerBKTPathway learnerBKTPathway = (LearnerBKTPathway) provider.get();
            return learnerBKTPathway
                    .setId(id)
                    .setDeploymentId(deploymentId)
                    .setChangeId(changeId)
                    .setConfig(config)
                    .setPreloadPathway(preloadPathway);
        default:
            throw new IllegalArgumentFault("Invalid pathway type: " + type);
        }
    }
}
