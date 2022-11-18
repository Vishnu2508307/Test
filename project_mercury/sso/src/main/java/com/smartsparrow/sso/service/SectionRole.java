package com.smartsparrow.sso.service;

import com.google.common.base.Objects;

public class SectionRole {
    private String id;
    private String role;

    public SectionRole() {
    }

    public String getId() {
        return id;
    }

    public SectionRole setId(String sectionId) {
        this.id = sectionId;
        return this;
    }

    public String getRole() {
        return role;
    }

    public SectionRole setRole(String role) {
        this.role = role;
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        SectionRole that = (SectionRole) o;
        return Objects.equal(role, that.role);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(role);
    }

    @Override
    public String toString() {
        return "SectionRole{" + "role=" + role + '}';
    }
}
