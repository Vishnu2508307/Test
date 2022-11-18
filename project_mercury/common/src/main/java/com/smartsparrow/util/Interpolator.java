package com.smartsparrow.util;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import org.apache.commons.lang.text.StrSubstitutor;

/**
 * Allows to replace a string template with variables that have been added to the context.
 * Replaces ${variable_name} with the value found in the context. See {@link StrSubstitutor}
 */
public class Interpolator {

    private Map<String, String> context;

    public Interpolator() {
        context = new HashMap<>();
    }

    public Map<String, String> getContext() {
        return context;
    }

    public Interpolator setContext(Map<String, String> context) {
        this.context = context;
        return this;
    }

    public Interpolator addVariable(final String name, final String value) {
        this.context.put(name, value);
        return this;
    }

    /**
     * Replaces any interpolated variable in the template string from values found in the interpolator
     * context.
     *
     * @param template the template to replace the interpolated variables for
     * @return a string whose interpolated variables have been replaced with actual values from the interpolator context
     */
    public String interpolate(final String template) {
        return new StrSubstitutor(this.context).replace(template);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Interpolator that = (Interpolator) o;
        return Objects.equals(context, that.context);
    }

    @Override
    public int hashCode() {
        return Objects.hash(context);
    }

    @Override
    public String toString() {
        return "Interpolator{" +
                "context=" + context +
                '}';
    }
}
