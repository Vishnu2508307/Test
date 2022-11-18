package com.smartsparrow.rtm.message.handler.competency;

import static com.smartsparrow.util.Warrants.affirmArgument;

import javax.inject.Inject;

import com.smartsparrow.util.log.MercuryLogger;
import com.smartsparrow.util.log.MercuryLoggerFactory;
import org.apache.http.HttpStatus;
import org.eclipse.jetty.websocket.api.Session;

import com.smartsparrow.competency.data.AccountDocumentCollaborator;
import com.smartsparrow.competency.data.TeamDocumentCollaborator;
import com.smartsparrow.competency.service.DocumentService;
import com.smartsparrow.iam.collaborator.CollaboratorResult;
import com.smartsparrow.iam.service.CollaboratorService;
import com.smartsparrow.rtm.lang.RTMValidationException;
import com.smartsparrow.rtm.lang.WriteResponseException;
import com.smartsparrow.rtm.message.MessageHandler;
import com.smartsparrow.rtm.message.recv.competency.ListCompetencyDocumentCollaboratorMessage;
import com.smartsparrow.rtm.message.send.BasicResponseMessage;
import com.smartsparrow.rtm.util.Responses;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.HashMap;
import java.util.Objects;

public class ListCompetencyDocumentCollaboratorMessageHandler implements MessageHandler<ListCompetencyDocumentCollaboratorMessage> {

    private static final MercuryLogger log = MercuryLoggerFactory.getLogger(ListCompetencyDocumentCollaboratorMessageHandler.class);

    public static final String WORKSPACE_COMPETENCY_COLLABORATOR_SUMMARY = "workspace.competency.collaborator.summary";
    private static final String WORKSPACE_COMPETENCY_COLLABORATOR_SUMMARY_OK = "workspace.competency.collaborator.summary.ok";
    private static final String WORKSPACE_COMPETENCY_COLLABORATOR_SUMMARY_ERROR = "workspace.competency.collaborator.summary.error";

    private final DocumentService documentService;
    private final CollaboratorService collaboratorService;

    @Inject
    public ListCompetencyDocumentCollaboratorMessageHandler(DocumentService documentService,
                                                            CollaboratorService collaboratorService) {
        this.documentService = documentService;
        this.collaboratorService = collaboratorService;
    }

    @Override
    public void validate(ListCompetencyDocumentCollaboratorMessage message) throws RTMValidationException {
        affirmArgument(message.getDocumentId() != null, "documentId is required");
    }

    @Override
    public void handle(Session session, ListCompetencyDocumentCollaboratorMessage message) throws WriteResponseException {
        Flux<AccountDocumentCollaborator> accounts = documentService.fetchAccountCollaborators(message.getDocumentId());
        Flux<TeamDocumentCollaborator> teams = documentService.fetchTeamCollaborators(message.getDocumentId());

        Mono<CollaboratorResult> collaboratorResult = Objects.nonNull(message.getLimit()) ? collaboratorService
                .getCollaborators(teams, accounts, message.getLimit()) : collaboratorService
                .getCollaborators(teams, accounts);

        collaboratorResult.subscribe(result -> {
            Responses.writeReactive(session, new BasicResponseMessage(WORKSPACE_COMPETENCY_COLLABORATOR_SUMMARY_OK, message.getId())
                    .addField("collaborators", result.getCollaborators())
                    .addField("total", result.getTotal()));
        }, ex -> {
            log.debug("error while listing collaborators for document", new HashMap<String, Object>(){
                {
                    put("documentId", message.getDocumentId());
                    put("error", ex.getStackTrace());
                }
            });
            Responses.errorReactive(session, message.getId(), WORKSPACE_COMPETENCY_COLLABORATOR_SUMMARY_ERROR,
                    HttpStatus.SC_UNPROCESSABLE_ENTITY, "error while listing collaborators");
        });

    }
}
