package com.smartsparrow.rtm.message.recv;

import java.util.Map;

import com.google.common.base.MoreObjects;
import com.google.common.base.Objects;
import com.smartsparrow.rtm.message.ReceivedMessage;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

@SuppressFBWarnings(value = "UWF_UNWRITTEN_FIELD", justification = "Hydrated via Jackson on message deserialization")
public class GraphQLQueryMessage extends ReceivedMessage {

    private String query;
    private Map<String, Object> parameters;

    public GraphQLQueryMessage() {
    }

    public String getQuery() {
        return query;
    }

    public Map<String, Object> getParameters() {
        return parameters;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        GraphQLQueryMessage that = (GraphQLQueryMessage) o;
        return Objects.equal(query, that.query) && Objects.equal(parameters, that.parameters);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(query, parameters);
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this).add("query", query).add("parameters", parameters).toString();
    }

}
