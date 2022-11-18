package com.smartsparrow.rtm.message.handler.competency;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.UUID;

import org.eclipse.jetty.websocket.api.Session;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.google.common.collect.Lists;
import com.smartsparrow.competency.service.DocumentPermissionService;
import com.smartsparrow.competency.service.DocumentService;
import com.smartsparrow.exception.IllegalArgumentFault;
import com.smartsparrow.iam.service.Account;
import com.smartsparrow.iam.service.AccountService;
import com.smartsparrow.iam.service.TeamService;
import com.smartsparrow.rtm.RTMWebSocketTestUtils;
import com.smartsparrow.rtm.lang.WriteResponseException;
import com.smartsparrow.rtm.message.recv.competency.RevokeDocumentPermissionMessage;

import reactor.core.publisher.Flux;

public class RevokeDocumentPermissionMessageHandlerTest {

    @InjectMocks
    RevokeDocumentPermissionMessageHandler handler;

    @Mock
    DocumentService documentService;

    @Mock
    AccountService accountService;

    @Mock
    TeamService teamService;

    @Mock
    DocumentPermissionService documentPermissionService;

    @Mock
    RevokeDocumentPermissionMessage message;

    @Mock
    RevokeDocumentPermissionMessage teamMessage;


    private Session session;
    private static final String messageId = UUID.randomUUID().toString();
    private static final UUID accountId = UUID.randomUUID();
    private static final UUID teamId = UUID.randomUUID();
    private static final UUID documentId = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        MockitoAnnotations.initMocks(this);

        session = RTMWebSocketTestUtils.mockSession();
        when(message.getDocumentId()).thenReturn(documentId);
        when(message.getAccountIds()).thenReturn(Lists.newArrayList(accountId));
        when(message.getTeamIds()).thenReturn(null);
        when(message.getId()).thenReturn(messageId);

        when(teamMessage.getDocumentId()).thenReturn(documentId);
        when(teamMessage.getTeamIds()).thenReturn(Lists.newArrayList(teamId));
        when(teamMessage.getAccountIds()).thenReturn(null);
        when(teamMessage.getId()).thenReturn(messageId);

        when(accountService.findById(accountId))
                .thenReturn(Flux.just(new Account()
                        .setId(accountId)));
    }


    @Test
    void validate_noDocumentId() {
        when(message.getDocumentId()).thenReturn(null);

        IllegalArgumentFault t = assertThrows(IllegalArgumentFault.class, () -> handler.validate(message));

        assertEquals("documentId is required", t.getMessage());
    }

    @Test
    void validate_noAccountIds() {
        when(message.getAccountIds()).thenReturn(null);

        IllegalArgumentFault t = assertThrows(IllegalArgumentFault.class, () -> handler.validate(message));

        assertEquals("either accountIds or teamIds is required", t.getMessage());
    }

    @Test
    void validate_emptyAccountIds() {
        when(message.getAccountIds()).thenReturn(Lists.newArrayList());

        IllegalArgumentFault t = assertThrows(IllegalArgumentFault.class, () -> handler.validate(message));

        assertEquals("either accountIds or teamIds is required", t.getMessage());
    }

    @Test
    void validate_noTeamIds() {
        when(teamMessage.getTeamIds()).thenReturn(null);

        IllegalArgumentFault t = assertThrows(IllegalArgumentFault.class, () -> handler.validate(teamMessage));

        assertEquals("either accountIds or teamIds is required", t.getMessage());
    }

    @Test
    void validate_emptyTeamIds() {
        when(teamMessage.getTeamIds()).thenReturn(Lists.newArrayList());

        IllegalArgumentFault t = assertThrows(IllegalArgumentFault.class, () -> handler.validate(teamMessage));

        assertEquals("either accountIds or teamIds is required", t.getMessage());
    }

    @Test
    void handle_accountPermissions() throws WriteResponseException {
        when(documentPermissionService
                .deleteAccountPermissions(Lists.newArrayList(accountId), documentId))
                .thenReturn(Flux.just(new Void[]{}));

        handler.handle(session, message);

        String expected = "{" +
                "\"type\":\"workspace.competency.permission.revoke.ok\"," +
                "\"replyTo\":\"" + messageId + "\"" +
                "}";
        verify(session.getRemote(), atLeastOnce()).sendStringByFuture(expected);
    }

    @Test
    void handle_teamPermissions() throws WriteResponseException {
        when(documentPermissionService
                .deleteTeamPermissions(Lists.newArrayList(teamId), documentId))
                .thenReturn(Flux.just(new Void[]{}));

        handler.handle(session, teamMessage);

        String expected = "{" +
                "\"type\":\"workspace.competency.permission.revoke.ok\"," +
                "\"replyTo\":\"" + messageId + "\"" +
                "}";
        verify(session.getRemote(), atLeastOnce()).sendStringByFuture(expected);
    }

}
