package com.smartsparrow.courseware.data;

import java.util.UUID;

import com.google.common.base.Objects;

public class FeedbackConfig {

    private UUID id;
    private UUID feedbackId;
    private String config;

    public FeedbackConfig() {
    }

    public UUID getId() {
        return id;
    }

    public FeedbackConfig setId(UUID id) {
        this.id = id;
        return this;
    }

    public UUID getFeedbackId() {
        return feedbackId;
    }

    public FeedbackConfig setFeedbackId(UUID feedbackId) {
        this.feedbackId = feedbackId;
        return this;
    }

    public String getConfig() {
        return config;
    }

    public FeedbackConfig setConfig(String config) {
        this.config = config;
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        FeedbackConfig that = (FeedbackConfig) o;
        return Objects.equal(id, that.id) && Objects.equal(feedbackId, that.feedbackId) && Objects.equal(config,
                that.config);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id, feedbackId, config);
    }

    @Override
    public String toString() {
        return "FeedbackConfig{" +
                "id=" + id +
                ", feedbackId=" + feedbackId +
                ", config='" + config + '\'' +
                '}';
    }
}
