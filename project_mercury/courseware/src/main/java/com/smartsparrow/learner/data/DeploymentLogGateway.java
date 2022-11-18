package com.smartsparrow.learner.data;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.datastax.driver.core.Session;
import com.smartsparrow.dse.api.Mutators;

import reactor.core.Exceptions;
import reactor.core.publisher.Flux;

@Singleton
public class DeploymentLogGateway {

    private static final Logger log = LoggerFactory.getLogger(DeploymentLogGateway.class);

    private final Session session;

    private final DeploymentLogMutator deploymentLogMutator;
    private final DeploymentLogByStateMutator deploymentLogByStateMutator;
    private final DeploymentLogByElementMutator deploymentLogByElementMutator;

    // Materializers currently not used
    private final DeploymentLogMaterializer deploymentLogMaterializer;
    private final DeploymentLogByStateMaterializer deploymentLogByStateMaterializer;
    private final DeploymentLogByElementMaterializer deploymentLogByElementMaterializer;

    @Inject
    public DeploymentLogGateway(Session session,
                                DeploymentLogMutator deploymentLogMutator,
                                DeploymentLogByStateMutator deploymentLogByStateMutator,
                                DeploymentLogByElementMutator deploymentLogByElementMutator,
                                DeploymentLogMaterializer deploymentLogMaterializer,
                                DeploymentLogByStateMaterializer deploymentLogByStateMaterializer,
                                DeploymentLogByElementMaterializer deploymentLogByElementMaterializer) {
        this.session = session;
        this.deploymentLogMutator = deploymentLogMutator;
        this.deploymentLogByStateMutator = deploymentLogByStateMutator;
        this.deploymentLogByElementMutator = deploymentLogByElementMutator;
        this.deploymentLogMaterializer = deploymentLogMaterializer;
        this.deploymentLogByStateMaterializer = deploymentLogByStateMaterializer;
        this.deploymentLogByElementMaterializer = deploymentLogByElementMaterializer;
    }

    /**
     * Persist a deployment log step to the database
     *
     * @param deploymentStepLog the deployment step object to log to the db
     * @return a flux of void
     */
    public Flux<Void> persist(DeploymentStepLog deploymentStepLog) {
        return Mutators.execute(session, Flux.just(
           deploymentLogMutator.upsert(deploymentStepLog),
           deploymentLogByStateMutator.upsert(deploymentStepLog),
           deploymentLogByElementMutator.upsert(deploymentStepLog)
        )).doOnError(throwable -> {

            if (log.isErrorEnabled()) {
                log.error("error while persisting deployment log step", throwable);
            }

            throw Exceptions.propagate(throwable);
        });
    }
}
