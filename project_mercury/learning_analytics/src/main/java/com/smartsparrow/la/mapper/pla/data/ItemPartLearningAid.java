package com.smartsparrow.la.mapper.pla.data;

import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.smartsparrow.util.Json;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class ItemPartLearningAid {
    private String itemPartLearningAidType;
    private String itemPartLearningAidId;

    @JsonProperty("itemPartLearningAidType")
    public String getItemPartLearningAidType() {
        return itemPartLearningAidType;
    }

    public ItemPartLearningAid setItemPartLearningAidType(String itemPartLearningAidType) {
        this.itemPartLearningAidType = itemPartLearningAidType;
        return this;
    }

    @JsonProperty("itemPartLearningAidId")
    public String getItemPartLearningAidId() {
        return itemPartLearningAidId;
    }

    public ItemPartLearningAid setItemPartLearningAidId(String itemPartLearningAidId) {
        this.itemPartLearningAidId = itemPartLearningAidId;
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ItemPartLearningAid that = (ItemPartLearningAid) o;
        return Objects.equals(itemPartLearningAidType, that.itemPartLearningAidType) &&
                Objects.equals(itemPartLearningAidId, that.itemPartLearningAidId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(itemPartLearningAidType, itemPartLearningAidId);
    }

    @Override
    public String toString() {
        return Json.stringify(this);
    }
}
