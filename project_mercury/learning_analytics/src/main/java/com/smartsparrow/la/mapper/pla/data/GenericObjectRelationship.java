package com.smartsparrow.la.mapper.pla.data;

import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.smartsparrow.util.Json;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class GenericObjectRelationship extends BronteMessageEnvelope {

    private static final String MESSAGE_TYPE_CODE = "GenericObjectRelationship";
    private static final String MESSAGE_VERSION = "1.1.0";
    private String messageTypeCode;
    private String originatingSystemCode;
    private String namespaceCode;
    private String messageVersion;
    private String environmentCode;
    private String transactionTypeCode;
    private String transactionDt;
    private String messageId;
    private String appId;
    private String relationshipContextCode;
    private String relationshipContextId;
    private String relationshipContextFunction;
    private String relationshipContextIdType;
    private String parentObjectType;
    private String parentObjectId;
    private String parentObjectIdType;
    private String relationshipTypeCode;
    private Integer sequenceNumber;
    private String childObjectType;
    private String childObjectId;
    private String childObjectIdType;

    @Override
    @JsonProperty("messageTypeCode")
    public String getMessageTypeCode() {
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
    public String getMessageVersion() {
        return MESSAGE_VERSION;
    }

    @Override
    @JsonProperty("environmentCode")
    public String getEnvironmentCode() {
        return environmentCode;
    }

    @Override
    @JsonProperty("transactionTypeCode")
    public String getTransactionTypeCode() {
        return transactionTypeCode;
    }

    @Override
    @JsonProperty("transactionDt")
    public String getTransactionDt() {
        return transactionDt;
    }

    @Override
    @JsonProperty("messageId")
    public String getMessageId() {
        return messageId;
    }

    public GenericObjectRelationship setMessageTypeCode(String messageTypeCode) {
        this.messageTypeCode = messageTypeCode;
        return this;
    }

    public GenericObjectRelationship setOriginatingSystemCode(String originatingSystemCode) {
        this.originatingSystemCode = originatingSystemCode;
        return this;
    }

    public GenericObjectRelationship setNamespaceCode(String namespaceCode) {
        this.namespaceCode = namespaceCode;
        return this;
    }

    public GenericObjectRelationship setMessageVersion(String messageVersion) {
        this.messageVersion = messageVersion;
        return this;
    }

    public GenericObjectRelationship setEnvironmentCode(String environmentCode) {
        this.environmentCode = environmentCode;
        return this;
    }

    public GenericObjectRelationship setTransactionTypeCode(String transactionTypeCode) {
        this.transactionTypeCode = transactionTypeCode;
        return this;
    }

    public GenericObjectRelationship setTransactionDt(String transactionDt) {
        this.transactionDt = transactionDt;
        return this;
    }

    public GenericObjectRelationship setMessageId(String messageId) {
        this.messageId = messageId;
        return this;
    }

    @JsonProperty("appId")
    public String getAppId() {
        return appId;
    }

    public GenericObjectRelationship setAppId(String appId) {
        this.appId = appId;
        return this;
    }

    @JsonProperty("relationshipContextCode")
    public String getRelationshipContextCode() {
        return relationshipContextCode;
    }

    public GenericObjectRelationship setRelationshipContextCode(String relationshipContextCode) {
        this.relationshipContextCode = relationshipContextCode;
        return this;
    }

    @JsonProperty("relationshipContextId")
    public String getRelationshipContextId() {
        return relationshipContextId;
    }

    public GenericObjectRelationship setRelationshipContextId(String relationshipContextId) {
        this.relationshipContextId = relationshipContextId;
        return this;
    }

    @JsonProperty("relationshipContextFunction")
    public String getRelationshipContextFunction() {
        return relationshipContextFunction;
    }

    public GenericObjectRelationship setRelationshipContextFunction(String relationshipContextFunction) {
        this.relationshipContextFunction = relationshipContextFunction;
        return this;
    }

    @JsonProperty("relationshipContextIdType")
    public String getRelationshipContextIdType() {
        return relationshipContextIdType;
    }

    public GenericObjectRelationship setRelationshipContextIdType(String relationshipContextIdType) {
        this.relationshipContextIdType = relationshipContextIdType;
        return this;
    }

    @JsonProperty("parentObjectType")
    public String getParentObjectType() {
        return parentObjectType;
    }

    public GenericObjectRelationship setParentObjectType(String parentObjectType) {
        this.parentObjectType = parentObjectType;
        return this;
    }

    @JsonProperty("parentObjectId")
    public String getParentObjectId() {
        return parentObjectId;
    }

    public GenericObjectRelationship setParentObjectId(String parentObjectId) {
        this.parentObjectId = parentObjectId;
        return this;
    }

    @JsonProperty("parentObjectIdType")
    public String getParentObjectIdType() {
        return parentObjectIdType;
    }

    public GenericObjectRelationship setParentObjectIdType(String parentObjectIdType) {
        this.parentObjectIdType = parentObjectIdType;
        return this;
    }

    @JsonProperty("relationshipTypeCode")
    public String getRelationshipTypeCode() {
        return relationshipTypeCode;
    }

    public GenericObjectRelationship setRelationshipTypeCode(String relationshipTypeCode) {
        this.relationshipTypeCode = relationshipTypeCode;
        return this;
    }

    @JsonProperty("sequenceNumber")
    public Integer getSequenceNumber() {
        return sequenceNumber;
    }

    public GenericObjectRelationship setSequenceNumber(Integer sequenceNumber) {
        this.sequenceNumber = sequenceNumber;
        return this;
    }

    @JsonProperty("childObjectType")
    public String getChildObjectType() {
        return childObjectType;
    }

    public GenericObjectRelationship setChildObjectType(String childObjectType) {
        this.childObjectType = childObjectType;
        return this;
    }

    @JsonProperty("childObjectId")
    public String getChildObjectId() {
        return childObjectId;
    }

    public GenericObjectRelationship setChildObjectId(String childObjectId) {
        this.childObjectId = childObjectId;
        return this;
    }

    @JsonProperty("childObjectIdType")
    public String getChildObjectIdType() {
        return childObjectIdType;
    }

    public GenericObjectRelationship setChildObjectIdType(String childObjectIdType) {
        this.childObjectIdType = childObjectIdType;
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        GenericObjectRelationship that = (GenericObjectRelationship) o;
        return Objects.equals(messageTypeCode, that.messageTypeCode) &&
                Objects.equals(originatingSystemCode, that.originatingSystemCode) &&
                Objects.equals(namespaceCode, that.namespaceCode) &&
                Objects.equals(messageVersion, that.messageVersion) &&
                Objects.equals(environmentCode, that.environmentCode) &&
                Objects.equals(transactionTypeCode, that.transactionTypeCode) &&
                Objects.equals(transactionDt, that.transactionDt) &&
                Objects.equals(messageId, that.messageId) &&
                Objects.equals(appId, that.appId) &&
                Objects.equals(relationshipContextCode, that.relationshipContextCode) &&
                Objects.equals(relationshipContextId, that.relationshipContextId) &&
                Objects.equals(relationshipContextFunction, that.relationshipContextFunction) &&
                Objects.equals(relationshipContextIdType, that.relationshipContextIdType) &&
                Objects.equals(parentObjectType, that.parentObjectType) &&
                Objects.equals(parentObjectId, that.parentObjectId) &&
                Objects.equals(parentObjectIdType, that.parentObjectIdType) &&
                Objects.equals(relationshipTypeCode, that.relationshipTypeCode) &&
                Objects.equals(sequenceNumber, that.sequenceNumber) &&
                Objects.equals(childObjectType, that.childObjectType) &&
                Objects.equals(childObjectId, that.childObjectId) &&
                Objects.equals(childObjectIdType, that.childObjectIdType);
    }

    @Override
    public int hashCode() {
        return Objects.hash(messageTypeCode, originatingSystemCode, namespaceCode, messageVersion, environmentCode, transactionTypeCode, transactionDt, messageId, appId, relationshipContextCode, relationshipContextId, relationshipContextFunction, relationshipContextIdType, parentObjectType, parentObjectId, parentObjectIdType, relationshipTypeCode, sequenceNumber, childObjectType, childObjectId, childObjectIdType);
    }

    @Override
    public String toString() {
        return Json.stringify(this);
    }
}
