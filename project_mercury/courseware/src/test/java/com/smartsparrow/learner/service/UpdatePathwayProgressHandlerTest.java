package com.smartsparrow.learner.service;

import static com.smartsparrow.dataevent.RouteUri.LEARNER_PROGRESS_UPDATE_PATHWAY;
import static com.smartsparrow.learner.service.UpdateCoursewareElementProgressEventDataStub.deploymentId;
import static com.smartsparrow.learner.service.UpdateCoursewareElementProgressEventDataStub.elementId;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.UUID;

import org.apache.camel.Exchange;
import org.apache.camel.component.reactive.streams.api.CamelReactiveStreamsService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.smartsparrow.courseware.data.CoursewareElementType;
import com.smartsparrow.courseware.pathway.LearnerPathway;
import com.smartsparrow.courseware.pathway.PathwayType;
import com.smartsparrow.eval.action.progress.ProgressActionContext;
import com.smartsparrow.eval.action.progress.ProgressionType;
import com.smartsparrow.learner.event.UpdateCoursewareElementProgressEvent;

import reactor.core.publisher.Mono;
import reactor.test.publisher.TestPublisher;

class UpdatePathwayProgressHandlerTest {

    @InjectMocks
    private UpdatePathwayProgressHandler handler;

    @Mock
    private LearnerPathwayService learnerPathwayService;

    @Mock
    private CamelReactiveStreamsService camel;

    private UpdateCoursewareElementProgressEvent event;

    private Exchange exchange;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.initMocks(this);
        final ProgressActionContext progressActionContext = new ProgressActionContext()
                .setProgressionType(ProgressionType.INTERACTIVE_COMPLETE)
                .setElementId(UUID.randomUUID())
                .setElementType(CoursewareElementType.INTERACTIVE);
        event = UpdateCoursewareElementProgressEventDataStub.progressEventCompleted(null, CoursewareElementType.PATHWAY);
        exchange = UpdateCoursewareElementProgressEventDataStub.mockExchangeFrom(event, progressActionContext);

        TestPublisher<Exchange> exchangePublisher = TestPublisher.create();
        when(camel.toStream(anyString(), any(Object.class))).thenReturn(exchangePublisher);
    }

    @Test
    void routeEvent_pathwayNotFound() {

        when(learnerPathwayService.find(elementId, deploymentId)).thenReturn(Mono.empty());

        IllegalStateException e = assertThrows(IllegalStateException.class, () -> handler.routeEvent(exchange));
        assertEquals(String.format("pathway %s does not exist in deployment %s", elementId, deploymentId), e.getMessage());
    }

    @Test
    void routeEvent_success() {
        LearnerPathway learnerPathway = mock(LearnerPathway.class);
        when(learnerPathway.getType()).thenReturn(PathwayType.LINEAR);

        when(learnerPathwayService.find(elementId, deploymentId)).thenReturn(Mono.just(learnerPathway));

        handler.routeEvent(exchange);

//        verify(exchange.getIn()).setHeader(eq("pathwayUri"), eq(LEARNER_PROGRESS_UPDATE_PATHWAY + "/" + learnerPathway.getType()));
        String name = LEARNER_PROGRESS_UPDATE_PATHWAY + "/" + learnerPathway.getType();
        verify(exchange.getIn()).setHeader(eq("pathwayUri"), eq(name));

    }

}
