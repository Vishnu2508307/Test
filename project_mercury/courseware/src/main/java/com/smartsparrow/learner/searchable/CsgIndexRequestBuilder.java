package com.smartsparrow.learner.searchable;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import javax.ws.rs.HttpMethod;

import com.google.common.collect.ImmutableMap;
import com.smartsparrow.ext_http.service.Request;
import com.smartsparrow.learner.data.LearnerSearchableDocument;

/**
 * Builds an ext_http type Request object that conforms to CSG Index request format.
 */
public class CsgIndexRequestBuilder {

    String uri;
    String piToken;
    String applicationId;
    List<LearnerSearchableDocumentIndexBodyWrapper> fields = new ArrayList<>();
    UUID deploymentId;
    UUID changeId;


    public Request build() {
        return new Request()
                .setUri(uri)
                .setMethod(HttpMethod.POST)  //
                .setJson(true)
                .addField("headers", ImmutableMap.of( //
                    "application-id", applicationId, //
                    "x-authorization", piToken //
                )) //
                .addField("json", true) //
                .addField("body", fields);
    }

    /**
     * Check if any learner searchable documents were added
     */
    public boolean isEmpty() {
        return fields.isEmpty();
    }

    public CsgIndexRequestBuilder sethUri(String uri) {
        this.uri = uri;
        return this;
    }

    public CsgIndexRequestBuilder setPiToken(String piToken) {
        this.piToken = piToken;
        return this;
    }

    public CsgIndexRequestBuilder setApplicationId(String applicationId) {
        this.applicationId = applicationId;
        return this;
    }

    public List<LearnerSearchableDocumentIndexBodyWrapper> getFields() {
        return fields;
    }

    public CsgIndexRequestBuilder addLearnerDocument(LearnerSearchableDocument document) {
        this.fields.add(new LearnerSearchableDocumentIndexBodyWrapper().setFields(document));
        return this;
    }

    public UUID getDeploymentId() {
        return deploymentId;
    }

    public CsgIndexRequestBuilder setDeploymentId(final UUID deploymentId) {
        this.deploymentId = deploymentId;
        return this;
    }

    public UUID getChangeId() {
        return changeId;
    }

    public CsgIndexRequestBuilder setChangeId(final UUID changeId) {
        this.changeId = changeId;
        return this;
    }

    public String getCorrelationId() {
        return deploymentId.toString() + "-" + changeId.toString();
    }
}
