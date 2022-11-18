package com.smartsparrow.learner.data;

import com.smartsparrow.courseware.data.Walkable;

import io.leangen.graphql.annotations.types.GraphQLInterface;

@GraphQLInterface(name = "LearnerWalkable", implementationAutoDiscovery = true)
public interface LearnerWalkable extends Walkable, LearnerElement {

}
