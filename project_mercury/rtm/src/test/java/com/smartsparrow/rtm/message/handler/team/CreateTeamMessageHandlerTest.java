package com.smartsparrow.rtm.message.handler.team;

import static com.smartsparrow.rtm.RTMWebSocketTestUtils.mockSession;
import static com.smartsparrow.rtm.message.handler.team.CreateTeamMessageHandler.IAM_TEAM_CREATE_ERROR;
import static com.smartsparrow.rtm.message.handler.team.CreateTeamMessageHandler.IAM_TEAM_CREATE_OK;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.util.Map;
import java.util.UUID;

import org.eclipse.jetty.websocket.api.Session;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.google.inject.Provider;
import com.smartsparrow.iam.data.team.TeamSummary;
import com.smartsparrow.iam.service.Account;
import com.smartsparrow.iam.service.AuthenticationContext;
import com.smartsparrow.iam.service.PermissionLevel;
import com.smartsparrow.iam.service.SubscriptionPermissionService;
import com.smartsparrow.iam.service.TeamService;
import com.smartsparrow.rtm.MessageHandlerTestUtils;
import com.smartsparrow.rtm.lang.RTMValidationException;
import com.smartsparrow.rtm.message.recv.team.CreateTeamMessage;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.publisher.TestPublisher;

class CreateTeamMessageHandlerTest {

    @InjectMocks
    private CreateTeamMessageHandler handler;
    @Mock
    private TeamService teamService;
    @Mock
    private Provider<AuthenticationContext> authenticationContextProvider;
    private Session session;
    @Mock
    private CreateTeamMessage message;
    @Mock
    private SubscriptionPermissionService subscriptionPermissionService;

    private static final UUID accountId = UUID.randomUUID();
    private static final UUID subscriptionId = UUID.randomUUID();
    private static final String name = "Team";
    private static final String description = "Description of Team";
    private static final String thumbnail = "Thumbnail for Team";

    @BeforeEach
    void setUp() {
        MockitoAnnotations.initMocks(this);

        Account account = mock(Account.class);
        AuthenticationContext context = mock(AuthenticationContext.class);
        when(account.getId()).thenReturn(accountId);
        when(account.getSubscriptionId()).thenReturn(subscriptionId);
        when(context.getAccount()).thenReturn(account);
        when(authenticationContextProvider.get()).thenReturn(context);

        session = mockSession();
        when(message.getName()).thenReturn(name);
        when(message.getDescription()).thenReturn(description);
        when(message.getThumbnail()).thenReturn(thumbnail);
    }

    @Test
    void validate_noName() {
        when(message.getName()).thenReturn(null);

        RTMValidationException ex = assertThrows(RTMValidationException.class, () -> handler.validate(message));
        assertEquals(IAM_TEAM_CREATE_ERROR, ex.getType());
        assertEquals(400, ex.getStatusCode());
        assertEquals("Team name is required", ex.getErrorMessage());
    }

    @Test
    void validate_EmptyName() {
        when(message.getName()).thenReturn("");

        RTMValidationException ex = assertThrows(RTMValidationException.class, () -> handler.validate(message));
        assertEquals(IAM_TEAM_CREATE_ERROR, ex.getType());
        assertEquals(400, ex.getStatusCode());
        assertEquals("Team name is required", ex.getErrorMessage());
    }

    @Test
    void validate_noDescription() throws RTMValidationException {
        when(message.getDescription()).thenReturn(null);
        handler.validate(message);

        when(message.getDescription()).thenReturn("");
        handler.validate(message);
    }

    @Test
    void validate_noThumbnail() throws RTMValidationException {
        when(message.getThumbnail()).thenReturn(null);
        handler.validate(message);

        when(message.getThumbnail()).thenReturn("");
        handler.validate(message);
    }

    @Test
    void handle_success() throws IOException {
        UUID teamId = UUID.randomUUID();
        TeamSummary team = new TeamSummary().setId(teamId).setName(name).setDescription(description)
                .setThumbnail(thumbnail).setSubscriptionId(subscriptionId);
        when(teamService.createTeam(eq(accountId), eq(name), eq(description), eq(thumbnail), eq(subscriptionId)))
                .thenReturn(Mono.just(team));
        when(subscriptionPermissionService.saveTeamPermission(teamId, subscriptionId, PermissionLevel.REVIEWER))
                .thenReturn(Flux.just(new Void[]{}));

        handler.handle(session, message);

        MessageHandlerTestUtils.verifySentMessage(session, response -> {
            assertAll(() -> {
                assertEquals(IAM_TEAM_CREATE_OK, response.getType());
                Map responseMap = ((Map) response.getResponse().get("team"));
                assertEquals(teamId.toString(), responseMap.get("id"));
                assertEquals(name, responseMap.get("name"));
                assertEquals(description, responseMap.get("description"));
                assertEquals(thumbnail, responseMap.get("thumbnail"));
                assertEquals(subscriptionId.toString(), responseMap.get("subscriptionId"));
            });
        });
    }

    @Test
    void handle_exception() throws IOException {
        TestPublisher<TeamSummary> error = TestPublisher.create();
        error.error(new RuntimeException("can't create"));
        when(teamService.createTeam(eq(accountId), eq(name), eq(description), eq(thumbnail), eq(subscriptionId)))
                .thenReturn(error.mono());

        handler.handle(session, message);

        MessageHandlerTestUtils.verifySentMessage(session, "{\"type\":\"" + IAM_TEAM_CREATE_ERROR + "\",\"code\":422," +
                "\"message\":\"Unable to create team\"}");
    }
}
