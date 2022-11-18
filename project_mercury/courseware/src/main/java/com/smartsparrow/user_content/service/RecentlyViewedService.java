package com.smartsparrow.user_content.service;

import static com.smartsparrow.util.Warrants.affirmNotNull;

import java.util.UUID;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.newrelic.api.agent.Trace;
import com.smartsparrow.user_content.data.RecentlyViewedGateway;
import com.smartsparrow.user_content.data.RecentlyViewed;
import com.smartsparrow.user_content.data.ResourceType;
import com.smartsparrow.util.UUIDs;
import com.smartsparrow.util.log.MercuryLogger;
import com.smartsparrow.util.log.MercuryLoggerFactory;
import com.smartsparrow.util.monitoring.ReactiveTransaction;

import reactor.core.publisher.Mono;

@Singleton
public class RecentlyViewedService {

    private static final MercuryLogger logger = MercuryLoggerFactory.getLogger(RecentlyViewedService.class);
    private final RecentlyViewedGateway recentlyViewedGateway;
    @Inject
    public RecentlyViewedService(final RecentlyViewedGateway recentlyViewedGateway) {
        this.recentlyViewedGateway = recentlyViewedGateway;
    }

    /**
     * Persist users Recently viewed details
     * @param activityId activityId
     * @param workspaceId workspace id
     * @param accountId account id
     * @param projectId project id
     * @param rootElementId rootElement id
     * @param documentId document id
     * @param resourceType resource type
     * @return void flux
     */
    @Trace(async = true)
    public Mono<Void> create(final UUID activityId,
                                     final UUID workspaceId,
                                     final UUID accountId,
                                     final UUID projectId,
                                     final UUID rootElementId,
                                     final UUID documentId,
                                     final ResourceType resourceType) {

        affirmNotNull(accountId, "accountId is required");
        affirmNotNull(workspaceId, "workspaceId is required");
        affirmNotNull(rootElementId, "rootElementId is required");
        affirmNotNull(resourceType, "resourceType is required");
        affirmNotNull(projectId, "projectId is required");
        if(resourceType.equals(ResourceType.LESSON)) {
            affirmNotNull(activityId, "activityId is required");
        }
        RecentlyViewed recentlyViewed = new RecentlyViewed()
                .setId(UUIDs.timeBased())
                .setActivityId(activityId)
                .setWorkspaceId(workspaceId)
                .setAccountId(accountId)
                .setProjectId(projectId)
                .setRootElementId(rootElementId)
                .setDocumentId(documentId)
                .setResourceType(resourceType);
        return recentlyViewedGateway.persist(recentlyViewed).singleOrEmpty()
                .doOnEach(ReactiveTransaction.linkOnNext());
    }
}
