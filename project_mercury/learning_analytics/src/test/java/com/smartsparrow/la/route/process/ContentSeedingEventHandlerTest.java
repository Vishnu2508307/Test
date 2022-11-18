package com.smartsparrow.la.route.process;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.UUID;

import org.apache.camel.Exchange;
import org.apache.camel.Message;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.smartsparrow.courseware.data.CoursewareElementType;
import com.smartsparrow.la.event.AutobahnPublishMessage;
import com.smartsparrow.la.event.ContentSeedingMessage;
import com.smartsparrow.la.lang.EventSummaryCreationException;
import com.smartsparrow.la.mapper.pla.data.LearningResource;
import com.smartsparrow.la.service.ActivitySeedingService;
import com.smartsparrow.la.service.PublishToAutobahnService;

public class ContentSeedingEventHandlerTest {

    @InjectMocks
    ContentSeedingEventHandler contentSeedingEventHandler;

    @Mock
    ActivitySeedingService activitySeedingService;
    @Mock
    PublishToAutobahnService publishToAutobahnService;

    Exchange exchange;
    ContentSeedingMessage contentSeedingMessage;
    Message message;
    private static final UUID changeId = UUID.randomUUID();
    private static final UUID cohortId = UUID.randomUUID();
    private static final CoursewareElementType coursewareElementType = CoursewareElementType.ACTIVITY;
    private static final UUID deploymentId = UUID.randomUUID();
    private static final UUID elementId = UUID.randomUUID();
    private static final UUID trackingId = UUID.randomUUID();
    private LearningResource learningResource;
    private AutobahnPublishMessage autobahnPublishMessage;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.initMocks(this);

        learningResource = new LearningResource()
                .setTitle("Some title")
                .setDescription("some desc")
                .setTimeCategorizationCodeLearning("LEARNING")
                .setLearningResourceIdType(CoursewareElementType.ACTIVITY.toString())
                .setLearningResourceId(elementId.toString());

        autobahnPublishMessage = new AutobahnPublishMessage()
                .setMessageTypeCode("LearningResource")
                .setCorrelationId(null)
                .setNamespace("Common")
                .setPayload(learningResource.toString())
                .setCreateType("Create")
                .setStreamType("Activity")
                .setVersion("3.2.1");

        exchange = mock(Exchange.class);
        message = mock(Message.class);
        contentSeedingMessage = new ContentSeedingMessage(cohortId)
                .setElementId(elementId)
                .setChangeId(changeId)
                .setCoursewareElementType(CoursewareElementType.ACTIVITY)
                .setDeploymentId(deploymentId);
        when(exchange.getIn()).thenReturn(message);
        when(exchange.getIn().getBody()).thenReturn(contentSeedingMessage);
        when(exchange.getOut()).thenReturn(message);
    }

    @Test
    void handle_exception() {
        when(activitySeedingService.toLearningResource(elementId)).thenReturn(learningResource);
        when(activitySeedingService.learningResourceToAutobahnPublishMessage(any(LearningResource.class))).thenReturn(autobahnPublishMessage);
        when(publishToAutobahnService.publish(any(AutobahnPublishMessage.class))).thenThrow(new EventSummaryCreationException(autobahnPublishMessage.toString()));
        contentSeedingEventHandler.handle(exchange);

        assertNotNull(exchange);
        assertNotNull(exchange.getOut());
        assertNotNull(exchange.getOut().getBody());
    }

    @Test
    void handle_success() {

        when(activitySeedingService.toLearningResource(elementId)).thenReturn(learningResource);
        when(activitySeedingService.learningResourceToAutobahnPublishMessage(any(LearningResource.class))).thenReturn(autobahnPublishMessage);
        when(publishToAutobahnService.publish(any(AutobahnPublishMessage.class))).thenReturn(trackingId);
        contentSeedingEventHandler.handle(exchange);

        assertEquals(trackingId, autobahnPublishMessage.getTrackingId());
        assertEquals(learningResource.toString(), autobahnPublishMessage.getPayload());
    }

}
