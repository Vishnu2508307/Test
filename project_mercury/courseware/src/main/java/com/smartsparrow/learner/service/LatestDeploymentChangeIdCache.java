package com.smartsparrow.learner.service;

import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;

import javax.annotation.Nullable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.cache.LoadingCache;

public class LatestDeploymentChangeIdCache {

    private Logger log = LoggerFactory.getLogger(LatestDeploymentChangeIdCache.class);
    private LoadingCache<UUID, UUID> cache;

    /**
     * Wrapper around a @Singleton instance of LoadingCache<UUID, UUID> that stores latest change id for a deploymentId
     * Instances of this object should be acquired by a provider
     *
     */
    public LatestDeploymentChangeIdCache(LoadingCache<UUID, UUID> cache) {
        this.cache = cache;
    }

    /**
     * Retrieves from in memory cache the latest changeId for given deploymentId, if not cached, fetches from db
     * and caches it
     *
     * May return null if either load cant find a value or unexpected error {@link ExecutionException} or
     * {@link ClassCastException} occurred.
     *
     * @param deploymentId key id of deploymentId
     * @return
     */
    @Nullable
    UUID get(UUID deploymentId) {
        try {
            return cache.get(deploymentId);
        } catch (ExecutionException | ClassCastException | NullPointerException e) {
            log.error("Failed looking up cached  key " + deploymentId, e);
            return null;
        }
    }

}
