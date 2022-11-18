package com.smartsparrow.courseware.pathway;

import java.util.Map;
import java.util.UUID;

import javax.inject.Inject;
import javax.inject.Provider;

import com.smartsparrow.exception.IllegalArgumentFault;

public class PathwayBuilder {
    //
    private final Map<PathwayType, Provider<Pathway>> pathwayProvider;

    //
    @Inject
    public PathwayBuilder(Map<PathwayType, Provider<Pathway>> pathwayProvider) {
        this.pathwayProvider = pathwayProvider;
    }

    public Pathway build(final PathwayType type, final UUID pathwayId, final PreloadPathway preloadPathwayType) {

        PreloadPathway preloadPathway = preloadPathwayType != null ? preloadPathwayType : PreloadPathway.NONE;
        //
        Provider<Pathway> provider = pathwayProvider.get(type);
        // FIXME: affirm it is a valid registered type (i.e. not null)

        switch (type) {
        case LINEAR:
            LinearPathway linearPathway = (LinearPathway)provider.get();
            return linearPathway //
                    .setId(pathwayId)
                    .setPreloadPathway(preloadPathway);
        case FREE:
            FreePathway freePathway = (FreePathway) provider.get();
            return freePathway //
                    .setId(pathwayId)
            .setPreloadPathway(preloadPathway);

        case GRAPH:
            GraphPathway graphPathway = (GraphPathway) provider.get();
            return graphPathway
                    .setId(pathwayId)
                    .setPreloadPathway(preloadPathway);

        case RANDOM:
            RandomPathway randomPathway = (RandomPathway) provider.get();
            return randomPathway
                    .setId(pathwayId)
                    .setPreloadPathway(preloadPathway);
        case ALGO_BKT:
            BKTPathway bktPathway = (BKTPathway) provider.get();
            return bktPathway
                    .setId(pathwayId)
                    .setPreloadPathway(preloadPathway);
        default:
            throw new IllegalArgumentFault("Invalid pathway type: " + type);
        }
    }
}
