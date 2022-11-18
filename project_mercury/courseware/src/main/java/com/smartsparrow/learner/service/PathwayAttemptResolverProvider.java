package com.smartsparrow.learner.service;

import javax.inject.Inject;

import com.google.inject.Provider;
import com.smartsparrow.courseware.pathway.PathwayType;
import com.smartsparrow.exception.IllegalArgumentFault;

public class PathwayAttemptResolverProvider {

    private final Provider<LinearPathwayAttemptResolver> linearPathwayAttemptResolver;
    private final Provider<FreePathwayAttemptResolver> freePathwayAttemptResolver;
    private final Provider<GraphPathwayAttemptResolver> graphPathwayAttemptResolver;
    private final Provider<RandomPathwayAttemptResolver> randomPathwayAttemptResolver;
    private final Provider<BKTPathwayAttemptResolver> bktPathwayAttemptResolver;

    @Inject
    public PathwayAttemptResolverProvider(Provider<LinearPathwayAttemptResolver> linearPathwayAttemptResolver,
                                          Provider<FreePathwayAttemptResolver> freePathwayAttemptResolver,
                                          Provider<GraphPathwayAttemptResolver> graphPathwayAttemptResolver,
                                          Provider<RandomPathwayAttemptResolver> randomPathwayAttemptResolver,
                                          Provider<BKTPathwayAttemptResolver> bktPathwayAttemptResolver) {
        this.linearPathwayAttemptResolver = linearPathwayAttemptResolver;
        this.freePathwayAttemptResolver = freePathwayAttemptResolver;
        this.graphPathwayAttemptResolver = graphPathwayAttemptResolver;
        this.randomPathwayAttemptResolver = randomPathwayAttemptResolver;
        this.bktPathwayAttemptResolver = bktPathwayAttemptResolver;
    }

    public PathwayAttemptResolver get(final PathwayType type) {
        switch (type) {
            case LINEAR:
                return linearPathwayAttemptResolver.get();
            case FREE:
                return freePathwayAttemptResolver.get();
            case GRAPH:
                return graphPathwayAttemptResolver.get();
            case RANDOM:
                return randomPathwayAttemptResolver.get();
            case ALGO_BKT:
                return bktPathwayAttemptResolver.get();
            default:
                throw new IllegalArgumentFault("Unsupported pathway type " + type);
        }
    }

}
