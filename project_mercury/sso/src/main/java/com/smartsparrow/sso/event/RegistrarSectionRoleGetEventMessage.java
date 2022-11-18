package com.smartsparrow.sso.event;

import com.smartsparrow.sso.service.SectionRole;

import java.util.Objects;

public class RegistrarSectionRoleGetEventMessage {
    private final String pearsonUid;
    private final String pearsonSectionId;
    private final String accessToken;
    private SectionRole sectionRole;

    public RegistrarSectionRoleGetEventMessage(final String pearsonUid, final String pearsonSectionId, final String accessToken) {
        this.pearsonUid = pearsonUid;
        this.pearsonSectionId = pearsonSectionId;
        this.accessToken = accessToken;
    }

    public String getPearsonUid() {
        return pearsonUid;
    }

    public String getPearsonSectionId() {
        return pearsonSectionId;
    }

    public String getAccessToken() {
        return accessToken;
    }

    public SectionRole getSectionRole() {
        return sectionRole;
    }

    public RegistrarSectionRoleGetEventMessage setSectionRole(SectionRole sectionRole) {
        this.sectionRole = sectionRole;
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        RegistrarSectionRoleGetEventMessage that = (RegistrarSectionRoleGetEventMessage) o;
        return Objects.equals(pearsonUid, that.pearsonUid) &&
                Objects.equals(pearsonSectionId, that.pearsonSectionId) &&
                Objects.equals(accessToken, that.accessToken) &&
                Objects.equals(sectionRole, that.sectionRole);
    }

    @Override
    public int hashCode() {
        return Objects.hash(pearsonUid, pearsonSectionId, accessToken, sectionRole);
    }

    @Override
    public String toString() {
        return "RegistrarSectionRoleGetEventMessage{" +
                "pearsonUid='" + pearsonUid + '\'' +
                ", pearsonSectionId='" + pearsonSectionId + '\'' +
                ", accessToken='" + accessToken + '\'' +
                ", sectionRole=" + sectionRole +
                '}';
    }
}
