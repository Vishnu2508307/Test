package com.smartsparrow.data;

import java.util.Objects;

/**
 * This class represent a mime type string making the type and subtype accessible with getter methods.
 */
public class MimeType {

    private static final String ERROR = "invalid mimeType format";

    private final String type;
    private final String subType;

    /**
     * Creates a new {@link MimeType} representation
     * @param mimeType the mimeType string value. The parameter requires a `type/subtype` string format.
     * @throws UnsupportedOperationException when the supplied mimeType has an invalid format
     */
    public MimeType(String mimeType) {
        if (!mimeType.contains("/")) {
            throw new UnsupportedOperationException(ERROR);
        }

        String[] parts = mimeType.split("/");

        if (parts.length != 2) {
            throw new UnsupportedOperationException(ERROR);
        }

        this.type = parts[0];
        this.subType = parts[1];
    }

    public String getType() {
        return type;
    }

    public String getSubType() {
        return subType;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MimeType mimeType = (MimeType) o;
        return Objects.equals(type, mimeType.type) &&
                Objects.equals(subType, mimeType.subType);
    }

    @Override
    public int hashCode() {
        return Objects.hash(type, subType);
    }

    @Override
    public String toString() {
        return "MimeType{" +
                "type='" + type + '\'' +
                ", subType='" + subType + '\'' +
                '}';
    }
}
