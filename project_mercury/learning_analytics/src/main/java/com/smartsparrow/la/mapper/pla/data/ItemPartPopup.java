package com.smartsparrow.la.mapper.pla.data;

import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.smartsparrow.util.Json;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class ItemPartPopup {

    private String itemPartPopupType;
    private String itemPartPopupId;

    @JsonProperty("itemPartPopupType")
    public String getItemPartPopupType() {
        return itemPartPopupType;
    }

    public ItemPartPopup setItemPartPopupType(String itemPartPopupType) {
        this.itemPartPopupType = itemPartPopupType;
        return this;
    }

    @JsonProperty("itemPartPopupId")
    public String getItemPartPopupId() {
        return itemPartPopupId;
    }

    public ItemPartPopup setItemPartPopupId(String itemPartPopupId) {
        this.itemPartPopupId = itemPartPopupId;
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ItemPartPopup that = (ItemPartPopup) o;
        return Objects.equals(itemPartPopupType, that.itemPartPopupType) &&
                Objects.equals(itemPartPopupId, that.itemPartPopupId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(itemPartPopupType, itemPartPopupId);
    }

    @Override
    public String toString() {
        return Json.stringify(this);
    }
}
