package com.smartsparrow.courseware.data;

import com.google.common.collect.ImmutableList;

import io.leangen.graphql.annotations.GraphQLEnumValue;

public enum CoursewareElementType {
    @GraphQLEnumValue(name = "ACTIVITY") ACTIVITY,
    @GraphQLEnumValue(name = "PATHWAY") PATHWAY,
    @GraphQLEnumValue(name = "INTERACTIVE") INTERACTIVE,
    @GraphQLEnumValue(name = "COMPONENT") COMPONENT,
    @GraphQLEnumValue(name = "SCENARIO") SCENARIO,
    @GraphQLEnumValue(name = "FEEDBACK") FEEDBACK;

    private static ImmutableList<CoursewareElementType> pluginReferences = ImmutableList.of(
            ACTIVITY,
            INTERACTIVE,
            COMPONENT,
            FEEDBACK
    );

    private static ImmutableList<CoursewareElementType> walkables = ImmutableList.of(
            ACTIVITY,
            INTERACTIVE
    );

    private static ImmutableList<CoursewareElementType> haveProgress = ImmutableList.of(
            ACTIVITY,
            INTERACTIVE,
            PATHWAY
    );

    /**
     * Helper method to check if a {@link CoursewareElementType} is for a {@link PluginReference} type
     *
     * @param type the type to check
     * @return <code>true</code> if it is a plugin reference type. <code>false</code> if it is not a plugin reference type
     */
    public static boolean isAPluginReferenceType(CoursewareElementType type) {
        return pluginReferences.contains(type);
    }

    /**
     * Helper method to check if a {@link CoursewareElementType} is a {@link Walkable} type
     *
     * @param type the type to check
     * @return <code>true</code> if it is a walkable. <code>false</code> if it is not a walkable
     */
    public static boolean isAWalkable(CoursewareElementType type) {
        return walkables.contains(type);
    }

    /**
     * Helper method to check if a {@link CoursewareElementType} can have a progress update or not
     *
     * @param type the type to check
     * @return <code>true</code> when the element can have a progress update; <code>false</code> otherwise
     */
    public static boolean canHaveProgress(CoursewareElementType type) {
        return haveProgress.contains(type);
    }
}
