package com.smartsparrow.graphql.type;

import java.util.Map;

public class LTISignedLaunch {
    private String url;
    private String method;
    private Map<String, String> formParameters;

    public LTISignedLaunch() {
    }

    public String getUrl() {
        return url;
    }

    public LTISignedLaunch setUrl(final String url) {
        this.url = url;
        return this;
    }

    public String getMethod() {
        return method;
    }

    public LTISignedLaunch setMethod(final String method) {
        this.method = method;
        return this;
    }

    public Map<String, String> getFormParameters() {
        return formParameters;
    }

    public LTISignedLaunch setFormParameters(final Map<String, String> formParameters) {
        this.formParameters = formParameters;
        return this;
    }
}
