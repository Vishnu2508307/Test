package com.smartsparrow.sso.service;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/**
 * Lightweight grouping of LTI parameters.
 */
public class LTIMessage {

    public static final String VERSION_ONE = "LTI-1p0";

    private Map<String, String> params = new HashMap<>();

    public LTIMessage() {
    }

    /**
     * Get all the parameters
     *
     * @return all the current parameters
     */
    public Map<String, String> getParams() {
        return params;
    }

    /**
     * Get the entries as a set
     *
     * @return a view of the entries as a Set
     */
    public Set<Map.Entry<String, String>> entrySet() {
        return params.entrySet();
    }

    /**
     * Put a single parameter value
     *
     * @param param the parameter name
     * @param value the parameter value
     * @return this
     */
    public LTIMessage put(final String param, final String value) {
        this.params.put(param, value);
        return this;
    }

    /**
     * Get an LTI parameter from the parameters
     *
     * @param param the parameter to fetch
     * @return the value of the param or null
     */
    public String get(LTIParam param) {
        return params.get(param.getValue());
    }

    /**
     * Put all the parameters contained within the argument
     *
     * @param params the parameter and values to add
     * @return this
     */
    public LTIMessage putAll(final Map<String, String> params) {
        this.params.putAll(params);
        return this;
    }

    /**
     * Directly set the parameters
     *
     * @param params the parameters to set
     * @return this
     */
    public LTIMessage setParams(final Map<String, String> params) {
        this.params = params;
        return this;
    }

    /**
     * Add a custom parameter
     *
     * @param name the name of the custom parameter, will be prefixed with LTIParam.LTI_CUSTOM_PARAM_PREFIX
     * @param value the value to set
     */
    public void addCustomParam(String name, String value) {
        params.put(LTIParam.LTI_CUSTOM_PARAM_PREFIX.getValue() + name, value);
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        LTIMessage that = (LTIMessage) o;
        return Objects.equals(params, that.params);
    }

    @Override
    public int hashCode() {
        return Objects.hash(params);
    }

    @Override
    public String toString() {
        return "LTIMessage{" + "params=" + params + '}';
    }
}
