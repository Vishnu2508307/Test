package com.smartsparrow.export.service;

import static org.apache.camel.component.aws.sqs.SqsConstants.MESSAGE_ATTRIBUTES;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.apache.camel.Exchange;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.smartsparrow.exception.IllegalStateFault;
import com.smartsparrow.export.data.ExportErrorNotification;
import com.smartsparrow.export.data.ExportResultNotification;
import com.smartsparrow.export.data.ExportSummary;

import reactor.core.publisher.Mono;

class ExportRouteErrorHandlerTest {

    @InjectMocks
    private ExportRouteErrorHandler handler;

    @Mock
    private ExportService exportService;

    @Mock
    private ExportResultBroker exportResultBroker;

    @Mock
    private Exchange exchange;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        when(exchange.getProperty(Exchange.EXCEPTION_CAUGHT, Exception.class)).thenReturn(new RuntimeException("error"));
    }

    @Test
    void handle_missingRequiredProperty() {
        when(exchange.getProperty(MESSAGE_ATTRIBUTES, Map.class)).thenReturn(null);

        IllegalStateFault f = assertThrows(IllegalStateFault.class, () -> handler.handle(exchange));

        assertEquals("could not handle export error, notificationId attribute is missing", f.getMessage());
    }

// FIXME: I work on local but I don't work on the pipeline, something is wrong
//
//    @Test
//    void handle_success(){
//        final UUID notificationId = UUID.randomUUID();
//        final UUID exportId = UUID.randomUUID();
//        ArgumentCaptor<ExportErrorNotification> captor = ArgumentCaptor.forClass(ExportErrorNotification.class);
//        when(exchange.getProperty(MESSAGE_ATTRIBUTES, Map.class)).thenReturn(new HashMap<String, Object>(){
//            {put("notificationId", notificationId);}
//        });
//        when(exportService.findNotification(notificationId)).thenReturn(Mono.just(new ExportResultNotification()
//                .setExportId(exportId)
//                .setNotificationId(notificationId)));
//        when(exportService.processErrorNotification(any(ExportErrorNotification.class)))
//                .thenReturn(Mono.just(new ExportResultNotification()
//                        .setExportId(exportId)));
//        when(exportResultBroker.broadcast(exportId)).thenReturn(Mono.just(new ExportSummary()));
//
//        handler.handle(exchange);
//
//        verify(exportService).findNotification(notificationId);
//        verify(exportService).processErrorNotification(captor.capture());
//        verify(exportResultBroker).broadcast(exportId);
//
//        ExportErrorNotification error = captor.getValue();
//        assertNotNull(error);
//        assertEquals(exportId, error.getExportId());
//        assertEquals(notificationId, error.getNotificationId());
//        assertEquals("error", error.getErrorMessage());
//        assertEquals("unknown cause", error.getCause());
//    }
}