package com.smartsparrow.sso.service;

import java.io.Serializable;

import com.google.common.base.Objects;

public class IdentityProfile implements Serializable {

    private static final long serialVersionUID = 2462310017525082475L;

    private String id;
    private String givenName;
    private String familyName;
    private String primaryEmail;

    public IdentityProfile() {
    }

    public String getId() {
        return id;
    }

    public IdentityProfile setId(String uuid) {
        this.id = uuid;
        return this;
    }

    public String getGivenName() {
        return givenName;
    }

    public IdentityProfile setGivenName(String givenName) {
        this.givenName = givenName;
        return this;
    }

    public String getFamilyName() {
        return familyName;
    }

    public IdentityProfile setFamilyName(String familyName) {
        this.familyName = familyName;
        return this;
    }

    public String getPrimaryEmail() {
        return primaryEmail;
    }

    public IdentityProfile setPrimaryEmail(String primaryEmail) {
        this.primaryEmail = primaryEmail;
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        IdentityProfile that = (IdentityProfile) o;
        return Objects.equal(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }

    @Override
    public String toString() {
        return "IdentityProfile{" + "id=" + id + '}';
    }
}
