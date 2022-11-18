package com.smartsparrow.rtm.message.handler.competency;


import static com.smartsparrow.util.Warrants.affirmArgument;

import javax.inject.Inject;

import com.smartsparrow.util.log.MercuryLogger;
import com.smartsparrow.util.log.MercuryLoggerFactory;
import org.apache.http.HttpStatus;
import org.eclipse.jetty.websocket.api.Session;

import com.smartsparrow.competency.service.DocumentPermissionService;
import com.smartsparrow.competency.service.DocumentService;
import com.smartsparrow.iam.service.AccountService;
import com.smartsparrow.iam.service.TeamService;
import com.smartsparrow.rtm.lang.RTMValidationException;
import com.smartsparrow.rtm.lang.WriteResponseException;
import com.smartsparrow.rtm.message.MessageHandler;
import com.smartsparrow.rtm.message.handler.cohort.RevokeCohortPermissionMessageHandler;
import com.smartsparrow.rtm.message.recv.competency.RevokeDocumentPermissionMessage;
import com.smartsparrow.rtm.message.send.BasicResponseMessage;
import com.smartsparrow.rtm.util.Responses;

import reactor.core.publisher.Flux;

import java.util.HashMap;

public class RevokeDocumentPermissionMessageHandler implements MessageHandler<RevokeDocumentPermissionMessage> {

    private static final MercuryLogger log = MercuryLoggerFactory.getLogger(RevokeCohortPermissionMessageHandler.class);

    public static final String WORKSPACE_COMPETENCY_PERMISSION_REVOKE = "workspace.competency.permission.revoke";
    private static final String WORKSPACE_COMPETENCY_PERMISSION_REVOKE_ERROR = "workspace.competency.permission.revoke.error";
    private static final String WORKSPACE_COMPETENCY_PERMISSION_REVOKE_OK = "workspace.competency.permission.revoke.ok";

    private final DocumentService documentService;
    private final AccountService accountService;
    private final TeamService teamService;
    private final DocumentPermissionService documentPermissionService;

    @Inject
    public RevokeDocumentPermissionMessageHandler(DocumentService documentService,
                                                  AccountService accountService,
                                                  TeamService teamService,
                                                  DocumentPermissionService documentPermissionService) {
        this.documentService = documentService;
        this.accountService = accountService;
        this.teamService = teamService;
        this.documentPermissionService = documentPermissionService;
    }


    @Override
    public void validate(RevokeDocumentPermissionMessage message) throws RTMValidationException {
        affirmArgument(message.getDocumentId() != null, "documentId is required");
        affirmArgument((message.getAccountIds() == null || message.getAccountIds().isEmpty())
                        != (message.getTeamIds() == null || message.getTeamIds().isEmpty()),
                "either accountIds or teamIds is required");

        if (message.getTeamIds() != null) {
            message.getTeamIds().forEach(teamId -> {
                affirmArgument(teamService.findTeam(teamId).block() != null,
                        String.format("team not found %s", teamId));
            });
        } else {
            message.getAccountIds().forEach(accountId -> {
                affirmArgument(accountService.findById(accountId).blockLast() != null,
                        String.format("account not found %s", accountId));
            });
        }
    }

    @Override
    public void handle(Session session, RevokeDocumentPermissionMessage message) throws WriteResponseException {
        Flux<Void> voidFlux;

        if (message.getTeamIds() != null) {
            voidFlux = documentPermissionService
                    .deleteTeamPermissions(message.getTeamIds(), message.getDocumentId());
        } else {
            voidFlux = documentPermissionService
                    .deleteAccountPermissions(message.getAccountIds(), message.getDocumentId());
        }

        voidFlux
                .subscribe(aVoid -> {
                        },
                        ex -> {
                            log.debug("Unable to revoke document permission", new HashMap<String, Object>(){
                                {
                                    put("documentId", message.getDocumentId());
                                    put("error", ex.getStackTrace());
                                }
                            });
                            Responses.errorReactive(session, message.getId(), WORKSPACE_COMPETENCY_PERMISSION_REVOKE_ERROR,
                                    HttpStatus.SC_UNPROCESSABLE_ENTITY,
                                    "Unable to revoke document permission");
                        }, () -> {
                            BasicResponseMessage responseMessage = new BasicResponseMessage(WORKSPACE_COMPETENCY_PERMISSION_REVOKE_OK,
                                    message.getId());
                            Responses.writeReactive(session, responseMessage);
                        });
    }
}
