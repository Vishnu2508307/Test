package com.smartsparrow.rtm.message.handler.cohort;

import javax.inject.Inject;

import org.apache.http.HttpStatus;
import org.eclipse.jetty.websocket.api.Session;

import com.smartsparrow.cohort.payload.CohortPayload;
import com.smartsparrow.cohort.service.CohortService;
import com.smartsparrow.rtm.lang.RTMValidationException;
import com.smartsparrow.rtm.lang.WriteResponseException;
import com.smartsparrow.rtm.message.MessageHandler;
import com.smartsparrow.rtm.message.recv.cohort.CohortGenericMessage;
import com.smartsparrow.rtm.message.send.BasicResponseMessage;
import com.smartsparrow.rtm.message.send.ErrorMessage;
import com.smartsparrow.rtm.util.Responses;

public class GetCohortMessageHandler implements MessageHandler<CohortGenericMessage> {

    public static final String WORKSPACE_COHORT_GET = "workspace.cohort.get";
    public static final String WORKSPACE_COHORT_GET_OK = "workspace.cohort.get.ok";
    public static final String WORKSPACE_COHORT_GET_ERROR = "workspace.cohort.get.error";

    private final CohortService cohortService;

    @Inject
    public GetCohortMessageHandler(CohortService cohortService) {
        this.cohortService = cohortService;
    }

    @Override
    public void validate(CohortGenericMessage message) throws RTMValidationException {
        if (message.getCohortId() == null) {
            throw new RTMValidationException("cohortId is required", message.getId(), WORKSPACE_COHORT_GET_ERROR);
        }
    }

    @Override
    public void handle(Session session, CohortGenericMessage message) throws WriteResponseException {
        CohortPayload payload = cohortService.getCohortPayload(message.getCohortId()).block();

        if (payload == null) {
            emitError(session, message.getId(), String.format("Cohort with id '%s' not found", message.getCohortId()));
        } else {
            emitSuccess(session, message.getId(), payload);
        }
    }

    private void emitSuccess(Session session, String clientId, CohortPayload payload) throws WriteResponseException {
        BasicResponseMessage response = new BasicResponseMessage(WORKSPACE_COHORT_GET_OK, clientId);
        response.addField("cohort", payload);
        Responses.write(session, response);
    }

    private void emitError(Session session, String clientId, String message) throws WriteResponseException {
        ErrorMessage error = new ErrorMessage(WORKSPACE_COHORT_GET_ERROR);
        error.setReplyTo(clientId);
        error.setCode(HttpStatus.SC_NOT_FOUND);
        error.setMessage(message);
        Responses.write(session, error);
    }
}
