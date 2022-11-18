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
import com.smartsparrow.rtm.message.recv.competency.GrantDocumentPermissionMessage;
import com.smartsparrow.rtm.message.send.BasicResponseMessage;
import com.smartsparrow.rtm.util.Responses;

import reactor.core.publisher.Flux;

import java.util.HashMap;

public class GrantDocumentPermissionMessageHandler implements MessageHandler<GrantDocumentPermissionMessage> {

    private static final MercuryLogger log = MercuryLoggerFactory.getLogger(GrantDocumentPermissionMessageHandler.class);

    public static final String WORKSPACE_COMPETENCY_PERMISSION_GRANT = "workspace.competency.permission.grant";
    private static final String WORKSPACE_COMPETENCY_PERMISSION_GRANT_ERROR = "workspace.competency.permission.grant.error";
    private static final String WORKSPACE_COMPETENCY_PERMISSION_GRANT_OK = "workspace.competency.permission.grant.ok";

    private final DocumentService documentService;
    private final AccountService accountService;
    private final TeamService teamService;
    private final DocumentPermissionService documentPermissionService;

    @Inject
    public GrantDocumentPermissionMessageHandler(DocumentService documentService,
                                                 AccountService accountService,
                                                 TeamService teamService,
                                                 DocumentPermissionService documentPermissionService) {
        this.documentService = documentService;
        this.accountService = accountService;
        this.teamService = teamService;
        this.documentPermissionService = documentPermissionService;
    }


    @Override
    public void validate(GrantDocumentPermissionMessage message) throws RTMValidationException {
        affirmArgument(message.getDocumentId() != null, "documentId is required");
        affirmArgument(message.getPermissionLevel() != null, "permissionLevel is required");
        affirmArgument((message.getAccountIds() == null || message.getAccountIds().isEmpty())
                        != (message.getTeamIds() == null || message.getTeamIds().isEmpty()),
                "either accountIds or teamIds is required");

        //TODO: Uncomment when document summary has been implemented
//        affirmArgument(documentService.findDocument(message.getDocumentId()).block()!=null,
//                String.format("document not found %s",message.getDocumentId()));

        if (message.getAccountIds() != null) {
            message.getAccountIds().forEach(accountId -> {
                affirmArgument(accountService.findById(accountId).blockLast() != null,
                        String.format("account not found %s", accountId));
            });
        } else {
            message.getTeamIds().forEach(teamId -> {
                affirmArgument(teamService.findTeam(teamId).block() != null,
                        String.format("team not found %s", teamId));
            });
        }
    }

    @Override
    public void handle(Session session, GrantDocumentPermissionMessage message) throws WriteResponseException {
        Flux<Void> voidFlux;
        if (message.getAccountIds() != null) {
            voidFlux = documentPermissionService
                    .saveAccountPermissions(message.getAccountIds(),
                            message.getDocumentId(),
                            message.getPermissionLevel());
        } else {
            voidFlux = documentPermissionService
                    .saveTeamPermissions(message.getTeamIds(),
                            message.getDocumentId(),
                            message.getPermissionLevel());
        }

        voidFlux
                .subscribe(aVoid -> {
                }, ex -> {
                    log.debug("Unable to grant document permission", new HashMap<String, Object>(){
                        {
                            put("documentId", message.getDocumentId());
                            put("error", ex.getStackTrace());
                        }
                    });
                    Responses.errorReactive(session, message.getId(), WORKSPACE_COMPETENCY_PERMISSION_GRANT_ERROR,
                            HttpStatus.SC_UNPROCESSABLE_ENTITY, "Unable to grant document permission");
                }, () -> {
                    BasicResponseMessage responseMessage = new BasicResponseMessage(WORKSPACE_COMPETENCY_PERMISSION_GRANT_OK,
                            message.getId())
                            .addField("documentId", message.getDocumentId())
                            .addField("permissionLevel", message.getPermissionLevel());
                    if (message.getAccountIds() != null) {
                        responseMessage
                                .addField("accountIds", message.getAccountIds());
                    } else {
                        responseMessage
                                .addField("teamIds", message.getTeamIds());
                    }
                    Responses.writeReactive(session, responseMessage);
                });
    }

}
