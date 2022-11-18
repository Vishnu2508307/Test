package com.smartsparrow.annotation.service;

import java.io.IOException;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.smartsparrow.exception.IllegalStateFault;
import com.smartsparrow.util.DateFormat;

import io.leangen.graphql.annotations.GraphQLIgnore;

/**
 *
 * Provide end-user annotations, to offer functionality such as bookmarking, commenting, etc.
 *
 * Loosely based on (drawing main concepts from; their data model is shit)
 * 1. https://www.w3.org/blog/news/archives/6156
 * 2. https://www.w3.org/TR/2017/REC-annotation-model-20170223/
 *
 * Notes:
 *  - body and target are persisted as JSON text for the current version. They are complex objects which we can model
 *    more properly in the future when the use-cases are more well defined.
 *
 */
public interface Annotation {

    /**
     * The ID field of the annotation
     * @return the annotation id
     */
    UUID getId();

    /**
     * The (internal) version of the annotation, used to track changes over time.
     * @return the version id of the annotation
     */
    @GraphQLIgnore
    UUID getVersion();

    /**
     * The motivation field of the annotation
     * @return the motivation of the annotation
     */
    Motivation getMotivation();

    /**
     * The creator of the annotation
     * @return the account id of the creator of the annotation
     */
    @GraphQLIgnore
    UUID getCreatorAccountId();

    /**
     * The annotation body field as a JsonNode
     * @return the annotation body as a JsonNode
     */
    @GraphQLIgnore
    JsonNode getBodyJson();

    /**
     * The annotation body field
     * @return the annotation body field as supplied
     */
    @JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "body")
    default String getBody() {
        final JsonNode bodyJson = getBodyJson();
        if (bodyJson == null) {
            return null;
        }
        ObjectMapper mapper = new ObjectMapper();
        try {
            return mapper.writeValueAsString(bodyJson);
        } catch (IOException e) {
            throw new IllegalStateFault("error processing json body");
        }
    }

    /**
     * The annotation target field as a JSONObject
     * @return the annotation target as a JSONObject
     */
    @GraphQLIgnore
    JsonNode getTargetJson();

    /**
     * The annotation target field
     * @return the annotation target field as supplied
     */
    @JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "target")
    default String getTarget() {
        final JsonNode targetJson = getTargetJson();
        if (targetJson == null) {
            return null;
        }
        ObjectMapper mapper = new ObjectMapper();
        try {
            return mapper.writeValueAsString(targetJson);
        } catch (IOException e) {
            throw new IllegalStateFault("error processing json body");
        }
    }

    /**
     * The type field as specified in the W3C spec. Always "Annotation" at this time.
     *
     * @return the type of the annotation
     */
    @JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "type")
    default Type getType() {
        return Type.Annotation;
    }

    /**
     * An RFC-1123 date based on the id of the annotation of when it was created
     *
     * @return the date based on the creation of the annotation
     */
    @JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "created")
    default String getCreated() {
        return DateFormat.asRFC1123(getId());
    }

    /**
     * An RFC-1123 date based on the last modification or creation.
     *
     * @return the date based on the last modification of the annotation
     */
    @JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "modified")
    default String getModified() {
        return DateFormat.asRFC1123(getVersion());
    }

}
