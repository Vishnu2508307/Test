package com.smartsparrow.user_content.service;

import static com.smartsparrow.util.Warrants.affirmNotNull;

import java.util.UUID;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.newrelic.api.agent.Trace;
import com.smartsparrow.user_content.data.ResourceType;
import com.smartsparrow.user_content.data.SharedResource;
import com.smartsparrow.user_content.data.SharedResourceGateway;
import com.smartsparrow.util.UUIDs;
import com.smartsparrow.util.log.MercuryLogger;
import com.smartsparrow.util.log.MercuryLoggerFactory;
import com.smartsparrow.util.monitoring.ReactiveTransaction;

import reactor.core.publisher.Mono;

@Singleton
public class SharedResourceService {

    private static final MercuryLogger logger = MercuryLoggerFactory.getLogger(SharedResourceService.class);
    private final SharedResourceGateway sharedResourceGateway;
    @Inject
    public SharedResourceService(final SharedResourceGateway sharedResourceGateway) {
        this.sharedResourceGateway = sharedResourceGateway;
    }

    /**
     * Persist users shared resource details
     * @param resourceId activityId
     * @param sharedAccountId workspace id
     * @param accountId account id
     * @param resourceType resource type
     * @return void flux
     */
    @Trace(async = true)
    public Mono<Void> create(final UUID resourceId,
                             final UUID sharedAccountId,
                             final UUID accountId,
                             final ResourceType resourceType) {

        affirmNotNull(accountId, "accountId is required");
        affirmNotNull(sharedAccountId, "sharedAccountId is required");
        affirmNotNull(resourceType, "resourceType is required");
        affirmNotNull(resourceId, "resourceId is required");
        SharedResource favorite = new SharedResource()
                .setId(UUIDs.timeBased())
                .setSharedAccountId(sharedAccountId)
                .setAccountId(accountId)
                .setSharedAt(UUIDs.timeBased())
                .setResourceId(resourceId)
                .setResourceType(resourceType);
        return sharedResourceGateway.persist(favorite).singleOrEmpty()
                .doOnEach(ReactiveTransaction.linkOnNext());
    }
}
