package com.smartsparrow.la.mapper.pla.data;

import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.smartsparrow.util.Json;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class ItemPartAnswerChoiceDisplay {
    private String choiceIdentificationCode;

    @JsonProperty("choiceIdentificationCode")
    public String getChoiceIdentificationCode() {
        return choiceIdentificationCode;
    }

    public ItemPartAnswerChoiceDisplay setChoiceIdentificationCode(String choiceIdentificationCode) {
        this.choiceIdentificationCode = choiceIdentificationCode;
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ItemPartAnswerChoiceDisplay that = (ItemPartAnswerChoiceDisplay) o;
        return Objects.equals(choiceIdentificationCode, that.choiceIdentificationCode);
    }

    @Override
    public int hashCode() {
        return Objects.hash(choiceIdentificationCode);
    }

    @Override
    public String toString() {
        return Json.stringify(this);
    }
}
