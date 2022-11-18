package com.smartsparrow.learner.data;

import java.util.Objects;

public class Score {

    private Double value;
    private ScoreReason reason;

    public Double getValue() {
        return value;
    }

    public Score setValue(Double value) {
        this.value = value;
        return this;
    }

    public ScoreReason getReason() {
        return reason;
    }

    public Score setReason(ScoreReason reason) {
        this.reason = reason;
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Score score = (Score) o;
        return Objects.equals(value, score.value) &&
                reason == score.reason;
    }

    @Override
    public int hashCode() {
        return Objects.hash(value, reason);
    }

    @Override
    public String toString() {
        return "Score{" +
                "value=" + value +
                ", reason=" + reason +
                '}';
    }
}
