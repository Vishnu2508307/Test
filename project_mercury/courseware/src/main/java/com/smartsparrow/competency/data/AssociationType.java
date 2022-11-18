package com.smartsparrow.competency.data;

import com.fasterxml.jackson.annotation.JsonValue;

import io.leangen.graphql.annotations.GraphQLEnumValue;

public enum AssociationType {

    @GraphQLEnumValue(name = "isChildOf") IS_CHILD_OF("isChildOf"),
    @GraphQLEnumValue(name = "isPeerOf") IS_PEER_OF("isPeerOf"),
    @GraphQLEnumValue(name = "isPartOf") IS_PART_OF("isPartOf"),
    @GraphQLEnumValue(name = "exactMatchOf") EXACT_MATCH_OF("exactMatchOf"),
    @GraphQLEnumValue(name = "precedes") PRECEDES("precedes"),
    @GraphQLEnumValue(name = "isRelatedTo") IS_RELATED_TO("isRelatedTo"),
    @GraphQLEnumValue(name = "replacedBy") REPLACED_BY("replacedBy"),
    @GraphQLEnumValue(name = "exemplar") EXEMPLAR("exemplar"),
    @GraphQLEnumValue(name = "hasSkillLevel") HAS_SKILL_LEVEL("hasSkillLevel"),
    @GraphQLEnumValue(name = "prerequisiteOf") PREREQUISITE_OF("prerequisiteOf");

    private String label;

    AssociationType(String label) {
        this.label = label;
    }

    @JsonValue
    public String getLabel() {
        return label;
    }
}
