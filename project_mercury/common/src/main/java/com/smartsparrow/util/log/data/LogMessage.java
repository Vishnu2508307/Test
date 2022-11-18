package com.smartsparrow.util.log.data;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.smartsparrow.util.Json;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class LogMessage {

    private Map<String, Object> fields = new HashMap<>();

    @JsonAnyGetter
    public Map<String, Object> getFields() {
        return fields;
    }

    @JsonAnySetter
    public LogMessage setField(final String name, final Object value) {
        fields.put(name, value);
        return this;
    }

    public LogMessage setFields(final Map<String, Object> fields) {
        this.fields.putAll(fields);
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        LogMessage that = (LogMessage) o;
        return Objects.equals(fields, that.fields);
    }

    @Override
    public int hashCode() {
        return Objects.hash(fields);
    }

    @Override
    public String toString() {
        return Json.stringify(this);
    }
}
