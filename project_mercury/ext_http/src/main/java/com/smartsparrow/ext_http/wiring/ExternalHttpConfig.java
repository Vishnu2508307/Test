package com.smartsparrow.ext_http.wiring;

import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonProperty;

public class ExternalHttpConfig {

    @JsonProperty("submitTopicNameOrArn")
    private String submitTopicNameOrArn;

    @JsonProperty("delayQueueNameOrArn")
    private String delayQueueNameOrArn;

    public String getSubmitTopicNameOrArn() {
        return submitTopicNameOrArn;
    }

    public ExternalHttpConfig setSubmitTopicNameOrArn(final String submitTopicNameOrArn) {
        this.submitTopicNameOrArn = submitTopicNameOrArn;
        return this;
    }

    public String getDelayQueueNameOrArn() {
        return delayQueueNameOrArn;
    }

    public ExternalHttpConfig setDelayQueueNameOrArn(final String delayQueueNameOrArn) {
        this.delayQueueNameOrArn = delayQueueNameOrArn;
        return this;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ExternalHttpConfig that = (ExternalHttpConfig) o;
        return Objects.equals(submitTopicNameOrArn, that.submitTopicNameOrArn) &&
                Objects.equals(delayQueueNameOrArn, that.delayQueueNameOrArn);
    }

    @Override
    public int hashCode() {
        return Objects.hash(submitTopicNameOrArn, delayQueueNameOrArn);
    }
}
