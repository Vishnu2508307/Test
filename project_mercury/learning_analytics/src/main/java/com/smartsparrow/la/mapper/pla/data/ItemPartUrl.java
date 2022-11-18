package com.smartsparrow.la.mapper.pla.data;

import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.smartsparrow.util.Json;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class ItemPartUrl {

    private String itemPartUrlType;
    private String itemPartUrl;

    @JsonProperty("itemPartUrlType")
    public String getItemPartUrlType() {
        return itemPartUrlType;
    }

    public ItemPartUrl setItemPartUrlType(String itemPartUrlType) {
        this.itemPartUrlType = itemPartUrlType;
        return this;
    }

    @JsonProperty("itemPartUrl")
    public String getItemPartUrl() {
        return itemPartUrl;
    }

    public ItemPartUrl setItemPartUrl(String itemPartUrl) {
        this.itemPartUrl = itemPartUrl;
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ItemPartUrl that = (ItemPartUrl) o;
        return Objects.equals(itemPartUrlType, that.itemPartUrlType) &&
                Objects.equals(itemPartUrl, that.itemPartUrl);
    }

    @Override
    public int hashCode() {
        return Objects.hash(itemPartUrlType, itemPartUrl);
    }

    @Override
    public String toString() {
        return Json.stringify(this);
    }
}
