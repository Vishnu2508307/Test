package com.smartsparrow.la.mapper.pla.data;

import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.smartsparrow.util.Json;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class ItemPartAnswerId {
    private String itemPartAnswerId;

    @JsonProperty("itemPartAnswerId")
    public String getItemPartAnswerId() {
        return itemPartAnswerId;
    }

    public ItemPartAnswerId setItemPartAnswerId(String itemPartAnswerId) {
        this.itemPartAnswerId = itemPartAnswerId;
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ItemPartAnswerId that = (ItemPartAnswerId) o;
        return Objects.equals(itemPartAnswerId, that.itemPartAnswerId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(itemPartAnswerId);
    }

    @Override
    public String toString() {
        return Json.stringify(this);
    }
}
