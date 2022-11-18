package com.smartsparrow.learner.data;

import java.util.UUID;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.datastax.driver.core.Session;
import com.smartsparrow.dse.api.ResultSets;

import reactor.core.publisher.Mono;

@Singleton
public class DeploymentChangeIdGateway {

    private static final Logger log = LoggerFactory.getLogger(DeploymentChangeIdGateway.class);

    private final Session session;
    private final DeploymentMaterializer deploymentMaterializer;

    /**
     * Whole reason for this isolated gateway is to avoid circular dependencies when trying to efficiently cache the
     * latest changeId of deployments
     *
     * @param session
     * @param deploymentMaterializer
     */
    @Inject
    public DeploymentChangeIdGateway(Session session, DeploymentMaterializer deploymentMaterializer) {
        this.session = session;
        this.deploymentMaterializer = deploymentMaterializer;
    }

    public Mono<UUID> findLatestChangeId(UUID id) {
        return ResultSets.query(session, deploymentMaterializer.findLatestChangeIds(id, 1))
                .flatMapIterable(row -> row)
                .map(row -> row.getUUID("change_id"))
                .singleOrEmpty();
    }

}
