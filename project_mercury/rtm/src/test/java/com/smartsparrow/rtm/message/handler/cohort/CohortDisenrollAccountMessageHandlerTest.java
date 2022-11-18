package com.smartsparrow.rtm.message.handler.cohort;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.mock;
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
import com.smartsparrow.cohort.service.CohortEnrollmentService;
import com.smartsparrow.rtm.RTMWebSocketTestUtils;
import com.smartsparrow.rtm.lang.RTMValidationException;
import com.smartsparrow.rtm.lang.WriteResponseException;
import com.smartsparrow.rtm.message.RTMClientContext;
import com.smartsparrow.rtm.message.recv.cohort.CohortEnrollmentMessage;
import com.smartsparrow.rtm.subscription.cohort.disenrolled.CohortDisEnrolledRTMProducer;

import reactor.core.publisher.Flux;

class CohortDisenrollAccountMessageHandlerTest {

    @Mock
    private CohortEnrollmentService cohortEnrollmentService;
    @Mock
    private Provider<RTMClientContext> rtmClientContextProvider;
    @Mock
    private RTMClientContext rtmClientContext;
    @Mock
    private CohortDisEnrolledRTMProducer cohortDisEnrolledRTMProducer;

    @InjectMocks
    private CohortDisenrollAccountMessageHandler handler;

    private CohortEnrollmentMessage message;
    private static final String messageId = "bingo";
    private static final UUID accountId = UUID.randomUUID();
    private static final UUID cohortId = UUID.randomUUID();
    private static final Session session = RTMWebSocketTestUtils.mockSession();

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        when(rtmClientContextProvider.get()).thenReturn(rtmClientContext);
        message = mock(CohortEnrollmentMessage.class);

        when(message.getId()).thenReturn(messageId);
        when(message.getCohortId()).thenReturn(cohortId);
        when(message.getAccountId()).thenReturn(accountId);
    }

    @Test
    void validate_accountIdNotSupplied() {
        when(message.getAccountId()).thenReturn(null);

        Throwable t = assertThrows(RTMValidationException.class, ()-> handler.validate(message));

        RTMValidationException e = (RTMValidationException) t;

        assertEquals("accountId is required", e.getErrorMessage());
        assertEquals(messageId, e.getReplyTo());
        assertEquals("workspace.cohort.account.disenroll.error", e.getType());
    }

    @Test
    void validate_cohortIdNotSupplied() {
        when(message.getCohortId()).thenReturn(null);

        Throwable t = assertThrows(RTMValidationException.class, ()-> handler.validate(message));

        RTMValidationException e = (RTMValidationException) t;

        assertEquals("cohortId is required", e.getErrorMessage());
        assertEquals(messageId, e.getReplyTo());
        assertEquals("workspace.cohort.account.disenroll.error", e.getType());
    }

    @Test
    void handle_success() throws WriteResponseException {
        when(cohortEnrollmentService.disenrollAccount(accountId, cohortId)).thenReturn(Flux.just(new Void[]{}));
        when(cohortDisEnrolledRTMProducer.buildCohortDisEnrolledRTMConsumable(rtmClientContext, cohortId)).thenReturn(cohortDisEnrolledRTMProducer);

        handler.handle(session, message);

        String expected = "{\"type\":\"workspace.cohort.account.disenroll.ok\",\"replyTo\":\""+messageId+"\"}";
        verify(session.getRemote(), atLeastOnce()).sendStringByFuture(expected);
        verify(cohortDisEnrolledRTMProducer).buildCohortDisEnrolledRTMConsumable(eq(rtmClientContext), eq(cohortId));
        verify(cohortDisEnrolledRTMProducer).produce();
    }

}
