package com.smartsparrow.courseware.data;

import java.util.Objects;
import java.util.UUID;

/**
 * Convenience Object that holds information about the {@link CoursewareElement} to register, the studentScopeURN to
 * register the element to and the {@link PluginReference} information
 */
public class ScopeReference {

    private UUID elementId;
    private UUID scopeURN;
    private CoursewareElementType elementType;
    private UUID pluginId;
    private String pluginVersion;

    public UUID getElementId() {
        return elementId;
    }

    public ScopeReference setElementId(UUID elementId) {
        this.elementId = elementId;
        return this;
    }

    public UUID getScopeURN() {
        return scopeURN;
    }

    public ScopeReference setScopeURN(UUID scopeURN) {
        this.scopeURN = scopeURN;
        return this;
    }

    public CoursewareElementType getElementType() {
        return elementType;
    }

    public ScopeReference setElementType(CoursewareElementType elementType) {
        this.elementType = elementType;
        return this;
    }

    public UUID getPluginId() {
        return pluginId;
    }

    public ScopeReference setPluginId(UUID pluginId) {
        this.pluginId = pluginId;
        return this;
    }

    public String getPluginVersion() {
        return pluginVersion;
    }

    public ScopeReference setPluginVersion(String pluginVersion) {
        this.pluginVersion = pluginVersion;
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ScopeReference that = (ScopeReference) o;
        return Objects.equals(elementId, that.elementId) &&
                Objects.equals(scopeURN, that.scopeURN) &&
                elementType == that.elementType &&
                Objects.equals(pluginId, that.pluginId) &&
                Objects.equals(pluginVersion, that.pluginVersion);
    }

    @Override
    public int hashCode() {
        return Objects.hash(elementId, scopeURN, elementType, pluginId, pluginVersion);
    }

    @Override
    public String toString() {
        return "ScopeReference{" +
                "elementId=" + elementId +
                ", scopeURN=" + scopeURN +
                ", elementType=" + elementType +
                ", pluginId=" + pluginId +
                ", pluginVersion='" + pluginVersion + '\'' +
                '}';
    }
}
