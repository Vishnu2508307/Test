package com.smartsparrow.ingestion.eventmessage;

import java.util.Objects;

import com.smartsparrow.ingestion.data.IngestionSummary;

/**
 * This message describes the bearerToken and the ingestion summary. The message is passed to ambrosia through SQS.
 */
public class IngestionSummaryEventMessage {

    private String bearerToken;
    private IngestionSummary ingestionSummary;

    public IngestionSummaryEventMessage(final String bearerToken, final IngestionSummary ingestionSummary) {
        this.bearerToken = bearerToken;
        this.ingestionSummary = ingestionSummary;
    }

    public String getBearerToken() {
        return bearerToken;
    }

    public IngestionSummary getIngestionSummary() {
        return ingestionSummary;
    }

    public IngestionSummaryEventMessage setBearerToken(final String bearerToken) {
        this.bearerToken = bearerToken;
        return this;
    }

    public IngestionSummaryEventMessage setIngestionSummary(final IngestionSummary ingestionSummary) {
        this.ingestionSummary = ingestionSummary;
        return this;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        IngestionSummaryEventMessage that = (IngestionSummaryEventMessage) o;
        return Objects.equals(bearerToken, that.bearerToken) &&
                Objects.equals(ingestionSummary, that.ingestionSummary);
    }

    @Override
    public int hashCode() {
        return Objects.hash(bearerToken, ingestionSummary);
    }

    @Override
    public String toString() {
        return "IngestionSummaryEventMessage{" +
                "bearerToken='" + bearerToken + '\'' +
                ", ingestionSummary=" + ingestionSummary +
                '}';
    }
}
