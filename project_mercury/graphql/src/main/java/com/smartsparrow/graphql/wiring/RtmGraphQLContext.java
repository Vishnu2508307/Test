package com.smartsparrow.graphql.wiring;

import java.util.HashMap;
import java.util.Map;

public class RtmGraphQLContext implements GraphQLContextHolder {

    private final Map<String, Object> parameters = new HashMap<>();

    @Override
    public Map<String, Object> getParameters() {
        return parameters;
    }
}
