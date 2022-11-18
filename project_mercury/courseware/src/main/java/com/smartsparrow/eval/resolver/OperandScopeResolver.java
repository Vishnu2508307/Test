package com.smartsparrow.eval.resolver;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.UUID;

import javax.inject.Inject;

import org.json.JSONException;
import org.json.JSONObject;

import com.newrelic.api.agent.Trace;
import com.rometools.utils.Lists;
import com.smartsparrow.eval.lang.UnableToResolveException;
import com.smartsparrow.eval.parser.Operand;
import com.smartsparrow.eval.parser.ScopeContext;
import com.smartsparrow.learner.data.EvaluationContext;
import com.smartsparrow.learner.data.EvaluationLearnerContext;
import com.smartsparrow.learner.data.EvaluationTestContext;
import com.smartsparrow.learner.service.StudentScopeService;
import com.smartsparrow.util.DataType;
import com.smartsparrow.util.Json;

import reactor.core.publisher.Mono;

public class OperandScopeResolver implements Resolver<Operand, DataType, EvaluationContext> {

    private final StudentScopeService studentScopeService;

    @Inject
    public OperandScopeResolver(StudentScopeService studentScopeService) {
        this.studentScopeService = studentScopeService;
    }

    @Override
    public Mono<Operand> resolve(final Operand resolvable, final EvaluationContext context) {
        return null;
    }

    /**
     * Resolve the operand scope value at runtime.
     * TODO validate that the field is of valid type
     *
     * @param operand the operand to resolve the value for
     * @param operandType the data type to resolve the operand value to
     * @param evaluationContext the evaluation context for which the operand should be resolved
     * @throws UnableToResolveException when unable to read and extract the value
     */
    @Trace(async = true)
    @Override
    public Mono<Operand> resolve(Operand operand, DataType operandType, EvaluationContext evaluationContext) {
        final ScopeContext scopeContext = (ScopeContext) operand.getResolver();

        if (evaluationContext.getType().equals(EvaluationContext.Type.LEARNER)) {
            // resolve the operand in a learner context
            return resolve(scopeContext, (EvaluationLearnerContext) evaluationContext)
                    .map(resolvedValue -> new Operand()
                            .setResolvedValue(Json._unwrapValue(resolvedValue, operandType))
                            .setResolver(operand.getResolver()));
        }
        // resolve the operand in a test context
        return resolve(scopeContext, (EvaluationTestContext) evaluationContext)
                .map(resolvedValue -> new Operand()
                        .setResolvedValue(Json._unwrapValue(resolvedValue, operandType))
                        .setResolver(operand.getResolver()));
    }

    /**
     * Read the scope entry from the evaluation test context and return the data at the path specified in
     * the {@link ScopeContext}
     *
     * @param scopeContext the scope context to resolve
     * @param evaluationTestContext the test evaluation context to resolve the operand for
     * @return a mono of object representing the resolved value
     * @throws UnableToResolveException when the scopeEntry is not found in the test data
     */
    private Mono<Object> resolve(final ScopeContext scopeContext, final EvaluationTestContext evaluationTestContext) {
        // TODO verify with the frontend if the sourceId is ok to use
        final UUID sourceId = scopeContext.getSourceId();

        try {
            JSONObject testData = Json.parse(evaluationTestContext.getData());
            JSONObject scopeEntry = testData.getJSONObject(sourceId.toString());

            return Mono.just(extract(scopeEntry.toString(), scopeContext.getContext()));
        } catch (JSONException e) {
            throw new UnableToResolveException(String.format("could not find test scopeEntry %s in %s",
                    sourceId, evaluationTestContext.getData()));
        }
    }

    /**
     * Find the student scope entry and return the data at the path specified in the {@link ScopeContext}
     *
     * @param scopeContext the scope context to resolve
     * @param evaluationLearnerContext the learner evaluation context to resolve the operand for
     * @return a mono of object representing the resolved value
     */
    private Mono<Object> resolve(ScopeContext scopeContext, EvaluationLearnerContext evaluationLearnerContext) {

        final UUID deploymentId = evaluationLearnerContext.getDeploymentId();
        final UUID studentId = evaluationLearnerContext.getStudentId();
        final UUID sourceId = scopeContext.getSourceId();
        final UUID studentScopeUrn = scopeContext.getStudentScopeURN();
        final List<String> contextPath = scopeContext.getContext();

        return studentScopeService.findScopeId(deploymentId, studentId, studentScopeUrn)
                .single()
                .doOnError(NoSuchElementException.class, ex -> {
                    throw new UnableToResolveException(String.format("scope id not found for %s", evaluationLearnerContext.toString()));
                })
                .flatMap(scopeId -> studentScopeService.fetchScopeEntry(scopeId, sourceId))
                .single()
                .doOnError(NoSuchElementException.class, ex -> {
                    throw new UnableToResolveException(String.format("student scope entry not found for %s", evaluationLearnerContext));
                })
                .map(scopeEntry -> extract(scopeEntry.getData(), contextPath));

    }

    /**
     * Extract the data from the json string for the specified context path
     *
     * @param jsonString  the json string to extract the data from
     * @param contextPath the json path to extract the data at
     * @return an {@link Object} representing the value
     * @throws JSONException            when failing to parse the json string
     * @throws UnableToResolveException when the jsonPath is <code>null</code>
     */
    private Object extract(String jsonString, List<String> contextPath) {

        JSONObject json = Json.parse(jsonString);

        if (Lists.isEmpty(contextPath)) {
            throw new UnableToResolveException("context is not defined");
        }

        String jsonPath = "/" + String.join("/", contextPath);

        Object result = json.query(jsonPath);

        if (result == null) {
            throw new UnableToResolveException(String.format("could not find path %s", contextPath.toString()));
        }

        return result;
    }

}
