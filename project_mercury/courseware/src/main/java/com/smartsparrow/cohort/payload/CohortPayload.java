package com.smartsparrow.cohort.payload;

import javax.annotation.Nonnull;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.Strings;

public class CohortPayload {

    @JsonProperty("summary")
    private CohortSummaryPayload summaryPayload;
    @JsonProperty("settings")
    private CohortSettingsPayload settingsPayload;

    @JsonIgnore
    public static CohortPayload from(@Nonnull CohortSummaryPayload summary, CohortSettingsPayload settings) {
        return new CohortPayload()
                .setSummaryPayload(summary)
                .setSettingsPayload(settings);
    }

    public CohortSummaryPayload getSummaryPayload() {
        return summaryPayload;
    }

    public CohortSettingsPayload getSettingsPayload() {
        return settingsPayload;
    }

    public CohortPayload setSummaryPayload(CohortSummaryPayload summaryPayload) {
        this.summaryPayload = summaryPayload;
        return this;
    }

    public CohortPayload setSettingsPayload(CohortSettingsPayload settingsPayload) {
        this.settingsPayload = settingsPayload;
        return this;
    }

    @Override
    public String toString() {
        return "CohortPayload{" +
                "summary=" + summaryPayload +
                ", settings=" + settingsPayload +
                '}';
    }
}
