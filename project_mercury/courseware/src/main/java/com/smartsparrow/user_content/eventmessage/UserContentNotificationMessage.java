package com.smartsparrow.user_content.eventmessage;

import java.util.Objects;
import java.util.UUID;

public class UserContentNotificationMessage {

    private String type;
    private UUID referenceId;
    private String referenceType;

    public String getType() {
        return type;
    }

    public UserContentNotificationMessage setType(final String type) {
        this.type = type;
        return this;
    }

    public UUID getReferenceId() {
        return referenceId;
    }

    public UserContentNotificationMessage setReferenceId(final UUID referenceId) {
        this.referenceId = referenceId;
        return this;
    }

    public String getReferenceType() {
        return referenceType;
    }

    public UserContentNotificationMessage setReferenceType(final String referenceType) {
        this.referenceType = referenceType;
        return this;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        UserContentNotificationMessage that = (UserContentNotificationMessage) o;
        return Objects.equals(type, that.type)
                && Objects.equals(referenceId, that.referenceId)
                && Objects.equals(referenceType, that.referenceType);
    }

    @Override
    public int hashCode() {
        return Objects.hash(type, referenceId, referenceType);
    }

    @Override
    public String toString() {
        return "{\"type\" : \""+type+"\",\"referenceId\": \""+referenceId+"\",\"referenceType\": \""+referenceType+"\"}";
    }
}
