package com.smartsparrow.sso.event;

import java.util.List;
import java.util.Objects;

import com.smartsparrow.sso.service.IdentityProfile;

public class IESBatchProfileGetEventMessage {

    private final IESBatchProfileGetParams params;
    private List<IdentityProfile> identityProfile;
    private List<String> notFound;

    /**
     * Create an ies batch profile get message event
     *
     * @param params the object representing the required parameters for this request
     */
    public IESBatchProfileGetEventMessage(IESBatchProfileGetParams params) {
        this.params = params;
    }

    public IESBatchProfileGetParams getParams() {
        return params;
    }

    public List<IdentityProfile> getIdentityProfile() {
        return identityProfile;
    }

    public IESBatchProfileGetEventMessage setIdentityProfile(List<IdentityProfile> identityProfile) {
        this.identityProfile = identityProfile;
        return this;
    }

    public List<String> getNotFound() {
        return notFound;
    }

    public IESBatchProfileGetEventMessage setNotFound(List<String> notFound) {
        this.notFound = notFound;
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        IESBatchProfileGetEventMessage that = (IESBatchProfileGetEventMessage) o;
        return Objects.equals(params, that.params) &&
                Objects.equals(identityProfile, that.identityProfile) &&
                Objects.equals(notFound, that.notFound);
    }

    @Override
    public int hashCode() {
        return Objects.hash(params, identityProfile, notFound);
    }

    @Override
    public String toString() {
        return "IESBatchProfileGetEventMessage{" +
                "params=" + params +
                ", identityProfile=" + identityProfile +
                ", notFound=" + notFound +
                '}';
    }
}
