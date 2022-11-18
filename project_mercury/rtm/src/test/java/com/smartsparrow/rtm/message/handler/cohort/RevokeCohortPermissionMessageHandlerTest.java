package com.smartsparrow.rtm.message.handler.cohort;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.eq;
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

import com.google.inject.Provider;
import com.smartsparrow.cohort.service.CohortPermissionService;
import com.smartsparrow.rtm.RTMWebSocketTestUtils;
import com.smartsparrow.rtm.lang.RTMValidationException;
import com.smartsparrow.rtm.lang.WriteResponseException;
import com.smartsparrow.rtm.message.RTMClientContext;
import com.smartsparrow.rtm.message.recv.cohort.RevokeCohortPermissionMessage;
import com.smartsparrow.rtm.subscription.cohort.revoked.CohortRevokedRTMProducer;

import reactor.core.publisher.Flux;

class RevokeCohortPermissionMessageHandlerTest {

    @Mock
    private CohortPermissionService cohortPermissionService;
    @Mock
    private Provider<RTMClientContext> rtmClientContextProvider;
    @Mock
    private RTMClientContext rtmClientContext;
    @Mock
    private CohortRevokedRTMProducer cohortRevokedRTMProducer;

    @InjectMocks
    private RevokeCohortPermissionMessageHandler handler;
    @Mock
    private RevokeCohortPermissionMessage accountMessage;
    @Mock
    private RevokeCohortPermissionMessage teamMessage;
    private static final UUID cohortId = UUID.randomUUID();
    private static final UUID accountId = UUID.randomUUID();
    private static final UUID teamId = UUID.randomUUID();
    private static final String messageId = "Manjaro";
    private static final Session session = RTMWebSocketTestUtils.mockSession();

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        when(rtmClientContextProvider.get()).thenReturn(rtmClientContext);

        when(accountMessage.getAccountId()).thenReturn(accountId);
        when(accountMessage.getTeamId()).thenReturn(null);
        when(accountMessage.getCohortId()).thenReturn(cohortId);
        when(accountMessage.getId()).thenReturn(messageId);
        when(teamMessage.getAccountId()).thenReturn(null);
        when(teamMessage.getTeamId()).thenReturn(teamId);
        when(teamMessage.getCohortId()).thenReturn(cohortId);
        when(teamMessage.getId()).thenReturn(messageId);
    }

    @Test
    void validate_cohortIdNotSupplied() {
        when(accountMessage.getCohortId()).thenReturn(null);

        RTMValidationException e = assertThrows(RTMValidationException.class, ()-> handler.validate(accountMessage));

        assertEquals("workspace.cohort.permission.revoke.error", e.getType());
        assertEquals("cohortId is required", e.getErrorMessage());
        assertEquals("Manjaro", e.getReplyTo());
    }

    @Test
    void validate_accountIdNotSupplied() {
        when(accountMessage.getAccountId()).thenReturn(null);

        RTMValidationException e = assertThrows(RTMValidationException.class, ()-> handler.validate(accountMessage));

        assertEquals("workspace.cohort.permission.revoke.error", e.getType());
        assertEquals("either accountId or teamId is required", e.getErrorMessage());
        assertEquals("Manjaro", e.getReplyTo());
    }

    @Test
    void validate_teamIdNotSupplied() {
        when(teamMessage.getTeamId()).thenReturn(null);

        RTMValidationException e = assertThrows(RTMValidationException.class, ()-> handler.validate(teamMessage));

        assertEquals("workspace.cohort.permission.revoke.error", e.getType());
        assertEquals("either accountId or teamId is required", e.getErrorMessage());
        assertEquals("Manjaro", e.getReplyTo());
    }

    @Test
    void handle_account() throws WriteResponseException {
        when(cohortPermissionService.deleteAccountPermissions(accountId, cohortId)).thenReturn(Flux.just(new Void[]{}));
        when(cohortRevokedRTMProducer.buildCohortRevokedRTMConsumable(rtmClientContext, cohortId)).thenReturn(cohortRevokedRTMProducer);

        handler.handle(session, accountMessage);

        String expected = "{\"type\":\"workspace.cohort.permission.revoke.ok\",\"replyTo\":\"Manjaro\"}";

        verify(session.getRemote(), atLeastOnce()).sendStringByFuture(expected);
        verify(cohortRevokedRTMProducer).buildCohortRevokedRTMConsumable(eq(rtmClientContext), eq(cohortId));
        verify(cohortRevokedRTMProducer).produce();
    }

    @Test
    void handle_team() throws WriteResponseException {
        when(cohortPermissionService.deleteTeamPermissions(teamId, cohortId)).thenReturn(Flux.just(new Void[]{}));
        when(cohortRevokedRTMProducer.buildCohortRevokedRTMConsumable(rtmClientContext, cohortId)).thenReturn(cohortRevokedRTMProducer);

        handler.handle(session, teamMessage);

        String expected = "{\"type\":\"workspace.cohort.permission.revoke.ok\",\"replyTo\":\"Manjaro\"}";

        verify(session.getRemote(), atLeastOnce()).sendStringByFuture(expected);
        verify(cohortRevokedRTMProducer).buildCohortRevokedRTMConsumable(eq(rtmClientContext), eq(cohortId));
        verify(cohortRevokedRTMProducer).produce();
    }

}
