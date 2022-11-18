package com.smartsparrow.graphql.schema;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.smartsparrow.exception.IllegalArgumentFault;
import com.smartsparrow.graphql.type.Learn;
import com.smartsparrow.learner.data.DeployedActivity;
import com.smartsparrow.learner.service.DeploymentService;

import reactor.core.publisher.Mono;

class ProductSchemaTest {

    @InjectMocks
    private ProductSchema productSchema;

    @Mock
    private DeploymentService deploymentService;

    private static final Learn learn = new Learn();
    private static final String productId = UUID.randomUUID().toString();
    private static final UUID cohortId = UUID.randomUUID();
    private static final UUID deploymentId = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void getProductDeployment_productIdNotFound() {
        when(deploymentService.findProductDeploymentId(productId)).thenReturn(Mono.empty());

        assertThrows(IllegalArgumentFault.class, () -> productSchema.getProductDeployment(learn, productId));
    }

    @Test
    void getProductDeployment_deploymentIdNotFound() {
        when(deploymentService.findProductDeploymentId(productId)).thenReturn(Mono.just(deploymentId));
        when(deploymentService.findDeployment(deploymentId)).thenReturn(Mono.empty());

        assertThrows(IllegalArgumentFault.class, () -> productSchema.getProductDeployment(learn, productId));
    }

    @Test
    void getCohortDeployment_success() {
        when(deploymentService.findProductDeploymentId(productId)).thenReturn(Mono.just(deploymentId));
        when(deploymentService.findDeployment(deploymentId)).thenReturn(Mono.just(new DeployedActivity().setCohortId(cohortId)));

        DeployedActivity result = productSchema.getProductDeployment(learn, productId);

        assertNotNull(result);
    }
}
