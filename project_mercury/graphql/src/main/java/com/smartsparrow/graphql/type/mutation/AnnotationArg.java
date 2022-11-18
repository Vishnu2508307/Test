package com.smartsparrow.graphql.type.mutation;

import java.util.Objects;
import java.util.UUID;

import com.smartsparrow.annotation.service.Motivation;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import io.leangen.graphql.annotations.GraphQLInputField;

@SuppressFBWarnings(value = "UWF_UNWRITTEN_FIELD")
public class AnnotationArg {

    private UUID id;
    private Motivation motivation;
    private String body;
    private String target;

    @GraphQLInputField(name = "id", description = "the annotation id, used in update mutations")
    public UUID getId() {
        return id;
    }

    @GraphQLInputField(name = "motivation", description = "the annotation motivation")
    public Motivation getMotivation() {
        return motivation;
    }

    @GraphQLInputField(name = "body", description = "the annotation body")
    public String getBody() {
        return body;
    }

    @GraphQLInputField(name = "target", description = "the annotation target")
    public String getTarget() {
        return target;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        AnnotationArg that = (AnnotationArg) o;
        return motivation == that.motivation && Objects.equals(body, that.body) && Objects.equals(target, that.target);
    }

    @Override
    public int hashCode() {
        return Objects.hash(motivation, body, target);
    }

    @Override
    public String toString() {
        return "AnnotationCreateInput{" + "motivation=" + motivation + ", body='" + body + '\'' + ", target='" + target
                + '\'' + '}';
    }
}
