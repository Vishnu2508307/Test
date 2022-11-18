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
import com.smartsparrow.cohort.service.CohortService;
import com.smartsparrow.rtm.RTMWebSocketTestUtils;
import com.smartsparrow.rtm.lang.RTMValidationException;
import com.smartsparrow.rtm.lang.WriteResponseException;
import com.smartsparrow.rtm.message.RTMClientContext;
import com.smartsparrow.rtm.message.recv.cohort.CohortGenericMessage;
import com.smartsparrow.rtm.subscription.cohort.unarchived.CohortUnArchivedRTMProducer;

import reactor.core.publisher.Flux;

class UnarchiveCohortMessageHandlerTest {

    @Mock
    private CohortService cohortService;
    @Mock
    private Provider<RTMClientContext> rtmClientContextProvider;
    @Mock
    private RTMClientContext rtmClientContext;
    @Mock
    private CohortUnArchivedRTMProducer cohortUnArchivedRTMProducer;

    @InjectMocks
    private UnarchiveCohortMessageHandler handler;

    private CohortGenericMessage message;
    private static final String messageId = "Achilles";
    private static final UUID cohortId = UUID.randomUUID();
    private static final Session session = RTMWebSocketTestUtils.mockSession();

    @BeforeEach
    void setUp() {
        MockitoAnnotations.initMocks(this);

        message = mock(CohortGenericMessage.class);

        when(message.getId()).thenReturn(messageId);
        when(message.getCohortId()).thenReturn(cohortId);

        when(cohortService.unarchive(cohortId)).thenReturn(Flux.just(new Void[]{}));

        when(rtmClientContextProvider.get()).thenReturn(rtmClientContext);
    }

    @Test
    void validate_cohortIdNotSupplied() {
        when(message.getCohortId()).thenReturn(null);

        Throwable t = assertThrows(RTMValidationException.class, ()-> handler.validate(message));

        RTMValidationException e = (RTMValidationException) t;

        assertEquals("cohortId is required", e.getErrorMessage());
        assertEquals(messageId, e.getReplyTo());
        assertEquals("workspace.cohort.unarchive.error", e.getType());
    }


    @Test
    void handle() throws WriteResponseException {
        when(cohortUnArchivedRTMProducer.buildCohortUnArchivedRTMConsumable(rtmClientContext, cohortId)).thenReturn(cohortUnArchivedRTMProducer);

        handler.handle(session, message);

        String expected = "{\"type\":\"workspace.cohort.unarchive.ok\",\"replyTo\":\"" + messageId + "\"}";

        verify(session.getRemote(), atLeastOnce()).sendStringByFuture(expected);
        verify(cohortUnArchivedRTMProducer).buildCohortUnArchivedRTMConsumable(eq(rtmClientContext), eq(cohortId));
        verify(cohortUnArchivedRTMProducer).produce();
    }

}
