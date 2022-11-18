package com.smartsparrow.rtm.message.handler.cohort;

import static com.google.common.base.Preconditions.checkArgument;

import java.util.HashMap;

import javax.inject.Inject;

import com.smartsparrow.rtm.subscription.cohort.revoked.CohortRevokedRTMProducer;
import com.smartsparrow.util.log.MercuryLogger;
import com.smartsparrow.util.log.MercuryLoggerFactory;
import org.apache.camel.component.reactive.streams.api.CamelReactiveStreamsService;
import org.apache.http.HttpStatus;
import org.eclipse.jetty.websocket.api.Session;

import com.google.inject.Provider;
import com.smartsparrow.cohort.service.CohortPermissionService;
import com.smartsparrow.rtm.lang.RTMValidationException;
import com.smartsparrow.rtm.lang.WriteResponseException;
import com.smartsparrow.rtm.message.MessageHandler;
import com.smartsparrow.rtm.message.RTMClientContext;
import com.smartsparrow.rtm.message.recv.cohort.RevokeCohortPermissionMessage;
import com.smartsparrow.rtm.message.send.BasicResponseMessage;
import com.smartsparrow.rtm.util.Responses;

import reactor.core.publisher.Flux;

public class RevokeCohortPermissionMessageHandler implements MessageHandler<RevokeCohortPermissionMessage> {

    private static final MercuryLogger log = MercuryLoggerFactory.getLogger(RevokeCohortPermissionMessageHandler.class);

    public static final String WORKSPACE_COHORT_PERMISSION_REVOKE = "workspace.cohort.permission.revoke";
    private static final String WORKSPACE_COHORT_PERMISSION_REVOKE_ERROR = "workspace.cohort.permission.revoke.error";
    private static final String WORKSPACE_COHORT_PERMISSION_REVOKE_OK = "workspace.cohort.permission.revoke.ok";

    private final CohortPermissionService cohortPermissionService;
    private final Provider<RTMClientContext> rtmClientContextProvider;
    private final CohortRevokedRTMProducer cohortRevokedRTMProducer;

    @Inject
    public RevokeCohortPermissionMessageHandler(CohortPermissionService cohortPermissionService,
                                                Provider<RTMClientContext> rtmClientContextProvider,
                                                CohortRevokedRTMProducer cohortRevokedRTMProducer) {
        this.cohortPermissionService = cohortPermissionService;
        this.rtmClientContextProvider = rtmClientContextProvider;
        this.cohortRevokedRTMProducer = cohortRevokedRTMProducer;
    }

    @Override
    public void validate(RevokeCohortPermissionMessage message) throws RTMValidationException {
        try {
            checkArgument(message.getCohortId() != null, "cohortId is required");
            checkArgument((message.getAccountId() == null) != (message.getTeamId() == null),
                    "either accountId or teamId is required");
        } catch (IllegalArgumentException e) {
            throw new RTMValidationException(e.getMessage(), message.getId(), WORKSPACE_COHORT_PERMISSION_REVOKE_ERROR);
        }
    }

    @Override
    public void handle(Session session, RevokeCohortPermissionMessage message) throws WriteResponseException {
        RTMClientContext rtmClientContext = rtmClientContextProvider.get();

        Flux<Void> publisher;
        if (message.getAccountId() != null) {
            publisher = cohortPermissionService.deleteAccountPermissions(message.getAccountId(), message.getCohortId());
        } else {
            publisher = cohortPermissionService.deleteTeamPermissions(message.getTeamId(), message.getCohortId());
        }

        publisher.subscribe(v -> {
            //on next do nothing
        }, ex -> {
            //on error
            log.jsonDebug("Unable to revoke cohort permission", new HashMap<String, Object>(){
                {
                    put("message", message);
                    put("error", ex.getStackTrace());
                }
            });
            Responses.errorReactive(session, message.getId(), WORKSPACE_COHORT_PERMISSION_REVOKE_ERROR,
                    HttpStatus.SC_UNPROCESSABLE_ENTITY, "Unable to revoke cohort permission");
        }, () -> {
            //on complete
            Responses.writeReactive(session, new BasicResponseMessage(WORKSPACE_COHORT_PERMISSION_REVOKE_OK, message.getId()));

            cohortRevokedRTMProducer.buildCohortRevokedRTMConsumable(rtmClientContext, message.getCohortId())
                    .produce();
        });
    }
}
