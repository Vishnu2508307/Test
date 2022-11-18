package com.smartsparrow.learner.searchable;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import javax.ws.rs.HttpMethod;

import com.google.common.collect.ImmutableMap;
import com.smartsparrow.ext_http.service.Request;
import com.smartsparrow.learner.data.LearnerSearchableDocumentIdentity;

/**
 * Builds an ext_http type Request object that conforms to CSG Index request format.
 */
public class CsgDeleteRequestBuilder {

    String uri;
    String piToken;
    String applicationId;
    List<String> body = new ArrayList<>();

    public Request build() {
        return new Request()
                .setUri(uri)
                .setMethod(HttpMethod.DELETE)  //
                .setJson(true)
                .addField("headers", ImmutableMap.of( //
                    "application-id", applicationId, //
                    "x-authorization", piToken //
                )) //
                .addField("json", true) //
                .addField("body", body);
    }

    public CsgDeleteRequestBuilder sethUri(String uri) {
        this.uri = uri;
        return this;
    }

    public CsgDeleteRequestBuilder setPiToken(String piToken) {
        this.piToken = piToken;
        return this;
    }

    public CsgDeleteRequestBuilder setApplicationId(String applicationId) {
        this.applicationId = applicationId;
        return this;
    }

    public CsgDeleteRequestBuilder addLearnerDocuments(Set<LearnerSearchableDocumentIdentity> document) {
        document.forEach(d -> body.add(d.getId()));
        return this;
    }

}
