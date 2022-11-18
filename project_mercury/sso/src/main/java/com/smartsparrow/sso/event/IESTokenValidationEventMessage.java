package com.smartsparrow.sso.event;

import java.util.Objects;

/**
 * This message describes the ies token validation event. The class keeps the state of whether the token is valid or not.
 * {@link IESTokenValidationEventMessage#isValid} is initialised with a value of false.
 * The method {@link IESTokenValidationEventMessage#markValid()} allows to set the token as valid
 */
public class IESTokenValidationEventMessage {

    private final String token;
    private Boolean isValid;

    public IESTokenValidationEventMessage(String token) {
        this.token = token;
        this.isValid = false;
    }

    public String getToken() {
        return token;
    }

    public Boolean getValid() {
        return isValid;
    }

    /**
     * Set the class field isValid to true, meaning the token has been validated successfully
     */
    public void markValid() {
        this.isValid = true;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        IESTokenValidationEventMessage that = (IESTokenValidationEventMessage) o;
        return Objects.equals(token, that.token) &&
                Objects.equals(isValid, that.isValid);
    }

    @Override
    public int hashCode() {
        return Objects.hash(token, isValid);
    }

    @Override
    public String toString() {
        return "IESTokenValidationEventMessage{" +
                "token='" + token + '\'' +
                ", isValid=" + isValid +
                '}';
    }
}
