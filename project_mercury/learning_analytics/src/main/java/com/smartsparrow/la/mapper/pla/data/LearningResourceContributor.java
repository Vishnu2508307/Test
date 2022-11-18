package com.smartsparrow.la.mapper.pla.data;

import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.smartsparrow.util.Json;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class LearningResourceContributor {
    private String personRoleCode;
    private FreeformData freeformData;

    @JsonProperty("personRoleCode")
    public String getPersonRoleCode() {
        return personRoleCode;
    }

    public LearningResourceContributor setPersonRoleCode(String personRoleCode) {
        this.personRoleCode = personRoleCode;
        return this;
    }

    @JsonProperty("freeformData")
    public FreeformData getFreeformData() {
        return freeformData;
    }

    public LearningResourceContributor setFreeformData(FreeformData freeformData) {
        this.freeformData = freeformData;
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        LearningResourceContributor that = (LearningResourceContributor) o;
        return Objects.equals(personRoleCode, that.personRoleCode) &&
                Objects.equals(freeformData, that.freeformData);
    }

    @Override
    public int hashCode() {
        return Objects.hash(personRoleCode, freeformData);
    }

    @Override
    public String toString() {
        return Json.stringify(this);
    }
}
