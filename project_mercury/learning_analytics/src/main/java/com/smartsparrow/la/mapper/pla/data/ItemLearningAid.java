package com.smartsparrow.la.mapper.pla.data;

import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.smartsparrow.util.Json;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class ItemLearningAid {
    private String itemLearningAidType;
    private String itemLearningAidId;

    @JsonProperty("itemLearningAidType")
    public String getItemLearningAidType() {
        return itemLearningAidType;
    }

    public ItemLearningAid setItemLearningAidType(String itemLearningAidType) {
        this.itemLearningAidType = itemLearningAidType;
        return this;
    }

    @JsonProperty("itemLearningAidId")
    public String getItemLearningAidId() {
        return itemLearningAidId;
    }

    public ItemLearningAid setItemLearningAidId(String itemLearningAidId) {
        this.itemLearningAidId = itemLearningAidId;
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ItemLearningAid that = (ItemLearningAid) o;
        return Objects.equals(itemLearningAidType, that.itemLearningAidType) &&
                Objects.equals(itemLearningAidId, that.itemLearningAidId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(itemLearningAidType, itemLearningAidId);
    }

    @Override
    public String toString() {
        return Json.stringify(this);
    }
}
