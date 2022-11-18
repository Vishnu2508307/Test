package com.smartsparrow.la.mapper.pla.data;

import java.util.List;
import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.smartsparrow.util.Json;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class Assessment extends BronteMessageEnvelope {

    private static final String MESSAGE_TYPE_CODE = "Assessment";
    private static final String MESSAGE_VERSION = "1.1.0";
    private String messageTypeCode;
    private String originatingSystemCode;
    private String namespaceCode;
    private String messageVersion;
    private String environmentCode;
    private String transactionTypeCode;
    private String transactionDt;
    private String messageId;
    private String assessmentId;
    private String assessmentIdType;
    private String assessmentName;
    private String assessmentItemListSourceCode;
    private String assessmentPurposeCode;
    private String assessmentType;
    private List<AssessmentItem> assessmentItems;

    @Override
    @JsonProperty("messageTypeCode")
    String getMessageTypeCode() {
        return MESSAGE_TYPE_CODE;
    }

    @Override
    @JsonProperty("originatingSystemCode")
    public String getOriginatingSystemCode() {
        return super.getOriginatingSystemCode();
    }

    @Override
    @JsonProperty("namespaceCode")
    public String getNamespaceCode() {
        return super.getNamespaceCode();
    }

    @Override
    @JsonProperty("messageVersion")
    String getMessageVersion() {
        return MESSAGE_VERSION;
    }

    @Override
    @JsonProperty("environmentCode")
    String getEnvironmentCode() {
        return environmentCode;
    }

    @Override
    @JsonProperty("transactionTypeCode")
    String getTransactionTypeCode() {
        return transactionTypeCode;
    }

    @Override
    @JsonProperty("transactionDt")
    String getTransactionDt() {
        return transactionDt;
    }

    @Override
    @JsonProperty("messageId")
    String getMessageId() {
        return messageId;
    }

    public Assessment setMessageTypeCode(String messageTypeCode) {
        this.messageTypeCode = messageTypeCode;
        return this;
    }

    public Assessment setOriginatingSystemCode(String originatingSystemCode) {
        this.originatingSystemCode = originatingSystemCode;
        return this;
    }

    public Assessment setNamespaceCode(String namespaceCode) {
        this.namespaceCode = namespaceCode;
        return this;
    }

    public Assessment setMessageVersion(String messageVersion) {
        this.messageVersion = messageVersion;
        return this;
    }

    public Assessment setEnvironmentCode(String environmentCode) {
        this.environmentCode = environmentCode;
        return this;
    }

    public Assessment setTransactionTypeCode(String transactionTypeCode) {
        this.transactionTypeCode = transactionTypeCode;
        return this;
    }

    public Assessment setTransactionDt(String transactionDt) {
        this.transactionDt = transactionDt;
        return this;
    }

    public Assessment setMessageId(String messageId) {
        this.messageId = messageId;
        return this;
    }

    @JsonProperty("assessmentId")
    public String getAssessmentId() {
        return assessmentId;
    }

    public Assessment setAssessmentId(String assessmentId) {
        this.assessmentId = assessmentId;
        return this;
    }

    @JsonProperty("assessmentIdType")
    public String getAssessmentIdType() {
        return assessmentIdType;
    }

    public Assessment setAssessmentIdType(String assessmentIdType) {
        this.assessmentIdType = assessmentIdType;
        return this;
    }

    @JsonProperty("assessmentName")
    public String getAssessmentName() {
        return assessmentName;
    }

    public Assessment setAssessmentName(String assessmentName) {
        this.assessmentName = assessmentName;
        return this;
    }

    @JsonProperty("assessmentItemListSourceCode")
    public String getAssessmentItemListSourceCode() {
        return assessmentItemListSourceCode;
    }

    public Assessment setAssessmentItemListSourceCode(String assessmentItemListSourceCode) {
        this.assessmentItemListSourceCode = assessmentItemListSourceCode;
        return this;
    }

    @JsonProperty("assessmentPurposeCode")
    public String getAssessmentPurposeCode() {
        return assessmentPurposeCode;
    }

    public Assessment setAssessmentPurposeCode(String assessmentPurposeCode) {
        this.assessmentPurposeCode = assessmentPurposeCode;
        return this;
    }

    @JsonProperty("assessmentType")
    public String getAssessmentType() {
        return assessmentType;
    }

    public Assessment setAssessmentType(String assessmentType) {
        this.assessmentType = assessmentType;
        return this;
    }

    @JsonProperty("assessmentItems")
    public List<AssessmentItem> getAssessmentItems() {
        return assessmentItems;
    }

    public Assessment setAssessmentItems(List<AssessmentItem> assessmentItems) {
        this.assessmentItems = assessmentItems;
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Assessment that = (Assessment) o;
        return Objects.equals(messageTypeCode, that.messageTypeCode) &&
                Objects.equals(originatingSystemCode, that.originatingSystemCode) &&
                Objects.equals(namespaceCode, that.namespaceCode) &&
                Objects.equals(messageVersion, that.messageVersion) &&
                Objects.equals(environmentCode, that.environmentCode) &&
                Objects.equals(transactionTypeCode, that.transactionTypeCode) &&
                Objects.equals(transactionDt, that.transactionDt) &&
                Objects.equals(messageId, that.messageId) &&
                Objects.equals(assessmentId, that.assessmentId) &&
                Objects.equals(assessmentIdType, that.assessmentIdType) &&
                Objects.equals(assessmentName, that.assessmentName) &&
                Objects.equals(assessmentItemListSourceCode, that.assessmentItemListSourceCode) &&
                Objects.equals(assessmentPurposeCode, that.assessmentPurposeCode) &&
                Objects.equals(assessmentType, that.assessmentType) &&
                Objects.equals(assessmentItems, that.assessmentItems);
    }

    @Override
    public int hashCode() {
        return Objects.hash(messageTypeCode, originatingSystemCode, namespaceCode, messageVersion, environmentCode, transactionTypeCode, transactionDt, messageId, assessmentId, assessmentIdType, assessmentName, assessmentItemListSourceCode, assessmentPurposeCode, assessmentType, assessmentItems);
    }

    @Override
    public String toString() {
        return Json.stringify(this);
    }
}
