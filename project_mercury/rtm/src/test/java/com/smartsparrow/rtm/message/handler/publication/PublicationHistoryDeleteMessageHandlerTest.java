package com.smartsparrow.rtm.message.handler.publication;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.UUID;

import org.apache.http.HttpStatus;
import org.eclipse.jetty.websocket.api.Session;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.smartsparrow.publication.service.PublicationService;
import com.smartsparrow.rtm.RTMWebSocketTestUtils;
import com.smartsparrow.rtm.lang.RTMValidationException;
import com.smartsparrow.rtm.lang.WriteResponseException;
import com.smartsparrow.rtm.message.handler.courseware.publication.PublicationHistoryDeleteMessageHandler;
import com.smartsparrow.rtm.message.recv.courseware.publication.PublicationHistoryDeleteMessage;

import reactor.test.publisher.TestPublisher;

public class PublicationHistoryDeleteMessageHandlerTest {

    @InjectMocks
    private PublicationHistoryDeleteMessageHandler handler;

    @Mock
    private PublicationService publicationService;

    @Mock
    private PublicationHistoryDeleteMessage message;

    private static final UUID publicationID = UUID.randomUUID();
    private static final UUID activityId = UUID.randomUUID();
    private static final String version = "version";

    private static Session session;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        session = RTMWebSocketTestUtils.mockSession();
        handler = new PublicationHistoryDeleteMessageHandler(publicationService);
    }

    @Test
    void handle_success() throws WriteResponseException {
        when(message.getPublicationId()).thenReturn(publicationID);
        when(message.getActivityId()).thenReturn(activityId);
        when(message.getVersion()).thenReturn(version);
        TestPublisher<Void> deletedActivity = TestPublisher.create();
        deletedActivity.complete();
        when(publicationService.deletePublicationHistory(publicationID, activityId, version)).thenReturn(deletedActivity.flux());
        handler.handle(session, message);
        String expectedMessage = "{\"type\":\"publication.history.delete.ok\",\"response\":{\"publicationId\":\""+publicationID+"\"}}";
        verify(publicationService, atLeastOnce()).deletePublicationHistory(publicationID, activityId, version);
        verify(session.getRemote(), atLeastOnce()).sendStringByFuture(expectedMessage);
    }

    @Test
    void handle_error() throws WriteResponseException {
        TestPublisher<Void> deletedActivity = TestPublisher.create();
        deletedActivity.error(new IllegalAccessException());

        when(publicationService.deletePublicationHistory(publicationID, activityId, version)).thenReturn(deletedActivity.flux());

        handler.handle(session, message);

        String expected = "{" +
                "\"type\":\"publication.history.delete.error\"," +
                "\"code\":422," +
                "\"message\":\"error deleting publications history\"}";

        verify(session.getRemote(), atLeastOnce()).sendStringByFuture(expected);

    }

    @Test
    void validate_publicationIdNotSupplied() throws WriteResponseException {
        when(message.getPublicationId()).thenReturn(null);

        RTMValidationException e = assertThrows(RTMValidationException.class, () -> handler.validate(message));
        assertEquals("missing publicationId", e.getErrorMessage());
        assertEquals("publication.history.delete.error", e.getType());
        assertEquals(HttpStatus.SC_BAD_REQUEST, e.getStatusCode());
    }

    @Test
    void validate_activityIdIdNotSupplied() throws WriteResponseException {
        when(message.getPublicationId()).thenReturn(publicationID);
        when(message.getActivityId()).thenReturn(null);

        RTMValidationException e = assertThrows(RTMValidationException.class, () -> handler.validate(message));
        assertEquals("missing activityId", e.getErrorMessage());
        assertEquals("publication.history.delete.error", e.getType());
        assertEquals(HttpStatus.SC_BAD_REQUEST, e.getStatusCode());
    }

    @Test
    void validate_versionNotSupplied() throws WriteResponseException {
        when(message.getPublicationId()).thenReturn(publicationID);
        when(message.getActivityId()).thenReturn(activityId);
        when(message.getVersion()).thenReturn(null);

        RTMValidationException e = assertThrows(RTMValidationException.class, () -> handler.validate(message));
        assertEquals("missing version", e.getErrorMessage());
        assertEquals("publication.history.delete.error", e.getType());
        assertEquals(HttpStatus.SC_BAD_REQUEST, e.getStatusCode());
    }
}
