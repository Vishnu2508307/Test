package com.smartsparrow.la.mapper.pla.data;

import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.smartsparrow.util.Json;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class LrIntendedEndUserRole {
    private String endUserRoleCode;

    @JsonProperty("endUserRoleCode")
    public String getEndUserRoleCode() {
        return endUserRoleCode;
    }

    public LrIntendedEndUserRole setEndUserRoleCode(String endUserRoleCode) {
        this.endUserRoleCode = endUserRoleCode;
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        LrIntendedEndUserRole that = (LrIntendedEndUserRole) o;
        return Objects.equals(endUserRoleCode, that.endUserRoleCode);
    }

    @Override
    public int hashCode() {
        return Objects.hash(endUserRoleCode);
    }

    @Override
    public String toString() {
        return Json.stringify(this);
    }
}
