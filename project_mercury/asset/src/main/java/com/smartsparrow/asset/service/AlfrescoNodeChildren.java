package com.smartsparrow.asset.service;

import com.google.common.base.Objects;

import java.util.HashSet;
import java.util.Set;

public class AlfrescoNodeChildren {
    private String id;
    private Set<String> children;

    public AlfrescoNodeChildren() {
        children = new HashSet<>();
    }

    public String getId() {
        return id;
    }

    public AlfrescoNodeChildren setId(String uuid) {
        this.id = uuid;
        return this;
    }

    public Set<String> getChildren() {
        return children;
    }

    public boolean addChild(String id) {
        return children.add(id);
    }

    public boolean removeChild(String id) {
        return children.remove(id);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        AlfrescoNodeChildren that = (AlfrescoNodeChildren) o;
        return Objects.equal(id, that.id) &&
                Objects.equal(children, that.children);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }

    @Override
    public String toString() {
        return "AlfrescoNodeChildren{"
                + "id=" + id
                + ", children=" + children
                + '}';
    }
}
