package com.smartsparrow.rtm.message.recv.iam;

import java.util.Objects;

import com.smartsparrow.rtm.message.ReceivedMessage;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

@SuppressFBWarnings(value = "UWF_UNWRITTEN_FIELD", justification = "Hydrated via Jackson on message deserialization")
public class AccountProvisionMessage extends ReceivedMessage {

    private String honorificPrefix;
    private String givenName;
    private String familyName;
    private String honorificSuffix;
    private String email;
    private String password;
    private String affiliation;
    private String jobTitle;

    public AccountProvisionMessage()  {
    }

    public String getHonorificPrefix() {
        return honorificPrefix;
    }

    public String getGivenName() {
        return givenName;
    }

    public String getFamilyName() {
        return familyName;
    }

    public String getHonorificSuffix() {
        return honorificSuffix;
    }

    public String getEmail() {
        return email;
    }

    public String getPassword() {
        return password;
    }

    public String getAffiliation() {
        return affiliation;
    }

    public String getJobTitle() {
        return jobTitle;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AccountProvisionMessage that = (AccountProvisionMessage) o;
        return Objects.equals(honorificPrefix, that.honorificPrefix) &&
                Objects.equals(givenName, that.givenName) &&
                Objects.equals(familyName, that.familyName) &&
                Objects.equals(honorificSuffix, that.honorificSuffix) &&
                Objects.equals(email, that.email) &&
                Objects.equals(password, that.password) &&
                Objects.equals(affiliation, that.affiliation) &&
                Objects.equals(jobTitle, that.jobTitle);
    }

    @Override
    public int hashCode() {
        return Objects.hash(honorificPrefix, givenName, familyName, honorificSuffix, email, password, affiliation, jobTitle);
    }

    @Override
    public String toString() {
        return "AccountProvisionMessage{" +
                "honorificPrefix='" + honorificPrefix + '\'' +
                ", givenName='" + givenName + '\'' +
                ", familyName='" + familyName + '\'' +
                ", honorificSuffix='" + honorificSuffix + '\'' +
                ", email='" + email + '\'' +
                ", password='" + password + '\'' +
                ", affiliation='" + affiliation + '\'' +
                ", jobTitle='" + jobTitle + '\'' +
                '}';
    }
}
