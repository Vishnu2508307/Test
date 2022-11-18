package com.smartsparrow.la.mapper.pla.data;

import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.smartsparrow.util.Json;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class FreeformData {
    private String personName;
    private String organizationName;

    @JsonProperty("personName")
    public String getPersonName() {
        return personName;
    }

    public FreeformData setPersonName(String personName) {
        this.personName = personName;
        return this;
    }

    @JsonProperty("organizationName")
    public String getOrganizationName() {
        return organizationName;
    }

    public FreeformData setOrganizationName(String organizationName) {
        this.organizationName = organizationName;
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        FreeformData that = (FreeformData) o;
        return Objects.equals(personName, that.personName) &&
                Objects.equals(organizationName, that.organizationName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(personName, organizationName);
    }

    @Override
    public String toString() {
        return Json.stringify(this);
    }
}
