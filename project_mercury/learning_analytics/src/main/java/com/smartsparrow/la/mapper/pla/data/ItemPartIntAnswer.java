package com.smartsparrow.la.mapper.pla.data;

import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.smartsparrow.util.Json;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class ItemPartIntAnswer {
    private String itemPartIntAnswerId;
    private String itemPartIntAnswerText;
    private Boolean itemPartIntFreezeAnsPosition;
    private String itemPartIntAnswerEquationTmplt;
    private String itemPartIntFeedbackWhenChosen;

    @JsonProperty("itemPartIntAnswerId")
    public String getItemPartIntAnswerId() {
        return itemPartIntAnswerId;
    }

    public ItemPartIntAnswer setItemPartIntAnswerId(String itemPartIntAnswerId) {
        this.itemPartIntAnswerId = itemPartIntAnswerId;
        return this;
    }

    @JsonProperty("itemPartIntAnswerText")
    public String getItemPartIntAnswerText() {
        return itemPartIntAnswerText;
    }

    public ItemPartIntAnswer setItemPartIntAnswerText(String itemPartIntAnswerText) {
        this.itemPartIntAnswerText = itemPartIntAnswerText;
        return this;
    }

    @JsonProperty("itemPartIntFreezeAnsPosition")
    public Boolean getItemPartIntFreezeAnsPosition() {
        return itemPartIntFreezeAnsPosition;
    }

    public ItemPartIntAnswer setItemPartIntFreezeAnsPosition(Boolean itemPartIntFreezeAnsPosition) {
        this.itemPartIntFreezeAnsPosition = itemPartIntFreezeAnsPosition;
        return this;
    }

    @JsonProperty("itemPartIntAnswerEquationTmplt")
    public String getItemPartIntAnswerEquationTmplt() {
        return itemPartIntAnswerEquationTmplt;
    }

    public ItemPartIntAnswer setItemPartIntAnswerEquationTmplt(String itemPartIntAnswerEquationTmplt) {
        this.itemPartIntAnswerEquationTmplt = itemPartIntAnswerEquationTmplt;
        return this;
    }

    @JsonProperty("itemPartIntFeedbackWhenChosen")
    public String getItemPartIntFeedbackWhenChosen() {
        return itemPartIntFeedbackWhenChosen;
    }

    public ItemPartIntAnswer setItemPartIntFeedbackWhenChosen(String itemPartIntFeedbackWhenChosen) {
        this.itemPartIntFeedbackWhenChosen = itemPartIntFeedbackWhenChosen;
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ItemPartIntAnswer that = (ItemPartIntAnswer) o;
        return Objects.equals(itemPartIntAnswerId, that.itemPartIntAnswerId) &&
                Objects.equals(itemPartIntAnswerText, that.itemPartIntAnswerText) &&
                Objects.equals(itemPartIntFreezeAnsPosition, that.itemPartIntFreezeAnsPosition) &&
                Objects.equals(itemPartIntAnswerEquationTmplt, that.itemPartIntAnswerEquationTmplt) &&
                Objects.equals(itemPartIntFeedbackWhenChosen, that.itemPartIntFeedbackWhenChosen);
    }

    @Override
    public int hashCode() {
        return Objects.hash(itemPartIntAnswerId, itemPartIntAnswerText, itemPartIntFreezeAnsPosition, itemPartIntAnswerEquationTmplt, itemPartIntFeedbackWhenChosen);
    }

    @Override
    public String toString() {
        return Json.stringify(this);
    }
}
