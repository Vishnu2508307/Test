package com.smartsparrow.la.service;

import java.util.List;
import java.util.UUID;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.json.JSONObject;
import org.slf4j.Logger;

import com.pearson.autobahn.common.domain.StreamType;
import com.smartsparrow.courseware.payload.ActivityPayload;
import com.smartsparrow.courseware.service.ActivityService;
import com.smartsparrow.la.config.LearningAnalyticsConfig;
import com.smartsparrow.la.event.AutobahnPublishMessage;
import com.smartsparrow.la.mapper.pla.data.LearningResource;
import com.smartsparrow.la.mapper.pla.data.LearningResourceComponent;
import com.smartsparrow.la.util.AnalyticsHelper;
import com.smartsparrow.util.Json;
import com.smartsparrow.util.log.MercuryLoggerFactory;

@Singleton
public class ActivitySeedingService {

    private static final Logger log = MercuryLoggerFactory.getLogger(ActivitySeedingService.class);
    private static final String ACTIVITY_LEARNING_RESOURCE_TYPE_CODE = "Activity";
    private static final String LEARNING_TIME_CATEGORIZATION_CODE = "Learning";

    private static final String CONFIG_TITLE_KEY = "title";
    private static final String CONFIG_DESC_KEY = "desc";
    private static final String TRANSACTION_TYPE_CREATE = "Create";

    private final ActivityService activityService;
    private final LearningAnalyticsConfig learningAnalyticsConfig;

    @Inject
    public ActivitySeedingService(ActivityService activityService, LearningAnalyticsConfig learningAnalyticsConfig) {
        this.activityService = activityService;
        this.learningAnalyticsConfig = learningAnalyticsConfig;
    }

    /**
     * Setup object to publish to PLA
     *
     * @param payload - The json payload, in this case a learning resource object {@link LearningResource}
     * @return AutobahnPublishMessage {@link AutobahnPublishMessage} - Components of a message that can be published to PLA via Autobahn
     */
    public AutobahnPublishMessage learningResourceToAutobahnPublishMessage(LearningResource payload) {
        return new AutobahnPublishMessage()
                .setNamespace(payload.getNamespaceCode())
                .setCreateType(payload.getTransactionTypeCode())
                .setMessageTypeCode(payload.getMessageTypeCode())
                .setStreamType(StreamType.EVENT.name())
                .setVersion(payload.getMessageVersion())
                .setPayload(payload.toString());
    }

    /**
     * Creates a learning resource for a given activity
     *
     * @param activityId - Unique identifier for an activity
     * @return LearningResource {@link LearningResource} learning resource object
     */
    public LearningResource toLearningResource(UUID activityId) {
        log.info("Creating LearningResource for activity with id {}", activityId);
        String messageId = AnalyticsHelper.generateMessageId();
        ActivityPayload activityPayload = activityService.getActivityPayload(activityId).block();
        LearningResource learningResource = new LearningResource()
                .setTransactionTypeCode(TRANSACTION_TYPE_CREATE)
                .setTransactionDt(AnalyticsHelper.generateTransactionDate())
                .setMessageId(messageId)
                .setEnvironmentCode(learningAnalyticsConfig.getAutobahnEnvironment())
                .setLearningResourceId(activityId.toString())
                .setLearningResourceIdType(learningAnalyticsConfig.getOriginatingSystemCode())
                .setLearningResourceTypeCode(ACTIVITY_LEARNING_RESOURCE_TYPE_CODE)
                .setDeliveryPlatformCode(learningAnalyticsConfig.getOriginatingSystemCode())
                .setTimeCategorizationCodeLearning(LEARNING_TIME_CATEGORIZATION_CODE)
                .setLearningResourceComponents(computeLearningResourceComponents());
        if (activityPayload != null && activityPayload.getConfig() != null) {
            String config = activityPayload.getConfig();
            JSONObject parsedConfig = Json.parse(config);
            if (parsedConfig.has(CONFIG_TITLE_KEY)) {
                String title = parsedConfig.getString(CONFIG_TITLE_KEY);
                learningResource.setTitle(title);
            }
            if (parsedConfig.has(CONFIG_DESC_KEY)) {
                String description = parsedConfig.getString(CONFIG_DESC_KEY);
                learningResource.setDescription(description);
            }
        }
        return learningResource;
    }

    // TODO: Will be implemented as part of another ticket
    private List<LearningResourceComponent> computeLearningResourceComponents() {
        return null;
    }
}
