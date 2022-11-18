package com.smartsparrow.graphql.schema;

import static com.smartsparrow.util.Warrants.affirmArgument;

import java.util.UUID;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.newrelic.api.agent.Trace;
import com.smartsparrow.graphql.type.Learn;
import com.smartsparrow.learner.data.DeployedActivity;
import com.smartsparrow.learner.service.DeploymentService;

import com.smartsparrow.util.monitoring.ReactiveMonitoring;
import com.smartsparrow.util.monitoring.ReactiveTransaction;
import io.leangen.graphql.annotations.GraphQLArgument;
import io.leangen.graphql.annotations.GraphQLContext;
import io.leangen.graphql.annotations.GraphQLNonNull;
import io.leangen.graphql.annotations.GraphQLQuery;

@Singleton
public class ProductSchema {

    private final DeploymentService deploymentService;

    @Inject
    public ProductSchema(DeploymentService deploymentService) {
        this.deploymentService = deploymentService;
    }

    @Trace(dispatcher = true, nameTransaction = false, metricName = "Product.deployment")
    @GraphQLQuery(name = "deploymentByProduct", description = "Fetch latest deployment data by product id")
    public DeployedActivity getProductDeployment(@GraphQLContext Learn learn,
                                                 @GraphQLArgument(name = "productId", description = "Product id to fetch deployment by") @GraphQLNonNull String productId) {
        UUID deploymentId = deploymentService.findProductDeploymentId(productId).block();
        affirmArgument(deploymentId != null, "Product does not exist");

        DeployedActivity deployment = deploymentService.findDeployment(deploymentId)
                .doOnEach(ReactiveTransaction.linkOnNext())
                .doOnEach(ReactiveTransaction.expireOnComplete())
                .subscriberContext(ReactiveMonitoring.createContext())
                .block();
        affirmArgument(deployment != null, "Deployment does not exist");

        return deployment;

    }

}
