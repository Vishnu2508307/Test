package com.smartsparrow.rtm.message.handler.team;

import static com.smartsparrow.rtm.RTMWebSocketTestUtils.mockSession;
import static com.smartsparrow.rtm.message.handler.team.UpdateTeamMessageHandler.IAM_TEAM_UPDATE_ERROR;
import static com.smartsparrow.rtm.message.handler.team.UpdateTeamMessageHandler.IAM_TEAM_UPDATE_OK;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
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
import com.smartsparrow.iam.service.AuthenticationContext;
import com.smartsparrow.iam.service.TeamService;
import com.smartsparrow.rtm.MessageHandlerTestUtils;
import com.smartsparrow.rtm.lang.RTMValidationException;
import com.smartsparrow.rtm.message.recv.team.UpdateTeamMessage;

import reactor.core.publisher.Mono;
import reactor.test.publisher.TestPublisher;

class UpdateTeamMessageHandlerTest {

    @InjectMocks
    private UpdateTeamMessageHandler handler;
    @Mock
    private TeamService teamService;
    @Mock
    private Provider<AuthenticationContext> authenticationContextProvider;
    private Session session;
    @Mock
    private UpdateTeamMessage message;

    private static final UUID teamId = UUID.randomUUID();
    private static final String name = "Team";
    private static final String description = "Description of Team";
    private static final String thumbnail = "Thumbnail for Team";

    @BeforeEach
    void setUp() {
        MockitoAnnotations.initMocks(this);
        session = mockSession();
        when(message.getTeamId()).thenReturn(teamId);
        when(message.getName()).thenReturn(name);
        when(message.getDescription()).thenReturn(description);
        when(message.getThumbnail()).thenReturn(thumbnail);
    }

    @Test
    void validate_noTeamId() {
        when(message.getTeamId()).thenReturn(null);

        RTMValidationException ex = assertThrows(RTMValidationException.class, () -> handler.validate(message));
        assertEquals(IAM_TEAM_UPDATE_ERROR, ex.getType());
        assertEquals(400, ex.getStatusCode());
        assertEquals("missing teamId", ex.getErrorMessage());
    }

    @Test
    void validate_emptyName() {
        when(message.getName()).thenReturn("");

        RTMValidationException ex = assertThrows(RTMValidationException.class, () -> handler.validate(message));
        assertEquals(IAM_TEAM_UPDATE_ERROR, ex.getType());
        assertEquals(400, ex.getStatusCode());
        assertEquals("Team name can not be empty", ex.getErrorMessage());
    }

    @Test
    void validate_noName() throws RTMValidationException {
        when(message.getName()).thenReturn(null);

        handler.validate(message);
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
        when(message.getDescription()).thenReturn(null);
        handler.validate(message);

        when(message.getDescription()).thenReturn("");
        handler.validate(message);
    }

    @Test
    void handle() throws IOException {
        TeamSummary team = new TeamSummary().setId(teamId).setName(name).setDescription(description).setThumbnail(thumbnail);
        when(teamService.updateTeam(teamId, name, description, thumbnail)).thenReturn(Mono.empty());
        when(teamService.findTeam(teamId)).thenReturn(Mono.just(team));

        handler.handle(session, message);

        MessageHandlerTestUtils.verifySentMessage(session, response -> {
            assertAll(() -> {
                assertEquals(IAM_TEAM_UPDATE_OK, response.getType());
                Map responseMap = ((Map) response.getResponse().get("team"));
                assertEquals(teamId.toString(), responseMap.get("id"));
                assertEquals(description, responseMap.get("description"));
                assertEquals(name, responseMap.get("name"));
                assertEquals(thumbnail, responseMap.get("thumbnail"));
            });
        });
    }

    @Test
    void handle_exception() throws IOException {
        TestPublisher<Void> error = TestPublisher.create();
        error.error(new RuntimeException("can't update"));
        when(teamService.updateTeam(teamId, name, description, thumbnail)).thenReturn(error.mono());
        when(teamService.findTeam(teamId)).thenReturn(Mono.just(new TeamSummary()));

        handler.handle(session, message);

        MessageHandlerTestUtils.verifySentMessage(session, "{\"type\":\"" + IAM_TEAM_UPDATE_ERROR + "\",\"code\":422," +
                "\"message\":\"Unable to update team\"}");
    }
}
