package com.smartsparrow.rtm.message.handler.learner;

import static com.smartsparrow.util.Warrants.affirmArgument;

import java.util.HashMap;
import java.util.UUID;

import javax.inject.Inject;

import org.apache.http.HttpStatus;
import org.eclipse.jetty.websocket.api.Session;

import com.newrelic.api.agent.Trace;
import com.smartsparrow.iam.wiring.AuthenticationContextProvider;
import com.smartsparrow.learner.data.StudentScopeEntry;
import com.smartsparrow.learner.lang.DataValidationException;
import com.smartsparrow.learner.lang.InvalidFieldsException;
import com.smartsparrow.learner.service.DeploymentService;
import com.smartsparrow.learner.service.StudentScopeService;
import com.smartsparrow.pubsub.subscriptions.studentscope.StudentScopeProducer;
import com.smartsparrow.rtm.lang.RTMValidationException;
import com.smartsparrow.rtm.message.MessageHandler;
import com.smartsparrow.rtm.message.recv.learner.SetStudentScopeMessage;
import com.smartsparrow.rtm.message.send.BasicResponseMessage;
import com.smartsparrow.rtm.util.Responses;
import com.smartsparrow.util.log.MercuryLogger;
import com.smartsparrow.util.log.MercuryLoggerFactory;
import com.smartsparrow.util.log.ReactiveMdc;
import com.smartsparrow.util.monitoring.ReactiveMonitoring;
import com.smartsparrow.util.monitoring.ReactiveTransaction;

import reactor.core.Exceptions;

public class SetStudentScopeMessageHandler implements MessageHandler<SetStudentScopeMessage> {

    private static final MercuryLogger log = MercuryLoggerFactory.getLogger(SetStudentScopeMessageHandler.class);

    public static final String LEARNER_STUDENT_SCOPE_SET = "learner.student.scope.set";
    public static final String LEARNER_STUDENT_SCOPE_SET_OK = "learner.student.scope.set.ok";
    public static final String LEARNER_STUDENT_SCOPE_SET_ERROR = "learner.student.scope.set.error";

    private final AuthenticationContextProvider authenticationContextProvider;
    private final StudentScopeService studentScopeService;
    private final DeploymentService deploymentService;
    private final StudentScopeProducer studentScopeProducer;

    @Inject
    public SetStudentScopeMessageHandler(AuthenticationContextProvider authenticationContextProvider,
                                         StudentScopeService studentScopeService,
                                         DeploymentService deploymentService,
                                         StudentScopeProducer studentScopeProducer) {
        this.authenticationContextProvider = authenticationContextProvider;
        this.studentScopeService = studentScopeService;
        this.deploymentService = deploymentService;
        this.studentScopeProducer = studentScopeProducer;
    }

    @Override
    public void validate(SetStudentScopeMessage message) throws RTMValidationException {
        affirmArgument(message.getSourceId() != null, "sourceId is required");
        affirmArgument(message.getDeploymentId() != null, "deploymentId is required");
        affirmArgument(message.getStudentScopeURN() != null, "studentScopeURN is required");
        affirmArgument(message.getData() != null, "data is required");
    }

    @Trace(dispatcher = true, nameTransaction = false, metricName = LEARNER_STUDENT_SCOPE_SET)
    @Override
    public void handle(Session session, SetStudentScopeMessage message) {
        UUID studentId = authenticationContextProvider.get().getAccount().getId();
        UUID studentScopeURN = message.getStudentScopeURN();

        deploymentService.findDeployment(message.getDeploymentId())
                .doOnEach(log.reactiveErrorThrowable("Error while getting the deployment by id"))
                .doOnEach(ReactiveTransaction.linkOnNext())
                .doOnEach(ReactiveTransaction.expireOnComplete())
                .subscriberContext(ReactiveMonitoring.createContext())
                .subscriberContext(ReactiveMdc.with(ReactiveMdc.Property.REQUEST_CONTEXT))
                .flatMap(deployment -> {
                    if (message.getTimeId() != null) {
                        return studentScopeService.setStudentScope(deployment, studentId, studentScopeURN, message.getSourceId(),
                                                                   message.getData(), message.getTimeId());
                    } else {
                        return studentScopeService.setStudentScope(deployment, studentId, studentScopeURN,
                                                            message.getSourceId(), message.getData());
                    }
                })
                .subscribe(studentScopeEntry -> {
                    emitSuccess(session, message, studentScopeEntry);

                    // produces consumable event
                    studentScopeProducer.buildStudentScopeConsumable(studentId,
                                                                     message.getDeploymentId(),
                                                                     studentScopeURN,
                                                                     studentScopeEntry)
                            .produce();

                }, ex -> {
                    emitError(session, message, ex);
                });
    }

    private void emitError(Session session, SetStudentScopeMessage message, Throwable ex) {
        ex = Exceptions.unwrap(ex);

        Throwable finalEx = ex;
        log.jsonDebug("error occurred while getting the deployment by id", new HashMap<String, Object>() {
            {
                put("id", message.getId());
                put("deploymentId", message.getDeploymentId());
                put("coursewareElementId", finalEx.getStackTrace());
            }
        });

        if (ex instanceof DataValidationException) {

            Throwable cause = ex.getCause();

            String errorMessage = ex.getMessage();

            if (cause instanceof InvalidFieldsException) {
                errorMessage = String.format("data has invalid fields: %s", ((InvalidFieldsException) cause).getInvalidFields());
            }

            Responses.errorReactive(session, message.getId(), LEARNER_STUDENT_SCOPE_SET_ERROR, HttpStatus.SC_UNPROCESSABLE_ENTITY,
                    errorMessage);
        } else {

            Responses.errorReactive(session, message.getId(), LEARNER_STUDENT_SCOPE_SET_ERROR, HttpStatus.SC_INTERNAL_SERVER_ERROR,
                    ex.getMessage());
        }
    }

    private void emitSuccess(Session session, SetStudentScopeMessage message, StudentScopeEntry studentScopeEntry) {
        BasicResponseMessage basicResponseMessage = new BasicResponseMessage(LEARNER_STUDENT_SCOPE_SET_OK, message.getId());
        basicResponseMessage.addField("studentScope", studentScopeEntry);
        Responses.writeReactive(session, basicResponseMessage);
    }

}
