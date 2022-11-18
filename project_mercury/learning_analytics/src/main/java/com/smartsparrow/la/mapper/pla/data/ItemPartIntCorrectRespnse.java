package com.smartsparrow.la.mapper.pla.data;

import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.smartsparrow.util.Json;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class ItemPartIntCorrectRespnse {
    private ItemPartAnswerId itemPartAnswerId;

    @JsonProperty("itemPartAnswerId")
    public ItemPartAnswerId getItemPartAnswerId() {
        return itemPartAnswerId;
    }

    public ItemPartIntCorrectRespnse setItemPartAnswerId(ItemPartAnswerId itemPartAnswerId) {
        this.itemPartAnswerId = itemPartAnswerId;
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ItemPartIntCorrectRespnse that = (ItemPartIntCorrectRespnse) o;
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
