package com.smartsparrow.rtm.message.handler.cohort;

import java.util.List;

import javax.inject.Inject;

import org.eclipse.jetty.websocket.api.Session;

import com.google.inject.Provider;
import com.smartsparrow.cohort.payload.CohortSummaryPayload;
import com.smartsparrow.cohort.service.CohortService;
import com.smartsparrow.iam.service.Account;
import com.smartsparrow.iam.service.AuthenticationContext;
import com.smartsparrow.rtm.lang.WriteResponseException;
import com.smartsparrow.rtm.message.MessageHandler;
import com.smartsparrow.rtm.message.recv.cohort.CohortListMessage;
import com.smartsparrow.rtm.message.send.BasicResponseMessage;
import com.smartsparrow.rtm.util.Responses;

public class CohortListMessageHandler implements MessageHandler<CohortListMessage> {

    public static final String WORKSPACE_COHORT_LIST = "workspace.cohort.list";
    private static final String WORKSPACE_COHORT_LIST_OK = "workspace.cohort.list.ok";

    private final CohortService cohortService;
    private final Provider<AuthenticationContext> authenticationContextProvider;

    @Inject
    public CohortListMessageHandler(CohortService cohortService,
            Provider<AuthenticationContext> authenticationContextProvider) {
        this.cohortService = cohortService;
        this.authenticationContextProvider = authenticationContextProvider;
    }

    @Override
    public void handle(Session session, CohortListMessage message) throws WriteResponseException {
        Account account = authenticationContextProvider.get().getAccount();

        List<CohortSummaryPayload> all = cohortService.fetchCohorts(account.getId())
                .flatMap(cohortService::getCohortSummaryPayload)
                // optionally filter to a specific workspace id
                // FIXME: don't overselect and implement properly, see PLT-5433
                // FIXME: push this logic deeper into a service.
                .filter(payload -> message.getWorkspaceId() == null ||  // no filter specified, allow through
                        message.getWorkspaceId().equals(payload.getWorkspaceId()))
                .collectList()
                .block();

        BasicResponseMessage response = new BasicResponseMessage(WORKSPACE_COHORT_LIST_OK, message.getId())
                .addField("cohorts", all);

        Responses.write(session, response);
    }
}
