package com.smartsparrow.rtm.message.handler.team;

import static com.smartsparrow.rtm.RTMWebSocketTestUtils.mockSession;
import static com.smartsparrow.rtm.message.handler.team.DeleteTeamMessageHandler.IAM_TEAM_DELETE_ERROR;
import static com.smartsparrow.rtm.message.handler.team.DeleteTeamMessageHandler.IAM_TEAM_DELETE_OK;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.util.UUID;

import org.eclipse.jetty.websocket.api.Session;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.smartsparrow.exception.IllegalArgumentFault;
import com.smartsparrow.iam.data.team.AccountTeamCollaborator;
import com.smartsparrow.iam.service.SubscriptionPermissionService;
import com.smartsparrow.iam.service.TeamService;
import com.smartsparrow.rtm.MessageHandlerTestUtils;
import com.smartsparrow.rtm.lang.RTMValidationException;
import com.smartsparrow.rtm.message.recv.team.DeleteTeamMessage;

import reactor.core.publisher.Flux;
import reactor.test.publisher.TestPublisher;

class DeleteTeamMessageHandlerTest {

    @InjectMocks
    private DeleteTeamMessageHandler handler;
    @Mock
    private TeamService teamService;
    @Mock
    private SubscriptionPermissionService subscriptionPermissionService;
    private Session session;
    @Mock
    private DeleteTeamMessage message;

    private static final UUID teamId = UUID.randomUUID();
    private static final UUID subscriptionId = UUID.randomUUID();
    private static final UUID accountId = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        MockitoAnnotations.initMocks(this);
        session = mockSession();

        when(message.getTeamId()).thenReturn(teamId);
        when(message.getSubscriptionId()).thenReturn(subscriptionId);
    }

    @Test
    void validate_noTeamId() {
        when(message.getTeamId()).thenReturn(null);
        IllegalArgumentFault ex = assertThrows(IllegalArgumentFault.class, () -> handler.validate(message));
        assertEquals("missing teamId", ex.getMessage());

    }

    @Test
    void validate_noSubscriptionId() {
        when(message.getSubscriptionId()).thenReturn(null);
        IllegalArgumentFault ex = assertThrows(IllegalArgumentFault.class, () -> handler.validate(message));
        assertEquals("missing subscriptionId", ex.getMessage());
    }

    @Test
    void handle() throws IOException {
        AccountTeamCollaborator teamCollaborator = new AccountTeamCollaborator().setTeamId(teamId).setAccountId(accountId);
        when(teamService.findAllCollaboratorsForATeam(teamId)).thenReturn(Flux.just(teamCollaborator));
        when(teamService.deleteTeamAccount(teamId, accountId)).thenReturn(Flux.just(new Void[]{}));
        when(subscriptionPermissionService.findTeamSubscriptions(teamId)).thenReturn(Flux.just(subscriptionId));
        when(subscriptionPermissionService.deleteTeamPermission(teamId, subscriptionId)).thenReturn(Flux.just(new Void[]{}));
        when(teamService.deleteTeamSubscription(teamId, subscriptionId)).thenReturn(Flux.just(new Void[]{}));
        when(teamService.deleteTeam(teamId)).thenReturn(Flux.just(new Void[]{}));

        handler.handle(session, message);

        MessageHandlerTestUtils.verifySentMessage(session, response -> {
            assertAll(() -> {
                assertEquals(IAM_TEAM_DELETE_OK, response.getType());
            });
        });
    }

    @Test
    void handle_exception() throws IOException {
        TestPublisher<Void> error = TestPublisher.create();
        error.error(new RuntimeException("can't delete"));
        AccountTeamCollaborator teamCollaborator = new AccountTeamCollaborator().setTeamId(teamId).setAccountId(accountId);
        when(teamService.findAllCollaboratorsForATeam(teamId)).thenReturn(Flux.just(teamCollaborator));
        when(teamService.deleteTeamAccount(teamId, accountId)).thenReturn(Flux.just(new Void[]{}));
        when(subscriptionPermissionService.findTeamSubscriptions(teamId)).thenReturn(Flux.just(subscriptionId));
        when(subscriptionPermissionService.deleteTeamPermission(teamId, subscriptionId)).thenReturn(Flux.just(new Void[]{}));
        when(teamService.deleteTeamSubscription(teamId, subscriptionId)).thenReturn(Flux.just(new Void[]{}));
        when(teamService.deleteTeam(teamId)).thenReturn(error.flux());

        handler.handle(session, message);

        MessageHandlerTestUtils.verifySentMessage(session, "{\"type\":\"" + IAM_TEAM_DELETE_ERROR + "\",\"code\":422," +
                "\"message\":\"Unable to delete team\"}");
    }
}
