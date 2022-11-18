package com.smartsparrow.plugin.publish;

import java.util.Map;
import java.util.Objects;

public class PluginParserBuilder {
    private Map<String, PluginField> fieldMap;
    private Map<String, FallbackStrategy> fallbackStrategies;

    public Map<String, PluginField> getFieldMap() {
        return fieldMap;
    }

    public PluginParserBuilder setFieldMap(Map<String, PluginField> fieldMap) {
        this.fieldMap = fieldMap;
        return this;
    }

    public Map<String, FallbackStrategy> getFallbackStrategies() {
        return fallbackStrategies;
    }

    public PluginParserBuilder setFallbackStrategies(Map<String, FallbackStrategy> fallbackStrategies) {
        this.fallbackStrategies = fallbackStrategies;
        return this;
    }

    PluginParserBuilder withField(String fieldName, PluginField pluginField) {
        fieldMap.put(fieldName, pluginField);
        return this;
    }

    PluginParserBuilder addFallbackStrategyFor(String fieldName, FallbackStrategy fallbackStrategy) {
        fallbackStrategies.put(fieldName, fallbackStrategy);
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PluginParserBuilder that = (PluginParserBuilder) o;
        return Objects.equals(fieldMap, that.fieldMap) &&
                Objects.equals(fallbackStrategies, that.fallbackStrategies);
    }

    @Override
    public int hashCode() {
        return Objects.hash(fieldMap, fallbackStrategies);
    }

    @Override
    public String toString() {
        return "PluginParserBuilder{" +
                "fieldMap=" + fieldMap +
                ", fallbackStrategies=" + fallbackStrategies +
                '}';
    }
}
