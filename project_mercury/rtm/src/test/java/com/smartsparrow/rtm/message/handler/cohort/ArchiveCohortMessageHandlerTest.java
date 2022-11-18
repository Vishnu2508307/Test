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

import com.datastax.driver.core.utils.UUIDs;
import com.google.inject.Provider;
import com.smartsparrow.cohort.service.CohortService;
import com.smartsparrow.rtm.RTMWebSocketTestUtils;
import com.smartsparrow.rtm.lang.RTMValidationException;
import com.smartsparrow.rtm.lang.WriteResponseException;
import com.smartsparrow.rtm.message.RTMClientContext;
import com.smartsparrow.rtm.message.recv.cohort.CohortGenericMessage;
import com.smartsparrow.rtm.subscription.cohort.archived.CohortArchivedRTMProducer;
import com.smartsparrow.util.DateFormat;

import reactor.core.publisher.Mono;

class ArchiveCohortMessageHandlerTest {

    @Mock
    private CohortService cohortService;
    @Mock
    private Provider<RTMClientContext> rtmClientContextProvider;
    @Mock
    private RTMClientContext rtmClientContext;
    @Mock
    private CohortArchivedRTMProducer cohortArchivedRTMProducer;

    @InjectMocks
    private ArchiveCohortMessageHandler handler;

    private CohortGenericMessage message;
    private static final UUID cohortId = UUID.randomUUID();
    private static final UUID finishedDate = UUIDs.timeBased();
    private static final String messageId = "Indian Ocean";
    private static final Session session = RTMWebSocketTestUtils.mockSession();

    @BeforeEach
    void setUp() {
        MockitoAnnotations.initMocks(this);

        message = mock(CohortGenericMessage.class);

        when(message.getCohortId()).thenReturn(cohortId);
        when(message.getId()).thenReturn(messageId);

        when(cohortService.archive(eq(cohortId))).thenReturn(Mono.just(finishedDate));

        when(rtmClientContextProvider.get()).thenReturn(rtmClientContext);
    }

    @Test
    void validate_cohortIdNotSupplied() {
        when(message.getCohortId()).thenReturn(null);

        Throwable t = assertThrows(RTMValidationException.class, ()-> handler.validate(message));

        RTMValidationException e = (RTMValidationException) t;

        assertEquals("cohortId is required", e.getErrorMessage());
        assertEquals(messageId, e.getReplyTo());
        assertEquals("workspace.cohort.archive.error", e.getType());
    }

    @Test
    void handle() throws WriteResponseException {
        when(cohortArchivedRTMProducer.buildCohortArchivedRTMConsumable(rtmClientContext, cohortId)).thenReturn(cohortArchivedRTMProducer);
        handler.handle(session, message);

        String expected = "{" +
                            "\"type\":\"workspace.cohort.archive.ok\"," +
                            "\"response\":{" +
                                "\"finishedDate\":\""+DateFormat.asRFC1123(finishedDate) +"\"" +
                            "},\"replyTo\":\"" + messageId + "\"}";

        verify(session.getRemote(), atLeastOnce()).sendStringByFuture(expected);
        verify(cohortArchivedRTMProducer).buildCohortArchivedRTMConsumable(eq(rtmClientContext), eq(cohortId));
        verify(cohortArchivedRTMProducer).produce();
    }

}
