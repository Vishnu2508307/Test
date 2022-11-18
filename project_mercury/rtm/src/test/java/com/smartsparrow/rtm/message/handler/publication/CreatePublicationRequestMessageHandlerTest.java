package com.smartsparrow.rtm.message.handler.publication;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.UUID;

import com.smartsparrow.publication.data.PublicationOutputType;
import org.eclipse.jetty.websocket.api.Session;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.smartsparrow.exception.IllegalArgumentFault;
import com.smartsparrow.publication.service.PublicationService;
import com.smartsparrow.rtm.RTMWebSocketTestUtils;
import com.smartsparrow.rtm.lang.WriteResponseException;
import com.smartsparrow.rtm.message.handler.courseware.publication.CreatePublicationRequestMessageHandler;
import com.smartsparrow.rtm.message.recv.courseware.publication.CreatePublicationMessage;

import reactor.core.publisher.Mono;
import reactor.test.publisher.TestPublisher;

public class CreatePublicationRequestMessageHandlerTest {

    @InjectMocks
    private CreatePublicationRequestMessageHandler createPublicationRequestMessageHandler;

    @Mock
    private PublicationService publicationService;

    @Mock
    private CreatePublicationMessage createPublicationMessage;

    private static final UUID publicationID = UUID.randomUUID();
    private static final UUID activityId = UUID.randomUUID();
    private static final UUID accountId = UUID.randomUUID();
    private static final UUID exportId = UUID.randomUUID();

    private Session session;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        session = RTMWebSocketTestUtils.mockSession();
        when(createPublicationMessage.getActivityId()).thenReturn(activityId);
        when(createPublicationMessage.getAccountId()).thenReturn(accountId);
        when(createPublicationMessage.getExportId()).thenReturn(exportId);
        when(createPublicationMessage.getVersion()).thenReturn("default");
        when(createPublicationMessage.getAuthor()).thenReturn("test author");
        when(createPublicationMessage.getPublicationTitle()).thenReturn("test title");
        when(createPublicationMessage.getDescription()).thenReturn("test title description");
        when(createPublicationMessage.getConfig()).thenReturn("");
    }

    @Test
    void handle_success() throws WriteResponseException {
        when(publicationService.createPublication(any(UUID.class),
                                                  any(UUID.class),
                                                  any(UUID.class),
                                                  any(String.class),
                                                  any(String.class),
                                                  any(String.class),
                                                  any(String.class),
                                                  any(String.class),
                                                  any(PublicationOutputType.class))).thenReturn(Mono.just(publicationID));
        createPublicationRequestMessageHandler.handle(session, createPublicationMessage);

        String expectedMessage = "{\"type\":\"publication.create.request.ok\",\"response\":{\"publicationId\":\"" + publicationID + "\"}}";
        verify(session.getRemote(), atLeastOnce()).sendStringByFuture(expectedMessage);

    }

    @Test
    void handle_exception() throws WriteResponseException {
        TestPublisher publication = TestPublisher.create();
        publication.error(new RuntimeException("can't create publication"));
        when(publicationService.createPublication(any(UUID.class),
                                                  any(UUID.class),
                                                  any(UUID.class),
                                                  any(String.class),
                                                  any(String.class),
                                                  any(String.class),
                                                  any(String.class),
                                                  any(String.class),
                                                  any(PublicationOutputType.class))).thenReturn(publication.mono());

        createPublicationRequestMessageHandler.handle(session, createPublicationMessage);
        String expectedMessage = "{\"type\":\"publication.create.request.error\"," +
                "\"code\":422," +
                "\"message\":\"Error creating publication\"}";
        verify(session.getRemote(), atLeastOnce()).sendStringByFuture(expectedMessage);
    }

    @Test
    void validate_noActivityId() {
        when(createPublicationMessage.getActivityId()).thenReturn(null);
        IllegalArgumentFault ex = assertThrows(IllegalArgumentFault.class,
                                               () -> createPublicationRequestMessageHandler.validate(
                                                       createPublicationMessage));
        assertEquals("missing activityId", ex.getMessage());
    }

    @Test
    void validate_noAccountId() {
        when(createPublicationMessage.getAccountId()).thenReturn(null);
        IllegalArgumentFault ex = assertThrows(IllegalArgumentFault.class,
                                               () -> createPublicationRequestMessageHandler.validate(
                                                       createPublicationMessage));
        assertEquals("missing accountId", ex.getMessage());
    }

    @Test
    void validate_noPublicationTitle() {
        when(createPublicationMessage.getPublicationTitle()).thenReturn(null);
        IllegalArgumentFault ex = assertThrows(IllegalArgumentFault.class,
                                               () -> createPublicationRequestMessageHandler.validate(
                                                       createPublicationMessage));
        assertEquals("missing publicationTitle", ex.getMessage());
    }

    @Test
    void validate_noAuthor() {
        when(createPublicationMessage.getAuthor()).thenReturn(null);
        IllegalArgumentFault ex = assertThrows(IllegalArgumentFault.class,
                                               () -> createPublicationRequestMessageHandler.validate(
                                                       createPublicationMessage));
        assertEquals("missing author", ex.getMessage());
    }

    @Test
    void validate_noVersion() {
        when(createPublicationMessage.getVersion()).thenReturn(null);
        IllegalArgumentFault ex = assertThrows(IllegalArgumentFault.class,
                                               () -> createPublicationRequestMessageHandler.validate(
                                                       createPublicationMessage));
        assertEquals("missing version", ex.getMessage());
    }

    @Test
    void validate_noOutputType_noExportId() {
        when(createPublicationMessage.getOutputType()).thenReturn(null);
        when(createPublicationMessage.getExportId()).thenReturn(null);
        IllegalArgumentFault ex = assertThrows(IllegalArgumentFault.class,
                                               () -> createPublicationRequestMessageHandler.validate(
                                                       createPublicationMessage));
        assertEquals("missing exportId", ex.getMessage());
    }

}
