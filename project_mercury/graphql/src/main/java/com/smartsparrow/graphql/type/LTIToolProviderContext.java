package com.smartsparrow.graphql.type;

import io.leangen.graphql.annotations.GraphQLIgnore;

public class LTIToolProviderContext {

    private LTIContext ltiContext;

    public LTIToolProviderContext(final LTIContext ltiContext) {
        this.ltiContext = ltiContext;
    }

    @GraphQLIgnore
    public LTIContext getLtiContext() {
        return ltiContext;
    }
}
