package com.smartsparrow.ext_http.wiring;

import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonProperty;

public class ExternalHttpInfraResponse {

    @JsonProperty("submitTopicNameOrArn")
    private String submitTopicNameOrArn;

    @JsonProperty("delayQueueNameOrArn")
    private String delayQueueNameOrArn;

    public String getSubmitTopicNameOrArn() {
        return submitTopicNameOrArn;
    }

    public ExternalHttpInfraResponse setSubmitTopicNameOrArn(final String submitTopicNameOrArn) {
        this.submitTopicNameOrArn = submitTopicNameOrArn;
        return this;
    }

    public String getDelayQueueNameOrArn() {
        return delayQueueNameOrArn;
    }

    public ExternalHttpInfraResponse setDelayQueueNameOrArn(final String delayQueueNameOrArn) {
        this.delayQueueNameOrArn = delayQueueNameOrArn;
        return this;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ExternalHttpInfraResponse that = (ExternalHttpInfraResponse) o;
        return Objects.equals(submitTopicNameOrArn, that.submitTopicNameOrArn) &&
                Objects.equals(delayQueueNameOrArn, that.delayQueueNameOrArn);
    }

    @Override
    public int hashCode() {
        return Objects.hash(submitTopicNameOrArn, delayQueueNameOrArn);
    }

    @Override
    public String toString() {
        return "ExternalHttpInfraResponse{" +
                "submitTopicNameOrArn='" + submitTopicNameOrArn + '\'' +
                ", delayQueueNameOrArn='" + delayQueueNameOrArn + '\'' +
                '}';
    }
}
