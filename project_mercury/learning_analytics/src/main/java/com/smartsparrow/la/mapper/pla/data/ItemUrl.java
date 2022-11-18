package com.smartsparrow.la.mapper.pla.data;

import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.smartsparrow.util.Json;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class ItemUrl {
    private String itemUrlType;
    private String itemUrl;

    @JsonProperty("itemUrlType")
    public String getItemUrlType() {
        return itemUrlType;
    }

    public ItemUrl setItemUrlType(String itemUrlType) {
        this.itemUrlType = itemUrlType;
        return this;
    }

    @JsonProperty("itemUrl")
    public String getItemUrl() {
        return itemUrl;
    }

    public ItemUrl setItemUrl(String itemUrl) {
        this.itemUrl = itemUrl;
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ItemUrl itemUrl1 = (ItemUrl) o;
        return Objects.equals(itemUrlType, itemUrl1.itemUrlType) &&
                Objects.equals(itemUrl, itemUrl1.itemUrl);
    }

    @Override
    public int hashCode() {
        return Objects.hash(itemUrlType, itemUrl);
    }

    @Override
    public String toString() {
        return Json.stringify(this);
    }
}
