package com.smartsparrow.learner.service;

import static com.smartsparrow.courseware.route.CoursewareRoutes.EVALUATION_EVENT_MESSAGE;
import static com.smartsparrow.util.Warrants.affirmArgument;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.inject.Inject;

import org.apache.camel.Exchange;
import org.apache.camel.Handler;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.smartsparrow.eval.action.scope.ChangeScopeAction;
import com.smartsparrow.eval.action.scope.ChangeScopeActionContext;
import com.smartsparrow.eval.mutation.MutationOperation;
import com.smartsparrow.eval.mutation.MutationOperationService;
import com.smartsparrow.eval.mutation.MutationOperator;
import com.smartsparrow.learner.data.Deployment;
import com.smartsparrow.learner.data.StudentScopeEntry;
import com.smartsparrow.learner.event.EvaluationEventMessage;
import com.smartsparrow.pubsub.subscriptions.studentscope.StudentScopeProducer;
import com.smartsparrow.util.DataType;
import com.smartsparrow.util.Json;

public class ChangeScopeEventHandler {

    private static final Logger log = LoggerFactory.getLogger(ChangeScopeEventHandler.class);

    private final DeploymentService deploymentService;
    private final StudentScopeService studentScopeService;
    private final MutationOperationService mutationOperationService;
    private final StudentScopeProducer studentScopeProducer;

    @Inject
    public ChangeScopeEventHandler(final DeploymentService deploymentService,
                                   final StudentScopeService studentScopeService,
                                   final MutationOperationService mutationOperationService,
                                   final StudentScopeProducer studentScopeProducer) {
        this.deploymentService = deploymentService;
        this.studentScopeService = studentScopeService;
        this.mutationOperationService = mutationOperationService;
        this.studentScopeProducer = studentScopeProducer;
    }

    @SuppressWarnings("unchecked")
    @Handler
    public void handle(Exchange exchange) {

        final EvaluationEventMessage eventMessage = exchange.getProperty(EVALUATION_EVENT_MESSAGE, EvaluationEventMessage.class);
        final UUID studentId = eventMessage.getStudentId();
        final ChangeScopeAction action = exchange.getIn().getBody(ChangeScopeAction.class);
        final ChangeScopeActionContext context = action.getContext();
        final UUID deploymentId = eventMessage.getDeploymentId();
        final UUID studentScopeURN = context.getStudentScopeURN();
        final UUID sourceId = context.getSourceId();
        final List<String> contextPath = context.getContext();
        final Map<String, String> schemaProperty = context.getSchemaProperty();
        final String type = schemaProperty.get("type");
        final boolean isListType = (type != null && type.equals("list"));
        final Object resolvedValue = action.getResolvedValue();

        if (log.isDebugEnabled()) {
            log.debug("About to set scope data via CHANGE_SCOPE action for student {}, deployment {}, studentScopeURN {}",
                    studentId, deploymentId, context.getStudentScopeURN());
        }

        final Deployment deployment = deploymentService.findDeployment(deploymentId)
                .block();

        // it should find the operator implementation
        MutationOperator actionOperator = context.getOperator();
        DataType dataType = context.getDataType();

        MutationOperation mutationOperation =  mutationOperationService.getMutationOperation(dataType, actionOperator, isListType);

        // it should find the value
        StudentScopeEntry entry = studentScopeService.findScopeId(deploymentId, studentId, studentScopeURN)
                .flatMap(scopeId -> studentScopeService.fetchScopeEntry(scopeId, sourceId))
                .block();

        affirmArgument(entry != null, "scopeEntry not found");

        String data = entry.getData();
        JSONObject parsed = Json.parse(data);
        Object jsonObject = Json.query(parsed, contextPath);

        Object unwrappedValue = Json.unwrapValue(jsonObject, dataType);

        Object mutated = mutationOperation.apply(unwrappedValue, resolvedValue);

        JSONObject mutatedData = Json.replace(parsed, contextPath, mutated);

        StudentScopeEntry newEntry = studentScopeService
                .setStudentScope(deployment, studentId, studentScopeURN, sourceId, mutatedData.toString()).block();

        //produces consumable event
        studentScopeProducer.buildStudentScopeConsumable(studentId, deploymentId, studentScopeURN, newEntry)
                .produce();

    }

}
