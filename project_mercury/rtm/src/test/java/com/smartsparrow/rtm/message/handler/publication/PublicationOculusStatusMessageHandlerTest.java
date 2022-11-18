package com.smartsparrow.rtm.message.handler.publication;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.eclipse.jetty.websocket.api.Session;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.smartsparrow.exception.IllegalArgumentFault;
import com.smartsparrow.publication.data.PublicationOculusData;
import com.smartsparrow.publication.service.PublicationService;
import com.smartsparrow.rtm.RTMWebSocketTestUtils;
import com.smartsparrow.rtm.lang.WriteResponseException;
import com.smartsparrow.rtm.message.handler.courseware.publication.PublicationOculusStatusMessageHandler;
import com.smartsparrow.rtm.message.recv.courseware.publication.PublicationOculusStatusMessage;

public class PublicationOculusStatusMessageHandlerTest {

    @InjectMocks
    private PublicationOculusStatusMessageHandler publicationOculusStatusMessageHandler;

    @Mock
    private PublicationService publicationService;

    @Mock
    private PublicationOculusStatusMessage publicationOculusStatusMessage;

    private Session session;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        session = RTMWebSocketTestUtils.mockSession();
        when(publicationOculusStatusMessage.getBookId()).thenReturn("BRNT-D8UZ24XCZIW-REV");
    }

    @Test
    void handle_success() throws WriteResponseException {
        PublicationOculusData publicationOculusData = new PublicationOculusData();
        publicationOculusData.setOculusStatus("PUBLISHED");
        publicationOculusData.setOculusVersion("v3");
        publicationOculusData.setBookId("BRNT-D8UZ24XCZIW-REV");
        when(publicationService.getOculusStatus(any(String.class))).thenReturn(publicationOculusData);
        publicationOculusStatusMessageHandler.handle(session, publicationOculusStatusMessage);

        String expectedMessage = "{\"type\":\"publication.oculus.status.ok\",\"response\":{\"oculusData\":{\"oculusStatus\":\"PUBLISHED\",\"oculusVersion\":\"v3\",\"bookId\":\"BRNT-D8UZ24XCZIW-REV\"}}}";
        verify(session.getRemote(), atLeastOnce()).sendStringByFuture(expectedMessage);

    }

    @Test
    void validate_noBookId() {
        when(publicationOculusStatusMessage.getBookId()).thenReturn(null);
        IllegalArgumentFault ex = assertThrows(IllegalArgumentFault.class,
                                               () -> publicationOculusStatusMessageHandler.validate(
                                                       publicationOculusStatusMessage));
        assertEquals("bookId is required", ex.getMessage());
    }
}
