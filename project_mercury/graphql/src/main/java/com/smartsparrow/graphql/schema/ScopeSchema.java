package com.smartsparrow.graphql.schema;

import static com.smartsparrow.util.Warrants.affirmArgument;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.smartsparrow.graphql.BronteGQLContext;
import com.smartsparrow.learner.data.Evaluation;
import com.smartsparrow.learner.data.LearnerWalkable;
import com.smartsparrow.learner.payload.StudentScopePayload;
import com.smartsparrow.learner.service.EvaluationResultService;
import com.smartsparrow.learner.service.StudentScopeService;

import io.leangen.graphql.annotations.GraphQLContext;
import io.leangen.graphql.annotations.GraphQLEnvironment;
import io.leangen.graphql.annotations.GraphQLQuery;
import io.leangen.graphql.execution.ResolutionEnvironment;

@Singleton
public class ScopeSchema {

    private final StudentScopeService studentScopeService;
    private final EvaluationResultService evaluationResultService;

    @Inject
    public ScopeSchema(StudentScopeService studentScopeService,
                       EvaluationResultService evaluationResultService) {
        this.studentScopeService = studentScopeService;
        this.evaluationResultService = evaluationResultService;
    }

    @GraphQLQuery(name = "scope", description = "Scope data")
    public CompletableFuture<List<StudentScopePayload>> getScope(@GraphQLEnvironment ResolutionEnvironment resolutionEnvironment,
                                                                 @GraphQLContext LearnerWalkable walkable) {
        BronteGQLContext context = resolutionEnvironment.dataFetchingEnvironment.getContext();

        UUID accountId = context.getAuthenticationContext().getAccount().getId();
        return studentScopeService.fetchScope(walkable.getDeploymentId(), accountId, walkable.getStudentScopeURN(), walkable.getChangeId()).
                collectList()
                .toFuture();
    }

    @GraphQLQuery(name = "scope", description = "Historic scope data")
    public CompletableFuture<List<StudentScopePayload>> getScope(@GraphQLContext Evaluation evaluation) {

        affirmArgument(evaluation.getId() != null, "evaluation id is required");

        return evaluationResultService.fetchHistoricScope(evaluation.getId())
                .toFuture();
    }

}
