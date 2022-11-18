package com.smartsparrow.graphql.schema;

import com.smartsparrow.graphql.type.Learn;
import com.smartsparrow.learner.data.LearnerInteractive;
import com.smartsparrow.learner.data.LearnerScopeReference;
import com.smartsparrow.learner.service.LearnerService;
import io.leangen.graphql.execution.relay.Page;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import reactor.core.publisher.Flux;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class ScopeRegistrySchemaTest {

    @InjectMocks
    private ScopeRegistrySchema scopeRegistrySchema;
    @Mock
    private LearnerService learnerService;

    private static final UUID deploymentId = UUID.randomUUID();
    private static final UUID changeId = UUID.randomUUID();
    private static final UUID studentScopeURN = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void getScope_Registry() {
        LearnerScopeReference learnerScopeReference1 = mock(LearnerScopeReference.class);
        LearnerScopeReference learnerScopeReference2 = mock(LearnerScopeReference.class);
        LearnerInteractive interactive = new LearnerInteractive();
        interactive.setDeploymentId(deploymentId);
        interactive.setStudentScopeURN(studentScopeURN);
        interactive.setChangeId(changeId);
        when(learnerService.findAllRegistered(studentScopeURN, deploymentId, changeId)).thenReturn(Flux.just(learnerScopeReference1, learnerScopeReference2));

        Page<LearnerScopeReference> scope = scopeRegistrySchema
                .getScopeRegistry(interactive, null,null)
                .join();

        assertNotNull(scope);
        assertNotNull(scope.getEdges());
        assertEquals(2, scope.getEdges().size());
    }
}
