package com.smartsparrow.la.mapper.pla.data;

import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.smartsparrow.util.Json;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class ItemPartAnswer {
    private String itemPartAnswerId;
    private String itemPartAnswerText;
    private String itemPartAnswerGraphicId;
    private String itemPartAnswerEquationTemplate;
    private Boolean freezeAnswerChoicePosition;

    @JsonProperty("itemPartAnswerId")
    public String getItemPartAnswerId() {
        return itemPartAnswerId;
    }

    public ItemPartAnswer setItemPartAnswerId(String itemPartAnswerId) {
        this.itemPartAnswerId = itemPartAnswerId;
        return this;
    }

    @JsonProperty("itemPartAnswerText")
    public String getItemPartAnswerText() {
        return itemPartAnswerText;
    }

    public ItemPartAnswer setItemPartAnswerText(String itemPartAnswerText) {
        this.itemPartAnswerText = itemPartAnswerText;
        return this;
    }

    @JsonProperty("itemPartAnswerGraphicId")
    public String getItemPartAnswerGraphicId() {
        return itemPartAnswerGraphicId;
    }

    public ItemPartAnswer setItemPartAnswerGraphicId(String itemPartAnswerGraphicId) {
        this.itemPartAnswerGraphicId = itemPartAnswerGraphicId;
        return this;
    }

    @JsonProperty("itemPartAnswerEquationTemplate")
    public String getItemPartAnswerEquationTemplate() {
        return itemPartAnswerEquationTemplate;
    }

    public ItemPartAnswer setItemPartAnswerEquationTemplate(String itemPartAnswerEquationTemplate) {
        this.itemPartAnswerEquationTemplate = itemPartAnswerEquationTemplate;
        return this;
    }

    @JsonProperty("freezeAnswerChoicePosition")
    public Boolean getFreezeAnswerChoicePosition() {
        return freezeAnswerChoicePosition;
    }

    public ItemPartAnswer setFreezeAnswerChoicePosition(Boolean freezeAnswerChoicePosition) {
        this.freezeAnswerChoicePosition = freezeAnswerChoicePosition;
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ItemPartAnswer that = (ItemPartAnswer) o;
        return Objects.equals(itemPartAnswerId, that.itemPartAnswerId) &&
                Objects.equals(itemPartAnswerText, that.itemPartAnswerText) &&
                Objects.equals(itemPartAnswerGraphicId, that.itemPartAnswerGraphicId) &&
                Objects.equals(itemPartAnswerEquationTemplate, that.itemPartAnswerEquationTemplate) &&
                Objects.equals(freezeAnswerChoicePosition, that.freezeAnswerChoicePosition);
    }

    @Override
    public int hashCode() {
        return Objects.hash(itemPartAnswerId, itemPartAnswerText, itemPartAnswerGraphicId, itemPartAnswerEquationTemplate, freezeAnswerChoicePosition);
    }

    @Override
    public String toString() {
        return Json.stringify(this);
    }
}
