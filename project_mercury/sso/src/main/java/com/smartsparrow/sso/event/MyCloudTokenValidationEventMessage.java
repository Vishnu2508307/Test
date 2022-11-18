package com.smartsparrow.sso.event;

import java.util.Objects;

/**
 * This message describes the myCloud token validation event. The class keeps the state of whether the token is valid or not.
 * {@link MyCloudTokenValidationEventMessage#isValid} is initialised with a value of false.
 * The method {@link MyCloudTokenValidationEventMessage#markValid()} allows to set the token as valid
 */
public class MyCloudTokenValidationEventMessage {

    private final String token;
    private Boolean isValid;
    private String pearsonUid;
    private Boolean hasError;

    public MyCloudTokenValidationEventMessage(String token) {
        this.token = token;
        this.isValid = false;
        this.pearsonUid = null;
        this.hasError = false;
    }

    public String getToken() {
        return token;
    }

    public Boolean isValid() {
        return isValid;
    }

    /**
     * Set the class field isValid to true, meaning the token has been validated successfully
     */
    public void markValid() {
        this.isValid = true;
    }

    public String getPearsonUid() {
        return pearsonUid;
    }

    public void setPearsonUid(String uid) {
        this.pearsonUid = uid;
    }

    public Boolean hasError() { return hasError; }

    public void markError() {
        this.hasError = true;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MyCloudTokenValidationEventMessage that = (MyCloudTokenValidationEventMessage) o;
        return Objects.equals(token, that.token) &&
                Objects.equals(isValid, that.isValid) &&
                Objects.equals(pearsonUid, that.pearsonUid) &&
                Objects.equals(hasError, that.hasError);
    }

    @Override
    public int hashCode() {
        return Objects.hash(token, isValid, pearsonUid, hasError);
    }

    @Override
    public String toString() {
        return "MyCloudTokenValidationEventMessage{" +
                "token='" + token + '\'' +
                ", isValid=" + isValid +
                ", pearsonUid='" + pearsonUid + '\'' +
                ", hasError='" + hasError + '\'' +
                '}';
    }
}
