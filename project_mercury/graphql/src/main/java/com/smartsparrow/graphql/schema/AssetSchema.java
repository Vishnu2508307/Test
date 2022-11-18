package com.smartsparrow.graphql.schema;

import static com.smartsparrow.util.Warrants.affirmArgument;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.smartsparrow.asset.service.AssetPayload;
import com.smartsparrow.courseware.data.CoursewareElementType;
import com.smartsparrow.graphql.service.GraphQLPageFactory;
import com.smartsparrow.learner.data.LearnerComponent;
import com.smartsparrow.learner.data.LearnerWalkable;
import com.smartsparrow.learner.service.LearnerAssetService;

import io.leangen.graphql.annotations.GraphQLArgument;
import io.leangen.graphql.annotations.GraphQLContext;
import io.leangen.graphql.annotations.GraphQLQuery;
import io.leangen.graphql.execution.relay.Page;
import reactor.core.publisher.Mono;

@Singleton
public class AssetSchema {

    private final LearnerAssetService learnerAssetService;

    @Inject
    public AssetSchema(LearnerAssetService learnerAssetService) {
        this.learnerAssetService = learnerAssetService;
    }

    /**
     * Fetch assets for an walkable that has been published
     *
     * @param learnerWalkable - The LearnerActivity/LearnerInteractive context from which this query is accessible
     * @param before          fetching only nodes before this node (exclusive)
     * @param last            fetching only the last certain number of nodes
     * @return Page of AssetPayload{@link Page<AssetPayload>}
     */
    @GraphQLQuery(name = "assets", description = "fetch all assets for walkables")
    public CompletableFuture<Page<AssetPayload>> getAssetsForWalkable(@GraphQLContext LearnerWalkable learnerWalkable,
                                                                      @GraphQLArgument(name = "before",
                                                           description = "fetching only nodes before this node (exclusive)") String before,
                                                                      @GraphQLArgument(name = "last",
                                                           description = "fetching only the last certain number of nodes") Integer last) {

        affirmArgument(learnerWalkable != null,
                "learnerWalkable context is required");

        affirmArgument(learnerWalkable.getChangeId() != null,
                "changeId is required");

        affirmArgument(CoursewareElementType.isAWalkable(learnerWalkable.getElementType()),
                "walkableType is required");

        affirmArgument(learnerWalkable.getId() != null,
                String.format("%s is required", learnerWalkable.getElementType().name()));

        return getAssetPayloadPage(before,
                last,
                learnerWalkable.getChangeId(),
                learnerWalkable.getId()).toFuture();
    }

    /**
     * Fetch assets for a component that has been published
     *
     * @param learnerComponent - The LearnerComponent context from which this query is accessible
     * @param before           fetching only nodes before this node (exclusive)
     * @param last             fetching only the last certain number of nodes
     * @return Page of AssetPayload{@link Page<AssetPayload>}
     */
    @GraphQLQuery(name = "assets", description = "fetch all assets for component")
    public CompletableFuture<Page<AssetPayload>> getAssetsForComponent(@GraphQLContext LearnerComponent learnerComponent,
                                                                       @GraphQLArgument(name = "before",
                                                            description = "fetching only nodes before this node (exclusive)") String before,
                                                                       @GraphQLArgument(name = "last",
                                                            description = "fetching only the last certain number of nodes") Integer last) {

        affirmArgument(learnerComponent != null,
                "learnerComponent context is required");

        affirmArgument(learnerComponent.getChangeId() != null,
                "changeId is required");

        affirmArgument(learnerComponent.getId() != null,
                "componentId is required");

        return getAssetPayloadPage(before,
                last,
                learnerComponent.getChangeId(),
                learnerComponent.getId()).toFuture();
    }

    /**
     * Get list of AssetPayload for an element id and a change id
     *
     * @param before    fetching only nodes before this node (exclusive)
     * @param last      fetching only the last certain number of nodes
     * @param changeId  change id to find the assets for
     * @param elementId element id to find the assets for
     * @return Page of AssetPayload{@link Page<AssetPayload>}
     */
    private Mono<Page<AssetPayload>> getAssetPayloadPage(String before, Integer last, UUID changeId, UUID elementId) {
        Mono<List<AssetPayload>> assetPayloads = learnerAssetService.fetchAssetsForElementAndChangeId(elementId, changeId)
                .collectList();

        return GraphQLPageFactory.createPage(assetPayloads, before, last);
    }
}
