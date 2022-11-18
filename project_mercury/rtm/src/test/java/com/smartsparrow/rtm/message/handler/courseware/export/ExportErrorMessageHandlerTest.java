package com.smartsparrow.rtm.message.handler.courseware.export;

import static com.smartsparrow.courseware.data.CoursewareElementType.INTERACTIVE;
import static com.smartsparrow.rtm.RTMWebSocketTestUtils.mockSession;
import static com.smartsparrow.rtm.message.handler.courseware.export.ExportErrorMessageHandler.AUTHOR_ELEMENT_EXPORT_ERROR_RESULT_ERROR;
import static com.smartsparrow.rtm.message.handler.courseware.export.ExportErrorMessageHandler.AUTHOR_ELEMENT_EXPORT_ERROR_RESULT_OK;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.util.UUID;

import javax.inject.Provider;

import org.eclipse.jetty.websocket.api.Session;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.smartsparrow.courseware.data.CoursewareElementType;
import com.smartsparrow.exception.IllegalArgumentFault;
import com.smartsparrow.export.data.AmbrosiaReducerErrorLog;
import com.smartsparrow.export.data.ExportErrorPayload;
import com.smartsparrow.export.data.ExportResultNotification;
import com.smartsparrow.export.service.ExportService;
import com.smartsparrow.iam.service.AuthenticationContext;
import com.smartsparrow.iam.service.WebSessionToken;
import com.smartsparrow.rtm.MessageHandlerTestUtils;
import com.smartsparrow.rtm.message.recv.courseware.export.ExportGenericMessage;

import reactor.core.publisher.Flux;
import reactor.test.publisher.TestPublisher;

public class ExportErrorMessageHandlerTest {
    private Session session;

    @InjectMocks
    ExportErrorMessageHandler handler;

    @Mock
    private ExportGenericMessage message;

    @Mock
    private ExportService exportService;

    @Mock
    private Provider<AuthenticationContext> authenticationContextProvider;

    @Mock
    private WebSessionToken webSessionToken;

    private static final UUID exportId = UUID.randomUUID();
    private static final CoursewareElementType elementType = INTERACTIVE;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        when(message.getExportId()).thenReturn(exportId);;

        session = mockSession();


        handler = new ExportErrorMessageHandler(
                exportService,
                authenticationContextProvider);
    }

    @Test
    void validate_noElementId() {
        when(message.getExportId()).thenReturn(null);
        IllegalArgumentFault ex = assertThrows(IllegalArgumentFault.class, () -> handler.validate(message));
        assertEquals("missing exportId", ex.getMessage());
    }


    @Test
    void handle() throws IOException {
        ExportErrorPayload exportErrorPayload = new ExportErrorPayload()
                .setExportId(exportId)
                .setNotificationId(UUID.randomUUID())
                .setErrorMessage("This is error message")
                .setCause("This is cause");

        when(exportService.getExportErrors(exportId))
                .thenReturn(Flux.just(exportErrorPayload));
        when(exportService.getAmbrosiaReducerErrors(exportId))
                .thenReturn(Flux.just(new AmbrosiaReducerErrorLog()
                                              .setErrorMessage("This is error")
                                              .setExportId(UUID.randomUUID())
                                              .setErrorMessage("This is error message")));

        handler.handle(session, message);

        MessageHandlerTestUtils.verifySentMessage(session, response -> {
            assertAll(() -> {
                assertEquals(AUTHOR_ELEMENT_EXPORT_ERROR_RESULT_OK, response.getType());
                assertNull(response.getResponse().get("exportList"));
            });
        });
    }

    @Test
    void handle_exception() throws IOException {
        Flux flux = TestPublisher.create().error(new RuntimeException("someException")).flux();
        when(exportService.getExportErrors(exportId))
                .thenReturn(flux);
        when(exportService.getAmbrosiaReducerErrors(any(UUID.class)))
                .thenReturn(flux);

        handler.handle(session, message);

        MessageHandlerTestUtils.verifySentMessage(session, "{\"type\":\"" + AUTHOR_ELEMENT_EXPORT_ERROR_RESULT_ERROR + "\",\"code\":422," +
                "\"message\":\"Unable to list export errors\"}");
    }
}
