package com.smartsparrow.iam.service;

import java.util.Objects;
import java.util.UUID;

import com.google.common.base.MoreObjects;

/**
 * A high-level identifier which ties users to the same set of shared configuration and limits.
 */
public class Subscription {

    private UUID id;
    private String name;
    private Region iamRegion;

    public Subscription() {
    }

    public UUID getId() {
        return id;
    }

    public Subscription setId(UUID id) {
        this.id = id;
        return this;
    }

    public String getName() {
        return name;
    }

    public Subscription setName(String name) {
        this.name = name;
        return this;
    }

    public Region getIamRegion() {
        return iamRegion;
    }

    public Subscription setIamRegion(Region iamRegion) {
        this.iamRegion = iamRegion;
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        Subscription that = (Subscription) o;
        return Objects.equals(id, that.id) && Objects.equals(name, that.name) && iamRegion == that.iamRegion;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name, iamRegion);
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this).add("id", id).add("name", name).add("iamRegion", iamRegion).toString();
    }
}
