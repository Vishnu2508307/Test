package com.smartsparrow.la.mapper.pla.data;

import java.util.List;
import java.util.Objects;

public class Pathway extends BronteMessageEnvelope {

    private static final String MESSAGE_TYPE_CODE = "Pathway";
    private static final String MESSAGE_VERSION = "1.0.0";
    private String messageTypeCode;
    private String messageVersion;
    private String environmentCode;
    private String transactionTypeCode;
    private String transactionDt;
    private String messageId;
    private String lessonId;
    private String learningResourceId;
    private String learningResourceIdType;
    private String pathwayType;
    private List<PathwayMember> pathwayMembers;

    @Override
    String getMessageTypeCode() {
        return MESSAGE_TYPE_CODE;
    }

    @Override
    String getMessageVersion() {
        return MESSAGE_VERSION;
    }

    @Override
    String getEnvironmentCode() {
        return environmentCode;
    }

    @Override
    String getTransactionTypeCode() {
        return transactionTypeCode;
    }

    @Override
    String getTransactionDt() {
        return transactionDt;
    }

    @Override
    String getMessageId() {
        return messageId;
    }

    public Pathway setMessageTypeCode(String messageTypeCode) {
        this.messageTypeCode = messageTypeCode;
        return this;
    }

    @Override
    public String getOriginatingSystemCode() {
        return super.getOriginatingSystemCode();
    }

    @Override
    public String getNamespaceCode() {
        return super.getNamespaceCode();
    }

    public Pathway setMessageVersion(String messageVersion) {
        this.messageVersion = messageVersion;
        return this;
    }

    public Pathway setEnvironmentCode(String environmentCode) {
        this.environmentCode = environmentCode;
        return this;
    }

    public Pathway setTransactionTypeCode(String transactionTypeCode) {
        this.transactionTypeCode = transactionTypeCode;
        return this;
    }

    public Pathway setTransactionDt(String transactionDt) {
        this.transactionDt = transactionDt;
        return this;
    }

    public Pathway setMessageId(String messageId) {
        this.messageId = messageId;
        return this;
    }

    public String getLessonId() {
        return lessonId;
    }

    public Pathway setLessonId(String lessonId) {
        this.lessonId = lessonId;
        return this;
    }

    public String getLearningResourceId() {
        return learningResourceId;
    }

    public Pathway setLearningResourceId(String learningResourceId) {
        this.learningResourceId = learningResourceId;
        return this;
    }

    public String getLearningResourceIdType() {
        return learningResourceIdType;
    }

    public Pathway setLearningResourceIdType(String learningResourceIdType) {
        this.learningResourceIdType = learningResourceIdType;
        return this;
    }

    public String getPathwayType() {
        return pathwayType;
    }

    public Pathway setPathwayType(String pathwayType) {
        this.pathwayType = pathwayType;
        return this;
    }

    public List<PathwayMember> getPathwayMembers() {
        return pathwayMembers;
    }

    public Pathway setPathwayMembers(List<PathwayMember> pathwayMembers) {
        this.pathwayMembers = pathwayMembers;
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Pathway pathway = (Pathway) o;
        return Objects.equals(messageTypeCode, pathway.messageTypeCode) &&
                Objects.equals(super.getOriginatingSystemCode(), pathway.getOriginatingSystemCode()) &&
                Objects.equals(super.getNamespaceCode(), pathway.getNamespaceCode()) &&
                Objects.equals(messageVersion, pathway.messageVersion) &&
                Objects.equals(environmentCode, pathway.environmentCode) &&
                Objects.equals(transactionTypeCode, pathway.transactionTypeCode) &&
                Objects.equals(transactionDt, pathway.transactionDt) &&
                Objects.equals(messageId, pathway.messageId) &&
                Objects.equals(lessonId, pathway.lessonId) &&
                Objects.equals(learningResourceId, pathway.learningResourceId) &&
                Objects.equals(learningResourceIdType, pathway.learningResourceIdType) &&
                Objects.equals(pathwayType, pathway.pathwayType) &&
                Objects.equals(pathwayMembers, pathway.pathwayMembers);
    }

    @Override
    public int hashCode() {
        return Objects.hash(messageTypeCode, super.getOriginatingSystemCode(), super.getNamespaceCode(), messageVersion, environmentCode, transactionTypeCode, transactionDt, messageId, lessonId, learningResourceId, learningResourceIdType, pathwayType, pathwayMembers);
    }

    @Override
    public String toString() {
        return "Pathway{" +
                "messageTypeCode='" + messageTypeCode + '\'' +
                ", originatingSystemCode='" + super.getOriginatingSystemCode() + '\'' +
                ", namespaceCode='" + super.getNamespaceCode() + '\'' +
                ", messageVersion='" + messageVersion + '\'' +
                ", environmentCode='" + environmentCode + '\'' +
                ", transactionTypeCode='" + transactionTypeCode + '\'' +
                ", transactionDt='" + transactionDt + '\'' +
                ", messageId='" + messageId + '\'' +
                ", lessonId='" + lessonId + '\'' +
                ", learningResourceId='" + learningResourceId + '\'' +
                ", learningResourceIdType='" + learningResourceIdType + '\'' +
                ", pathwayType='" + pathwayType + '\'' +
                ", pathwayMembers=" + pathwayMembers +
                "} " + super.toString();
    }
}
