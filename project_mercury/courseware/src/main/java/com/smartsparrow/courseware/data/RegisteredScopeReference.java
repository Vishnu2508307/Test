package com.smartsparrow.courseware.data;

import java.util.List;
import java.util.Objects;
import java.util.UUID;

public class RegisteredScopeReference {

    private UUID studentScopeUrn;
    private UUID elementId;
    private CoursewareElementType elementType;
    private List<ConfigurationField> configurationFields;
    private String configSchema;
    private UUID pluginId;
    private String pluginVersion;

    public UUID getStudentScopeUrn() {
        return studentScopeUrn;
    }

    public RegisteredScopeReference setStudentScopeUrn(final UUID studentScopeUrn) {
        this.studentScopeUrn = studentScopeUrn;
        return this;
    }

    public UUID getElementId() {
        return elementId;
    }

    public RegisteredScopeReference setElementId(final UUID elementId) {
        this.elementId = elementId;
        return this;
    }

    public CoursewareElementType getElementType() {
        return elementType;
    }

    public RegisteredScopeReference setElementType(final CoursewareElementType elementType) {
        this.elementType = elementType;
        return this;
    }

    public List<ConfigurationField> getConfigurationFields() {
        return configurationFields;
    }

    public RegisteredScopeReference setConfigurationFields(final List<ConfigurationField> configurationFields) {
        this.configurationFields = configurationFields;
        return this;
    }

    public String getConfigSchema() {
        return configSchema;
    }

    public RegisteredScopeReference setConfigSchema(final String configSchema) {
        this.configSchema = configSchema;
        return this;
    }

    public UUID getPluginId() {
        return pluginId;
    }

    public RegisteredScopeReference setPluginId(final UUID pluginId) {
        this.pluginId = pluginId;
        return this;
    }

    public String getPluginVersion() {
        return pluginVersion;
    }

    public RegisteredScopeReference setPluginVersion(final String pluginVersion) {
        this.pluginVersion = pluginVersion;
        return this;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        RegisteredScopeReference that = (RegisteredScopeReference) o;
        return Objects.equals(studentScopeUrn, that.studentScopeUrn) &&
                Objects.equals(elementId, that.elementId) &&
                elementType == that.elementType &&
                Objects.equals(configurationFields, that.configurationFields) &&
                Objects.equals(configSchema, that.configSchema) &&
                Objects.equals(pluginId, that.pluginId) &&
                Objects.equals(pluginVersion, that.pluginVersion);
    }

    @Override
    public int hashCode() {
        return Objects.hash(studentScopeUrn,
                            elementId,
                            elementType,
                            configurationFields,
                            configSchema,
                            pluginId,
                            pluginVersion);
    }

    @Override
    public String toString() {
        return "RegisteredScopeReference{" +
                "studentScopeUrn=" + studentScopeUrn +
                ", elementId=" + elementId +
                ", elementType=" + elementType +
                ", configurationFields=" + configurationFields +
                ", configSchema='" + configSchema + '\'' +
                ", pluginId=" + pluginId +
                ", pluginVersion='" + pluginVersion + '\'' +
                '}';
    }
}
