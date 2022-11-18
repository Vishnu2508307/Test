package com.smartsparrow.la.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.pearson.autobahn.common.domain.StreamType;
import com.smartsparrow.courseware.payload.ActivityPayload;
import com.smartsparrow.courseware.service.ActivityService;
import com.smartsparrow.la.config.LearningAnalyticsConfig;
import com.smartsparrow.la.event.AutobahnPublishMessage;
import com.smartsparrow.la.mapper.pla.data.LearningResource;

import reactor.core.publisher.Mono;

public class ActivitySeedingServiceTest {

    @InjectMocks
    ActivitySeedingService activitySeedingService;

    @Mock
    ActivityService activityService;

    @Mock
    LearningAnalyticsConfig learningAnalyticsConfig;

    private static final UUID activityId = UUID.randomUUID();
    private static final UUID messageId = UUID.randomUUID();
    ActivityPayload activityPayload;

    LearningResource payload;


    @BeforeEach
    void setUp() {

        MockitoAnnotations.initMocks(this);
        activityPayload = mock(ActivityPayload.class);
        payload = new LearningResource()
                .setTransactionTypeCode("Create")
                .setMessageId(messageId.toString())
                .setLearningResourceId(activityId.toString())
                .setLearningResourceTypeCode("Activity");
        when(activityPayload.getActivityId()).thenReturn(activityId);
        when(activityPayload.getConfig()).thenReturn("{\"title\": \"some title\", \"desc\": \"some desc\"}");
        when(activityService.getActivityPayload(activityId)).thenReturn(Mono.just(activityPayload));
        when(learningAnalyticsConfig.getAutobahnEnvironment()).thenReturn("INT");
        when(learningAnalyticsConfig.getOriginatingSystemCode()).thenReturn("Bronte");
    }

    @Test
    void test_toLearningResource_success() {
        LearningResource result = activitySeedingService.toLearningResource(activityId);
        assertNotNull(result);
        assertNotNull(result.getMessageId());
        assertEquals("Bronte", result.getDeliveryPlatformCode());
        assertNotNull(result.getEnvironmentCode());
        assertEquals("INT", result.getEnvironmentCode());
        assertEquals(activityId.toString(), result.getLearningResourceId());
        assertEquals("some title", result.getTitle());
        assertEquals("some desc", result.getDescription());
    }

    @Test
    void test_toLearningResource_success_without_configs() {
        when(activityPayload.getConfig()).thenReturn("{}");
        LearningResource result = activitySeedingService.toLearningResource(activityId);
        assertNotNull(result);
        assertNotNull(result.getMessageId());
        assertEquals("Bronte", result.getDeliveryPlatformCode());
        assertNotNull(result.getEnvironmentCode());
        assertEquals("INT", result.getEnvironmentCode());
        assertEquals(activityId.toString(), result.getLearningResourceId());
        assertNull(result.getTitle());
        assertNull(result.getDescription());
    }

    @Test
    void test_learningResourceToAutobahnPublishMessage() {
        String expected = "{" +
                "\"messageTypeCode\":\"LearningResource\"," +
                "\"originatingSystemCode\":\"Bronte\"," +
                "\"namespaceCode\":\"Common\"," +
                "\"messageVersion\":\"3.2.0\"," +
                "\"transactionTypeCode\":\"Create\"," +
                "\"messageId\":\"" + messageId + "\"," +
                "\"learningResourceId\":\"" + activityId + "\"," +
                "\"learningResourceTypeCode\":\"Activity\"" +
                "}";
        AutobahnPublishMessage autobahnPublishMessage = activitySeedingService.learningResourceToAutobahnPublishMessage(payload);
        assertNotNull(autobahnPublishMessage);
        assertEquals("Create", autobahnPublishMessage.getCreateType());
        assertEquals("LearningResource", autobahnPublishMessage.getMessageTypeCode());
        assertEquals("Common", autobahnPublishMessage.getNamespace());
        assertEquals(StreamType.EVENT.name(), autobahnPublishMessage.getStreamType());
        assertEquals("3.2.0", autobahnPublishMessage.getVersion());
        assertNotNull(autobahnPublishMessage.getPayload());
        assertEquals(expected, autobahnPublishMessage.getPayload());
    }


}
