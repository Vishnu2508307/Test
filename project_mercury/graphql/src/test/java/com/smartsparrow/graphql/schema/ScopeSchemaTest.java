package com.smartsparrow.graphql.schema;

import static graphql.schema.DataFetchingEnvironmentImpl.newDataFetchingEnvironment;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.smartsparrow.exception.IllegalArgumentFault;
import com.smartsparrow.graphql.BronteGQLContext;
import com.smartsparrow.iam.service.Account;
import com.smartsparrow.iam.service.AuthenticationContext;
import com.smartsparrow.iam.service.MutableAuthenticationContext;
import com.smartsparrow.iam.wiring.AuthenticationContextProvider;
import com.smartsparrow.learner.data.Evaluation;
import com.smartsparrow.learner.data.LearnerInteractive;
import com.smartsparrow.learner.payload.StudentScopePayload;
import com.smartsparrow.learner.service.EvaluationResultService;
import com.smartsparrow.learner.service.StudentScopeService;

import io.leangen.graphql.execution.ResolutionEnvironment;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

class ScopeSchemaTest {

    @InjectMocks
    private ScopeSchema scopeSchema;
    @Mock
    private StudentScopeService studentScopeService;
    @Mock
    private AuthenticationContextProvider authenticationContextProvider;
    @Mock
    private EvaluationResultService evaluationResultService;
    @Mock
    private MutableAuthenticationContext mutableAuthenticationContext;
    private ResolutionEnvironment resolutionEnvironment;

    private static final UUID accountId = UUID.randomUUID();
    private static final UUID deploymentId = UUID.randomUUID();
    private static final UUID changeId = UUID.randomUUID();
    private static final UUID scopeURN = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        AuthenticationContext authenticationContext = mock(AuthenticationContext.class);
        when(authenticationContextProvider.get()).thenReturn(authenticationContext);
        Account account = mock(Account.class);
        when(authenticationContext.getAccount()).thenReturn(account);
        when(account.getId()).thenReturn(accountId);
        when(evaluationResultService.fetchHistoricScope(any(UUID.class))).thenReturn(Mono.just(new ArrayList<>()));
        resolutionEnvironment = new ResolutionEnvironment(
                null,
                newDataFetchingEnvironment()
                        .context(new BronteGQLContext()
                                         .setMutableAuthenticationContext(mutableAuthenticationContext)
                                         .setAuthenticationContext(authenticationContextProvider.get())).build(),
                null,
                null,
                null,
                null);
    }

    @Test
    void getScope() {
        StudentScopePayload payload1 = mock(StudentScopePayload.class);
        StudentScopePayload payload2 = mock(StudentScopePayload.class);
        LearnerInteractive interactive = new LearnerInteractive();
        interactive.setDeploymentId(deploymentId);
        interactive.setStudentScopeURN(scopeURN);
        interactive.setChangeId(changeId);
        when(studentScopeService.fetchScope(deploymentId, accountId, scopeURN, changeId)).thenReturn(Flux.just(payload1, payload2));

        List<StudentScopePayload> result = scopeSchema.getScope(resolutionEnvironment,interactive).join();

        assertNotNull(result);
        assertEquals(2, result.size());
    }

    @Test
    void getScope_noScope() {
        LearnerInteractive interactive = new LearnerInteractive();
        interactive.setDeploymentId(deploymentId);
        interactive.setStudentScopeURN(scopeURN);
        interactive.setChangeId(changeId);
        when(studentScopeService.fetchScope(deploymentId, accountId, scopeURN, changeId)).thenReturn(Flux.empty());

        List<StudentScopePayload> result = scopeSchema.getScope(resolutionEnvironment, interactive).join();

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void getScope_historic_null_id() {
        Evaluation evaluation = new Evaluation();

        assertThrows(IllegalArgumentFault.class, () -> scopeSchema.getScope(evaluation));

        verify(evaluationResultService, never()).fetchHistoricScope(any(UUID.class));
    }

    @Test
    void getScope_historic() {
        Evaluation evaluation = new Evaluation()
                .setId(UUID.randomUUID());

        List<StudentScopePayload> scope = scopeSchema.getScope(evaluation).join();

        assertNotNull(scope);

        verify(evaluationResultService).fetchHistoricScope(any(UUID.class));
    }
}
