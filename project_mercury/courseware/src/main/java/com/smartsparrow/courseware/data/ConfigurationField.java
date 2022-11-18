package com.smartsparrow.courseware.data;

import java.util.Objects;

public class ConfigurationField {

    public static final String TITLE = "title";

    private String fieldName;
    private String fieldValue;

    public String getFieldName() {
        return fieldName;
    }

    public ConfigurationField setFieldName(String fieldName) {
        this.fieldName = fieldName;
        return this;
    }

    public String getFieldValue() {
        return fieldValue;
    }

    public ConfigurationField setFieldValue(String fieldValue) {
        this.fieldValue = fieldValue;
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ConfigurationField that = (ConfigurationField) o;
        return Objects.equals(fieldName, that.fieldName) &&
                Objects.equals(fieldValue, that.fieldValue);
    }

    @Override
    public int hashCode() {
        return Objects.hash(fieldName, fieldValue);
    }

    @Override
    public String toString() {
        return "ConfigurationField{" +
                "fieldName='" + fieldName + '\'' +
                ", fieldValue='" + fieldValue + '\'' +
                '}';
    }
}
