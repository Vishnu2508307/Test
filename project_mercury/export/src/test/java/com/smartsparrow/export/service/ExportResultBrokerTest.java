package com.smartsparrow.export.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.UUID;

import org.apache.camel.component.reactive.streams.api.CamelReactiveStreamsService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.smartsparrow.courseware.data.ExportProgress;
import com.smartsparrow.export.data.ExportGateway;
import com.smartsparrow.export.data.ExportStatus;
import com.smartsparrow.export.data.ExportSummary;
import com.smartsparrow.export.data.ExportSummaryNotification;
import com.smartsparrow.export.route.CoursewareExportRoute;
import com.smartsparrow.export.subscription.ExportProducer;


import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.publisher.TestPublisher;

class ExportResultBrokerTest {

    @InjectMocks
    private ExportResultBroker exportResultBroker;

    @Mock
    private ExportTrackService exportTrackService;

    @Mock
    private ExportService exportService;

    @Mock
    private ExportProducer exportProducer;

    @Mock
    private ExportGateway exportGateway;

    @Mock
    private CamelReactiveStreamsService camelReactiveStreamsService;

    private static final UUID exportId = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        when(exportTrackService.isCompleted(exportId)).thenReturn(Mono.just(true));

        when(exportService.findById(exportId)).thenReturn(Mono.just(new ExportSummary()
                .setId(exportId)
                .setStatus(ExportStatus.IN_PROGRESS)));

        when(exportGateway.persist(any(ExportSummary.class))).thenReturn(Flux.just(new Void[]{}));

        when(exportProducer.buildExportConsumable(any(UUID.class), any(ExportProgress.class)))
                .thenReturn(exportProducer);
    }

    @Test
    void broadcast_notComplete() {
        when(exportTrackService.isCompleted(exportId)).thenReturn(Mono.just(false));

        ExportSummary summary = exportResultBroker.broadcast(exportId)
                .block();

        assertNotNull(summary);
        assertNull(summary.getCompletedAt());
        assertEquals(ExportStatus.IN_PROGRESS, summary.getStatus());

        verify(exportService, never()).generateAmbrosia(any(ExportSummary.class));
        verify(exportGateway, never()).persist(any(ExportSummary.class));
    }

    @Test
    void broadcast_completeWithError() {
        ArgumentCaptor<ExportSummary> captor = ArgumentCaptor.forClass(ExportSummary.class);
        TestPublisher<ExportSummary> publisher = TestPublisher.create();
        TestPublisher<String> requestPublisher = TestPublisher.create();
        publisher.error(new RuntimeException("error"));
        when(camelReactiveStreamsService.toStream(
                eq(CoursewareExportRoute.SUBMIT_EXPORT_FAILURE),
                any(ExportSummaryNotification.class),
                eq(String.class))).thenReturn(requestPublisher.error(new RuntimeException("error")));
        when(exportService.generateAmbrosia(any(ExportSummary.class))).thenReturn(publisher.mono());

        final ExportSummary summary = exportResultBroker.broadcast(exportId)
                .block();

        assertNotNull(summary);
        assertNotNull(summary.getCompletedAt());
        assertEquals(ExportStatus.FAILED, summary.getStatus());

        verify(exportService).generateAmbrosia(any(ExportSummary.class));
        verify(exportGateway).persist(captor.capture());

        final ExportSummary persisted = captor.getValue();

        assertNotNull(persisted);
        assertEquals(summary, persisted);
    }

    @Test
    void broadcast_completeSuccess() {

        when(exportService.generateAmbrosia(any(ExportSummary.class)))
                .thenReturn(Mono.just(new ExportSummary()
                        .setId(exportId)
                        .setStatus(ExportStatus.COMPLETED)
                        .setCompletedAt(UUID.randomUUID())));

        final ExportSummary summary = exportResultBroker.broadcast(exportId)
                .block();

        assertNotNull(summary);
        assertNotNull(summary.getCompletedAt());
        assertEquals(ExportStatus.COMPLETED, summary.getStatus());

        verify(exportService).generateAmbrosia(any(ExportSummary.class));
        verify(exportProducer).buildExportConsumable(exportId, ExportProgress.COMPLETE);
        verify(exportGateway, never()).persist(any(ExportSummary.class));
    }

}
