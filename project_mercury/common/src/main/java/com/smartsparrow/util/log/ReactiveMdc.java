package com.smartsparrow.util.log;

import java.util.Map;

import org.slf4j.MDC;

import com.smartsparrow.util.Enums;

import reactor.util.context.Context;

public class ReactiveMdc {

    /**
     * Public enum of available MDC properties
     */
    public enum Property {
        REQUEST_CONTEXT,
        TRACE_ID
    }

    /**
     * Method to get the MDC property value from the MDC context map
     * the method safely returns a default string value when the MDC context map is null or the specified key is not
     * found.
     *
     * @param property the property to get the value for from the MDC context map
     * @param defaultValue the default value to return when appropriate
     * @return the MDC property value or the default string when either the context map is null or the property is not
     * found in the map
     */
    private static String getOrDefaultMDC(final Property property, final String defaultValue) {
        Map<String, String> contextMap = MDC.getCopyOfContextMap();

        if (contextMap == null) {
            return defaultValue;
        }

        return contextMap.getOrDefault(Enums.asString(property), defaultValue);
    }

    /**
     * Creates a context with property and value. When the value is not found this is defaulted to an empty string
     *
     * @param property the property holding the access key to the value
     * @return the created context
     */
    public static Context with(final Property property) {
        return Context.of(Enums.asString(property), getOrDefaultMDC(property, ""));
    }

    /**
     * Creates a context with property and value. When the value is not found the defaultValue is used
     *
     * @param property the property holding the access key to the value
     * @param defaultValue the default context value
     * @return the created context
     */
    public static Context withOrDefault(final Property property, final String defaultValue) {
        return Context.of(Enums.asString(property), getOrDefaultMDC(property, defaultValue));
    }
}
