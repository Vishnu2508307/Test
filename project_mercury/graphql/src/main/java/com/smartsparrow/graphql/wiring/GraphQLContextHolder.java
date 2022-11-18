package com.smartsparrow.graphql.wiring;

import java.util.Map;

public interface GraphQLContextHolder {

    Map<String, Object> getParameters();
}
