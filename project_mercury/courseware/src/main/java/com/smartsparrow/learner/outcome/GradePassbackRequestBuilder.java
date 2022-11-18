package com.smartsparrow.learner.outcome;

import static com.smartsparrow.data.Headers.PI_AUTHORIZATION_HEADER;

import com.google.common.collect.ImmutableMap;
import com.smartsparrow.ext_http.service.Request;
import com.smartsparrow.learner.data.GradePassbackAssignment;

import javax.ws.rs.HttpMethod;

/**
 * Request builder for a {@link com.smartsparrow.eval.action.Action.Type#GRADE_PASSBACK} evaluation
 * type.
 */
public class GradePassbackRequestBuilder {

    String uri;
    String piToken;
    GradePassbackAssignment gradePassbackAssignment;

    public Request build() {
        String requestBody = gradePassbackAssignment.toString();

        return new Request()
                .setUri(uri)
                .setMethod(HttpMethod.POST)  //
                .addField("headers", ImmutableMap.of( //
                        PI_AUTHORIZATION_HEADER, piToken //
                )) //
                .addField("body", requestBody);
    }

    public GradePassbackRequestBuilder setUri(String uri) {
        this.uri = uri;
        return this;
    }

    public GradePassbackRequestBuilder setPiToken(String piToken) {
        this.piToken = piToken;
        return this;
    }

    public GradePassbackRequestBuilder setGradePassbackAssignment(GradePassbackAssignment gradePassbackAssignment) {
        this.gradePassbackAssignment = gradePassbackAssignment;
        return this;
    }
}
