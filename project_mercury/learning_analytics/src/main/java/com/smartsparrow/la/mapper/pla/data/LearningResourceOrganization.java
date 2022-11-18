package com.smartsparrow.la.mapper.pla.data;

import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.smartsparrow.util.Json;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class LearningResourceOrganization {
    private String organizationRoleCode;
    private FreeformData freeformData;
    private StructuredData structuredData;

    @JsonProperty("organizationRoleCode")
    public String getOrganizationRoleCode() {
        return organizationRoleCode;
    }

    public LearningResourceOrganization setOrganizationRoleCode(String organizationRoleCode) {
        this.organizationRoleCode = organizationRoleCode;
        return this;
    }

    @JsonProperty("freeformData")
    public FreeformData getFreeformData() {
        return freeformData;
    }

    public LearningResourceOrganization setFreeformData(FreeformData freeformData) {
        this.freeformData = freeformData;
        return this;
    }

    @JsonProperty("structuredData")
    public StructuredData getStructuredData() {
        return structuredData;
    }

    public LearningResourceOrganization setStructuredData(StructuredData structuredData) {
        this.structuredData = structuredData;
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        LearningResourceOrganization that = (LearningResourceOrganization) o;
        return Objects.equals(organizationRoleCode, that.organizationRoleCode) &&
                Objects.equals(freeformData, that.freeformData) &&
                Objects.equals(structuredData, that.structuredData);
    }

    @Override
    public int hashCode() {
        return Objects.hash(organizationRoleCode, freeformData, structuredData);
    }

    @Override
    public String toString() {
        return Json.stringify(this);
    }
}
