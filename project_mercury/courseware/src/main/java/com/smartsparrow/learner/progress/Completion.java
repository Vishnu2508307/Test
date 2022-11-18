package com.smartsparrow.learner.progress;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.google.common.base.Objects;

/**
 * An object to represent Progress completion; values may be left unset.
 */
public class Completion implements Serializable {

    private static final long serialVersionUID = -361169056933478565L;
    private Float value;
    private Float confidence;

    public Completion() {
    }

    public Float getValue() {
        return value;
    }

    public Completion setValue(Float value) {
        this.value = value;
        return this;
    }

    public Float getConfidence() {
        return confidence;
    }

    public Completion setConfidence(Float confidence) {
        this.confidence = confidence;
        return this;
    }

    @JsonIgnore
    public boolean isCompleted() {
        return this.value.equals(1f);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        Completion that = (Completion) o;
        return Objects.equal(value, that.value) && Objects.equal(confidence, that.confidence);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(value, confidence);
    }

    @Override
    public String toString() {
        return "Completion{" + "value=" + value + ", confidence=" + confidence + '}';
    }
}
