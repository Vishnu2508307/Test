package com.smartsparrow.la.mapper.pla.data;

import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.smartsparrow.util.Json;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class StructuredData {
    private String organizationSourceSystemCode;
    private String organizationSsrId;

    @JsonProperty("Organization_Source_System_Code")
    public String getOrganizationSourceSystemCode() {
        return organizationSourceSystemCode;
    }

    public StructuredData setOrganizationSourceSystemCode(String organizationSourceSystemCode) {
        this.organizationSourceSystemCode = organizationSourceSystemCode;
        return this;
    }

    @JsonProperty("organizationSsrId")
    public String getOrganizationSsrId() {
        return organizationSsrId;
    }

    public StructuredData setOrganizationSsrId(String organizationSsrId) {
        this.organizationSsrId = organizationSsrId;
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        StructuredData that = (StructuredData) o;
        return Objects.equals(organizationSourceSystemCode, that.organizationSourceSystemCode) &&
                Objects.equals(organizationSsrId, that.organizationSsrId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(organizationSourceSystemCode, organizationSsrId);
    }

    @Override
    public String toString() {
        return Json.stringify(this);
    }
}
