package com.smartsparrow.la.mapper.pla.data;

import java.util.List;
import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.smartsparrow.util.Json;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class ItemPartAnswerFeedback {
    private String feedbackConditionCode;
    private List<String> feedbackMessageText;

    @JsonProperty("feedbackConditionCode")
    public String getFeedbackConditionCode() {
        return feedbackConditionCode;
    }

    public ItemPartAnswerFeedback setFeedbackConditionCode(String feedbackConditionCode) {
        this.feedbackConditionCode = feedbackConditionCode;
        return this;
    }

    @JsonProperty("feedbackMessageText")
    public List<String> getFeedbackMessageText() {
        return feedbackMessageText;
    }

    public ItemPartAnswerFeedback setFeedbackMessageText(List<String> feedbackMessageText) {
        this.feedbackMessageText = feedbackMessageText;
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ItemPartAnswerFeedback that = (ItemPartAnswerFeedback) o;
        return Objects.equals(feedbackConditionCode, that.feedbackConditionCode) &&
                Objects.equals(feedbackMessageText, that.feedbackMessageText);
    }

    @Override
    public int hashCode() {
        return Objects.hash(feedbackConditionCode, feedbackMessageText);
    }

    @Override
    public String toString() {
        return Json.stringify(this);
    }
}
