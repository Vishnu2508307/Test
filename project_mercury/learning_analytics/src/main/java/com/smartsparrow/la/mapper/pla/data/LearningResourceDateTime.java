package com.smartsparrow.la.mapper.pla.data;

import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.smartsparrow.util.Json;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class LearningResourceDateTime {
    private String datetimeType;
    private String datetimeValue;

    @JsonProperty("datetimeType")
    public String getDatetimeType() {
        return datetimeType;
    }

    public LearningResourceDateTime setDatetimeType(String datetimeType) {
        this.datetimeType = datetimeType;
        return this;
    }

    @JsonProperty("datetimeValue")
    public String getDatetimeValue() {
        return datetimeValue;
    }

    public LearningResourceDateTime setDatetimeValue(String datetimeValue) {
        this.datetimeValue = datetimeValue;
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        LearningResourceDateTime that = (LearningResourceDateTime) o;
        return Objects.equals(datetimeType, that.datetimeType) &&
                Objects.equals(datetimeValue, that.datetimeValue);
    }

    @Override
    public int hashCode() {
        return Objects.hash(datetimeType, datetimeValue);
    }

    @Override
    public String toString() {
        return Json.stringify(this);
    }
}
