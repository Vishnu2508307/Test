package com.smartsparrow.la.mapper.pla.data;

import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.smartsparrow.util.Json;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class LearningResourceSubject {
    private String subjectId;

    @JsonProperty("subjectId")
    public String getSubjectId() {
        return subjectId;
    }

    public LearningResourceSubject setSubjectId(String subjectId) {
        this.subjectId = subjectId;
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        LearningResourceSubject that = (LearningResourceSubject) o;
        return Objects.equals(subjectId, that.subjectId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(subjectId);
    }

    @Override
    public String toString() {
        return Json.stringify(this);
    }
}
