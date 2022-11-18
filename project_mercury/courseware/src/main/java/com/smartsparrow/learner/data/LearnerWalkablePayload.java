package com.smartsparrow.learner.data;

import java.util.UUID;

import javax.annotation.Nullable;

import io.leangen.graphql.annotations.types.GraphQLInterface;

@GraphQLInterface(name = "LearnerWalkablePayload", implementationAutoDiscovery = true)
public interface LearnerWalkablePayload extends LearnerWalkable {

    /**
     * @return the learner element theme or null when the element has no config
     */
    @Nullable
    String getTheme();

    /**
     * @return the learner element creatorId or null when the element has no config
     */
    @Nullable
    UUID getCreatorId();
}
