package com.smartsparrow.courseware.service;

import java.util.UUID;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.datastax.driver.core.utils.UUIDs;
import com.smartsparrow.courseware.data.ActivityConfig;
import com.smartsparrow.courseware.data.ActivityGateway;
import com.smartsparrow.courseware.data.ComponentConfig;
import com.smartsparrow.courseware.data.ComponentGateway;
import com.smartsparrow.courseware.data.CoursewareElementType;
import com.smartsparrow.courseware.data.FeedbackConfig;
import com.smartsparrow.courseware.data.FeedbackGateway;
import com.smartsparrow.courseware.data.InteractiveConfig;
import com.smartsparrow.courseware.data.InteractiveGateway;
import com.smartsparrow.courseware.data.PathwayConfig;
import com.smartsparrow.courseware.data.PathwayGateway;
import com.smartsparrow.util.log.MercuryLogger;
import com.smartsparrow.util.log.MercuryLoggerFactory;

import reactor.core.publisher.Mono;

@Singleton
public class CoursewareAssetConfigService {
    private static final MercuryLogger log = MercuryLoggerFactory.getLogger(CoursewareAssetConfigService.class);

    private final ActivityGateway activityGateway;
    private final InteractiveGateway interactiveGateway;
    private final ComponentGateway componentGateway;
    private final FeedbackGateway feedbackGateway;
    private final PathwayGateway pathwayGateway;

    @Inject
    public CoursewareAssetConfigService(final ActivityGateway activityGateway,
                                        final InteractiveGateway interactiveGateway,
                                        final ComponentGateway componentGateway,
                                        final FeedbackGateway feedbackGateway,
                                        final PathwayGateway pathwayGateway) {
        this.activityGateway = activityGateway;
        this.interactiveGateway = interactiveGateway;
        this.componentGateway = componentGateway;
        this.feedbackGateway = feedbackGateway;
        this.pathwayGateway = pathwayGateway;
    }

    /**
     * Update asset urn in the config
     *
     * @param elementId     the element id
     * @param elementType   the element type
     * @param oldAssetUrn   the original asset urn
     * @param newAssetUrn   the new asset urn
     * @return a mono of void
     */
    public Mono<Void> updateAssetUrn(final UUID elementId,
                                     final CoursewareElementType elementType,
                                     final String oldAssetUrn,
                                     final String newAssetUrn){

        UUID changeId = UUIDs.timeBased();

        switch(elementType){
            case ACTIVITY:
                return activityGateway.findLatestConfig(elementId)
                        .map(ActivityConfig::getConfig)
                        .flatMap(config -> {
                            String newConfig = config.replace(oldAssetUrn, newAssetUrn);
                            // only update if config and new config are different
                            if(config.equals(newConfig)){
                                return Mono.just(config);
                            }

                            ActivityConfig replacement = new ActivityConfig()
                                                            .setId(changeId)
                                                            .setActivityId(elementId)
                                                            .setConfig(newConfig);

                            return activityGateway.persist(replacement)
                                    .singleOrEmpty();
                        }).then();
            case INTERACTIVE:
                return interactiveGateway.findLatestConfig(elementId)
                        .map(InteractiveConfig::getConfig)
                        .flatMap(config -> {
                            String newConfig = config.replace(oldAssetUrn, newAssetUrn);
                            // only update if config and new config are different
                            if(config.equals(newConfig)){
                                return Mono.just(config);
                            }

                            InteractiveConfig replacement = new InteractiveConfig()
                                                                .setId(changeId)
                                                                .setInteractiveId(elementId)
                                                                .setConfig(newConfig);

                            return interactiveGateway.persist(replacement);
                        }).then();
            case COMPONENT:
                return componentGateway.findLatestConfig(elementId)
                        .map(ComponentConfig::getConfig)
                        .flatMap(config -> {
                            String newConfig = config.replace(oldAssetUrn, newAssetUrn);
                            // only update if config and new config are different
                            if(config.equals(newConfig)){
                                return Mono.just(config);
                            }

                            ComponentConfig componentConfig = new ComponentConfig()
                                                                    .setId(changeId)
                                                                    .setComponentId(elementId)
                                                                    .setConfig(newConfig);

                            return componentGateway.persist(componentConfig);
                    }).then();
            case FEEDBACK:
                return feedbackGateway.findLatestConfig(elementId)
                        .map(FeedbackConfig::getConfig)
                        .flatMap(config -> {
                            String newConfig = config.replace(oldAssetUrn, newAssetUrn);
                            // only update if config and new config are different
                            if(config.equals(newConfig)){
                                return Mono.just(config);
                            }

                            FeedbackConfig feedbackConfig = new FeedbackConfig()
                                                                .setId(changeId)
                                                                .setFeedbackId(elementId)
                                                                .setConfig(newConfig);

                            return feedbackGateway.persist(feedbackConfig);
                        }).then();
            case PATHWAY:
                return pathwayGateway.findLatestConfig(elementId)
                        .map(PathwayConfig::getConfig)
                        .flatMap(config -> {
                            String newConfig = config.replace(oldAssetUrn, newAssetUrn);
                            // only update if config and new config are different
                            if(config.equals(newConfig)){
                                return Mono.just(config);
                            }

                            PathwayConfig replacement = new PathwayConfig()
                                                            .setId(changeId)
                                                            .setPathwayId(elementId)
                                                            .setConfig(newConfig);

                            return pathwayGateway.persist(replacement)
                                    .singleOrEmpty();
                        }).then();
            default : throw new UnsupportedOperationException("Courseware type can not have config: " + elementType);
        }
    }

}
