package com.smartsparrow.export.service;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.smartsparrow.export.data.ExportErrorNotification;
import com.smartsparrow.export.data.ExportResultNotification;
import com.smartsparrow.export.data.ExportSummary;

import reactor.core.publisher.Mono;

class ExportErrorQueueHandlerTest {

    @InjectMocks
    private ExportErrorQueueHandler handler;

    @Mock
    private ExportService exportService;

    @Mock
    private ExportResultBroker exportResultBroker;

    @Mock
    private ExportErrorNotification errorNotification;

    private static final UUID exportId = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        when(errorNotification.getExportId()).thenReturn(exportId);

    }

    @Test
    void handle() {
        when(exportService.processErrorNotification(errorNotification))
                .thenReturn(Mono.just(new ExportResultNotification()
                        .setExportId(exportId)));
        when(exportResultBroker.broadcast(exportId)).thenReturn(Mono.just(new ExportSummary()));

        handler.handle(errorNotification);

        verify(exportService).processErrorNotification(errorNotification);
        verify(exportResultBroker).broadcast(exportId);
    }

}