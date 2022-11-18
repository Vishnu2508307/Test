package com.smartsparrow.rtm.message.handler.competency;

import static com.smartsparrow.competency.DocumentDataStubs.DOCUMENT_ID;
import static com.smartsparrow.rtm.message.handler.competency.CompetencyDocumentCollaboratorStubs.buildAccountCollaborator;
import static com.smartsparrow.rtm.message.handler.competency.CompetencyDocumentCollaboratorStubs.buildTeamCollaborator;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.eclipse.jetty.websocket.api.Session;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.google.common.collect.Lists;
import com.smartsparrow.competency.data.AccountDocumentCollaborator;
import com.smartsparrow.competency.data.TeamDocumentCollaborator;
import com.smartsparrow.competency.service.DocumentService;
import com.smartsparrow.exception.IllegalArgumentFault;
import com.smartsparrow.iam.collaborator.CollaboratorResult;
import com.smartsparrow.iam.collaborator.Collaborators;
import com.smartsparrow.iam.data.team.TeamSummary;
import com.smartsparrow.iam.payload.AccountCollaboratorPayload;
import com.smartsparrow.iam.payload.AccountPayload;
import com.smartsparrow.iam.payload.TeamCollaboratorPayload;
import com.smartsparrow.iam.service.CollaboratorService;
import com.smartsparrow.iam.service.PermissionLevel;
import com.smartsparrow.rtm.RTMWebSocketTestUtils;
import com.smartsparrow.rtm.lang.WriteResponseException;
import com.smartsparrow.rtm.message.recv.competency.ListCompetencyDocumentCollaboratorMessage;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.publisher.TestPublisher;

class ListCompetencyDocumentCollaboratorMessageHandlerTest {

    @InjectMocks
    private ListCompetencyDocumentCollaboratorMessageHandler handler;

    @Mock
    private DocumentService documentService;

    @Mock
    private CollaboratorService collaboratorService;

    private Session session;

    private ListCompetencyDocumentCollaboratorMessage message;

    private Flux<AccountDocumentCollaborator> accounts;
    private Flux<TeamDocumentCollaborator> teams;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.initMocks(this);

        message = mock(ListCompetencyDocumentCollaboratorMessage.class);
        session = RTMWebSocketTestUtils.mockSession();

        when(message.getLimit()).thenReturn(1);
        when(message.getDocumentId()).thenReturn(DOCUMENT_ID);

        accounts = Flux.just(buildAccountCollaborator());
        teams = Flux.just(buildTeamCollaborator());

        AccountCollaboratorPayload accountCollaboratorPayload = AccountCollaboratorPayload.from(new AccountPayload(), PermissionLevel.CONTRIBUTOR);
        TeamCollaboratorPayload teamCollaboratorPayload = TeamCollaboratorPayload.from(new TeamSummary(), PermissionLevel.CONTRIBUTOR);

        CollaboratorResult collaboratorResult = new CollaboratorResult()
                .setTotal(2L)
                .setCollaborators(new Collaborators()
                .setAccounts(Lists.newArrayList(accountCollaboratorPayload))
                .setTeams(Lists.newArrayList(teamCollaboratorPayload)));

        when(documentService.fetchAccountCollaborators(DOCUMENT_ID)).thenReturn(accounts);
        when(documentService.fetchTeamCollaborators(DOCUMENT_ID)).thenReturn(teams);

        when(collaboratorService.getCollaborators(teams, accounts, message.getLimit()))
                .thenReturn(Mono.just(collaboratorResult));

    }

    @Test
    void validate_nullDocumentId() {

        when(message.getDocumentId()).thenReturn(null);

        IllegalArgumentFault e = assertThrows(IllegalArgumentFault.class, ()-> handler.validate(message));

        assertEquals("documentId is required", e.getMessage());

    }

    @Test
    void handle_error() throws WriteResponseException {
        TestPublisher<CollaboratorResult> publisher = TestPublisher.create();
        publisher.error(new RuntimeException("error"));

        when(collaboratorService.getCollaborators(teams, accounts, message.getLimit()))
                .thenReturn(publisher.mono());

        handler.handle(session, message);

        String expected = "{" +
                            "\"type\":\"workspace.competency.collaborator.summary.error\"," +
                            "\"code\":422," +
                            "\"message\":\"error while listing collaborators\"" +
                          "}";

        verify(session.getRemote()).sendStringByFuture(expected);
    }

    @Test
    void handle() throws WriteResponseException {
        handler.handle(session, message);

        String expected = "{" +
                            "\"type\":\"workspace.competency.collaborator.summary.ok\"," +
                            "\"response\":{" +
                                "\"total\":2," +
                                "\"collaborators\":{" +
                                    "\"teams\":[{" +
                                        "\"permissionLevel\":\"CONTRIBUTOR\"," +
                                        "\"team\":{}" +
                                    "}]," +
                                    "\"accounts\":[{" +
                                        "\"permissionLevel\":\"CONTRIBUTOR\"," +
                                        "\"account\":{}" +
                                    "}]" +
                                "}" +
                            "}}";

        verify(session.getRemote()).sendStringByFuture(expected);

    }

}
