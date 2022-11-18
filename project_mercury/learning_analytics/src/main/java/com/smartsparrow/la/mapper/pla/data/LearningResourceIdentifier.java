package com.smartsparrow.la.mapper.pla.data;

import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.smartsparrow.util.Json;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class LearningResourceIdentifier {
    private String lrIdentifierTypeCode;
    private String learningResourceIdentifier;

    @JsonProperty("lrIdentifierTypeCode")
    public String getLrIdentifierTypeCode() {
        return lrIdentifierTypeCode;
    }

    public LearningResourceIdentifier setLrIdentifierTypeCode(String lrIdentifierTypeCode) {
        this.lrIdentifierTypeCode = lrIdentifierTypeCode;
        return this;
    }

    @JsonProperty("learningResourceIdentifier")
    public String getLearningResourceIdentifier() {
        return learningResourceIdentifier;
    }

    public LearningResourceIdentifier setLearningResourceIdentifier(String learningResourceIdentifier) {
        this.learningResourceIdentifier = learningResourceIdentifier;
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        LearningResourceIdentifier that = (LearningResourceIdentifier) o;
        return Objects.equals(lrIdentifierTypeCode, that.lrIdentifierTypeCode) &&
                Objects.equals(learningResourceIdentifier, that.learningResourceIdentifier);
    }

    @Override
    public int hashCode() {
        return Objects.hash(lrIdentifierTypeCode, learningResourceIdentifier);
    }

    @Override
    public String toString() {
        return Json.stringify(this);
    }
}
