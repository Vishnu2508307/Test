package com.smartsparrow.la.mapper.pla.data;

import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.smartsparrow.util.Json;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class ItemEduObjective {

    private String eduObjectiveTypeCode;
    private String eduObjectiveId;

    @JsonProperty("eduObjectiveTypeCode")
    public String getEduObjectiveTypeCode() {
        return eduObjectiveTypeCode;
    }

    public ItemEduObjective setEduObjectiveTypeCode(String eduObjectiveTypeCode) {
        this.eduObjectiveTypeCode = eduObjectiveTypeCode;
        return this;
    }

    @JsonProperty("eduObjectiveId")
    public String getEduObjectiveId() {
        return eduObjectiveId;
    }

    public ItemEduObjective setEduObjectiveId(String eduObjectiveId) {
        this.eduObjectiveId = eduObjectiveId;
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ItemEduObjective that = (ItemEduObjective) o;
        return Objects.equals(eduObjectiveTypeCode, that.eduObjectiveTypeCode) &&
                Objects.equals(eduObjectiveId, that.eduObjectiveId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(eduObjectiveTypeCode, eduObjectiveId);
    }

    @Override
    public String toString() {
        return Json.stringify(this);
    }
}
