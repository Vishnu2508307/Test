package com.smartsparrow.sso.event;

import java.util.Objects;

import com.smartsparrow.sso.service.IdentityProfile;

public class IESProfileGetEventMessage {

    private final String pearsonUid;
    private final String accessToken;
    private IdentityProfile identityProfile;

    public IESProfileGetEventMessage(final String pearsonUid, final String accessToken) {
        this.pearsonUid = pearsonUid;
        this.accessToken = accessToken;
    }

    public String getPearsonUid() {
        return pearsonUid;
    }

    public String getAccessToken() {
        return accessToken;
    }

    public IdentityProfile getIdentityProfile() {
        return identityProfile;
    }

    public IESProfileGetEventMessage setIdentityProfile(IdentityProfile identityProfile) {
        this.identityProfile = identityProfile;
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        IESProfileGetEventMessage that = (IESProfileGetEventMessage) o;
        return Objects.equals(pearsonUid, that.pearsonUid) &&
                Objects.equals(accessToken, that.accessToken) &&
                Objects.equals(identityProfile, that.identityProfile);
    }

    @Override
    public int hashCode() {
        return Objects.hash(pearsonUid, accessToken, identityProfile);
    }

    @Override
    public String toString() {
        return "IESProfileGetEventMessage{" +
                "pearsonUid='" + pearsonUid + '\'' +
                ", accessToken='" + "****" + '\'' +
                ", identityProfile=" + identityProfile +
                '}';
    }
}
