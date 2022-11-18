package com.smartsparrow.eval.data;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.inject.Inject;

import org.json.JSONObject;

import com.newrelic.api.agent.Trace;
import com.smartsparrow.eval.action.progress.EmptyActionResult;
import com.smartsparrow.eval.action.scope.ChangeScopeAction;
import com.smartsparrow.eval.action.scope.ChangeScopeActionContext;
import com.smartsparrow.eval.mutation.MutationOperation;
import com.smartsparrow.eval.mutation.MutationOperationService;
import com.smartsparrow.eval.mutation.MutationOperator;
import com.smartsparrow.learner.data.Deployment;
import com.smartsparrow.learner.data.StudentScopeEntry;
import com.smartsparrow.learner.service.ChangeScopeEventHandler;
import com.smartsparrow.learner.service.StudentScopeService;
import com.smartsparrow.pubsub.subscriptions.studentscope.StudentScopeProducer;
import com.smartsparrow.util.DataType;
import com.smartsparrow.util.Json;
import com.smartsparrow.util.monitoring.ReactiveTransaction;

import reactor.core.publisher.Mono;

/**
 * Consumes a change scope action
 * TODO missing unit tests, logic copied from {@link ChangeScopeEventHandler}
 */
public class LearnerChangeScopeActionConsumer implements ActionConsumer<ChangeScopeAction, EmptyActionResult> {

    private final ActionConsumerOptions options;
    private final StudentScopeService studentScopeService;
    private final MutationOperationService mutationOperationService;
    private final StudentScopeProducer studentScopeProducer;


    @Inject
    public LearnerChangeScopeActionConsumer(final StudentScopeService studentScopeService,
                                            final MutationOperationService mutationOperationService,
                                            final StudentScopeProducer studentScopeProducer) {
        this.studentScopeService = studentScopeService;
        this.mutationOperationService = mutationOperationService;
        this.studentScopeProducer = studentScopeProducer;
        this.options = new ActionConsumerOptions()
                .setAsync(false);
    }

    @Trace(async = true)
    @SuppressWarnings({"rawtypes", "unchecked", "Duplicates"})
    @Override
    public Mono<EmptyActionResult> consume(ChangeScopeAction changeScopeAction, LearnerEvaluationResponseContext context) {
        final LearnerEvaluationRequest request = context.getResponse()
                .getEvaluationRequest();

        // prepare all the required variables
        final Deployment deployment = request.getDeployment();
        final UUID studentId = request.getStudentId();
        final ChangeScopeActionContext actionContext = changeScopeAction.getContext();
        final UUID deploymentId = deployment.getId();
        final UUID studentScopeURN = actionContext.getStudentScopeURN();
        final UUID sourceId = actionContext.getSourceId();
        final List<String> contextPath = actionContext.getContext();
        final Map<String, String> schemaProperty = actionContext.getSchemaProperty();
        final Object resolvedValue = changeScopeAction.getResolvedValue();
        final UUID timeId = context.getTimeId();

        final String type = schemaProperty.get("type");
        // check if the schema property type is a list
        final boolean isListType = (type != null && type.equals("list"));
        // find the action operator for this change scope
        MutationOperator actionOperator = actionContext.getOperator();
        // find out the data type for the operation
        DataType dataType = actionContext.getDataType();
        // get the mutation operation implementation
        MutationOperation mutationOperation =  mutationOperationService.getMutationOperation(dataType, actionOperator, isListType);

        // find the scope id
        return studentScopeService.findScopeId(deploymentId, studentId, studentScopeURN)
                .doOnEach(ReactiveTransaction.linkOnNext())
                // find the entry for this scope
                .flatMap(scopeId -> studentScopeService.fetchScopeEntry(scopeId, sourceId))
                // read the entry data
                .flatMap(studentScopeEntry -> {
                    // parse the data to a json object
                    final String data = studentScopeEntry.getData();
                    final JSONObject parsed = Json.parse(data);
                    final Object jsonObject = Json.query(parsed, contextPath);
                    final Object unwrappedValue = Json.unwrapValue(jsonObject, dataType);

                    // apply the mutation
                    final Object mutated = mutationOperation.apply(unwrappedValue, resolvedValue);
                    // replace the scope with the mutated data
                    final JSONObject mutatedData = Json.replace(parsed, contextPath, mutated);

                    Mono<StudentScopeEntry> scopeEntryMono;

                    if (timeId != null) {
                        scopeEntryMono = studentScopeService.setStudentScope(deployment,
                                                                             studentId,
                                                                             studentScopeURN,
                                                                             sourceId,
                                                                             mutatedData.toString(),
                                                                             timeId);
                    } else {
                        scopeEntryMono = studentScopeService.setStudentScope(deployment,
                                                                             studentId,
                                                                             studentScopeURN,
                                                                             sourceId,
                                                                             mutatedData.toString());
                    }
                    // set the student scope
                    return scopeEntryMono
                            // publish the mutation to the scope subscription
                            .map(newEntry -> {
                                //produces consumable event
                                studentScopeProducer.buildStudentScopeConsumable(studentId,
                                                                                 deploymentId,
                                                                                 studentScopeURN,
                                                                                 newEntry)
                                        .produce();
                                return Mono.just(newEntry);
                            })
                            // return an empty action context
                            .then(Mono.just(new EmptyActionResult(changeScopeAction)));
                });
    }

    @Override
    public Mono<ActionConsumerOptions> getActionConsumerOptions() {
        return Mono.just(options);
    }

}
