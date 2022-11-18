package com.smartsparrow.la.mapper.pla.data;

import java.util.List;
import java.util.Objects;

import javax.annotation.Nullable;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.smartsparrow.util.Json;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class LearningResource extends BronteMessageEnvelope {
    private static final String MESSAGE_TYPE_CODE = "LearningResource";
    private static final String MESSAGE_VERSION = "3.2.0";
    private String messageTypeCode;
    private String originatingSystemCode;
    private String namespaceCode;
    private String messageVersion;
    private String environmentCode;
    private String transactionTypeCode;
    private String transactionDt;
    private String messageId;
    private String learningResourceId;
    private String learningResourceVersionId;
    private String learningResourceIdType;
    private String title;
    private String version;
    private String learningResourceTypeCode;
    private String timeCategorizationCodeLearning;
    private String lrCreationStatusCode;
    private String lrMediaTypeCode;
    private String description;
    private String deliveryPlatformCode;
    private List<LearningResourceComponent> learningResourceComponents;
    private List<LearningResourceDateTime> learningResourceDateTimes;
    private List<LearningResourceContributor> learningResourceContributors;
    private List<LearningResourceOrganization> learningResourceOrganizations;
    private List<LearningResourceIdentifier> learningResourceIdentifiers;
    private List<LearningResourceDiscipline> learningResourceDisciplines;
    private List<LearningResourceSubject> learningResourceSubjects;
    private List<LearningResourceKeyword> LearningResourceKeywords;
    private List<LrIntendedEndUserRole> lrIntendedEndUserRoles;
    private List<LrEduObjective> lrEduObjectives;
    private String lrContentModelTypeCode;
    private String refLanguageCode;
    private String parentLearningResourceId;
    private String asPublishedPriorActionMessageTypeCode;
    private String asPublishedPriorActionIdentifier;
    private String asPublishedPriorActionIdentifierValue;
    private String asPublishedNextActionMessageTypeCode;
    private String asPublishedNextActionIdentifier;
    private String asPublishedNextActionIdentifierValue;

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

    public LearningResource setMessageTypeCode(String messageTypeCode) {
        this.messageTypeCode = messageTypeCode;
        return this;
    }

    public LearningResource setOriginatingSystemCode(String originatingSystemCode) {
        this.originatingSystemCode = originatingSystemCode;
        return this;
    }

    public LearningResource setNamespaceCode(String namespaceCode) {
        this.namespaceCode = namespaceCode;
        return this;
    }

    public LearningResource setMessageVersion(String messageVersion) {
        this.messageVersion = messageVersion;
        return this;
    }

    public LearningResource setEnvironmentCode(String environmentCode) {
        this.environmentCode = environmentCode;
        return this;
    }

    public LearningResource setTransactionTypeCode(String transactionTypeCode) {
        this.transactionTypeCode = transactionTypeCode;
        return this;
    }

    public LearningResource setTransactionDt(String transactionDt) {
        this.transactionDt = transactionDt;
        return this;
    }

    public LearningResource setMessageId(String messageId) {
        this.messageId = messageId;
        return this;
    }

    @JsonProperty("learningResourceId")
    public String getLearningResourceId() {
        return learningResourceId;
    }

    public LearningResource setLearningResourceId(String learningResourceId) {
        this.learningResourceId = learningResourceId;
        return this;
    }

    @JsonProperty("learningResourceIdType")
    public String getLearningResourceIdType() {
        return learningResourceIdType;
    }

    public LearningResource setLearningResourceIdType(String learningResourceIdType) {
        this.learningResourceIdType = learningResourceIdType;
        return this;
    }

    @JsonProperty("title")
    public String getTitle() {
        return title;
    }

    public LearningResource setTitle(String title) {
        this.title = title;
        return this;
    }

    @JsonProperty("learningResourceTypeCode")
    public String getLearningResourceTypeCode() {
        return learningResourceTypeCode;
    }

    public LearningResource setLearningResourceTypeCode(String learningResourceTypeCode) {
        this.learningResourceTypeCode = learningResourceTypeCode;
        return this;
    }

    @JsonProperty("timeCategorizationCode")
    public String getTimeCategorizationCodeLearning() {
        return timeCategorizationCodeLearning;
    }

    public LearningResource setTimeCategorizationCodeLearning(String timeCategorizationCodeLearning) {
        this.timeCategorizationCodeLearning = timeCategorizationCodeLearning;
        return this;
    }

    @JsonProperty("description")
    public String getDescription() {
        return description;
    }

    public LearningResource setDescription(String description) {
        this.description = description;
        return this;
    }

    @JsonProperty("deliveryPlatformCode")
    public String getDeliveryPlatformCode() {
        return deliveryPlatformCode;
    }

    public LearningResource setDeliveryPlatformCode(String deliveryPlatformCode) {
        this.deliveryPlatformCode = deliveryPlatformCode;
        return this;
    }

    @JsonProperty("learningResourceComponents")
    @Nullable
    public List<LearningResourceComponent> getLearningResourceComponents() {
        return learningResourceComponents;
    }

    public LearningResource setLearningResourceComponents(List<LearningResourceComponent> learningResourceComponents) {
        this.learningResourceComponents = learningResourceComponents;
        return this;
    }

    @JsonProperty("learningResourceVersionId")
    public String getLearningResourceVersionId() {
        return learningResourceVersionId;
    }

    public LearningResource setLearningResourceVersionId(String learningResourceVersionId) {
        this.learningResourceVersionId = learningResourceVersionId;
        return this;
    }

    @JsonProperty("version")
    public String getVersion() {
        return version;
    }

    public LearningResource setVersion(String version) {
        this.version = version;
        return this;
    }

    @JsonProperty("lrCreationStatusCode")
    public String getLrCreationStatusCode() {
        return lrCreationStatusCode;
    }

    public LearningResource setLrCreationStatusCode(String lrCreationStatusCode) {
        this.lrCreationStatusCode = lrCreationStatusCode;
        return this;
    }

    @JsonProperty("lrMediaTypeCode")
    public String getLrMediaTypeCode() {
        return lrMediaTypeCode;
    }

    public LearningResource setLrMediaTypeCode(String lrMediaTypeCode) {
        this.lrMediaTypeCode = lrMediaTypeCode;
        return this;
    }

    @JsonProperty("learningResourceDateTimes")
    public List<LearningResourceDateTime> getLearningResourceDateTimes() {
        return learningResourceDateTimes;
    }

    public LearningResource setLearningResourceDateTimes(List<LearningResourceDateTime> learningResourceDateTimes) {
        this.learningResourceDateTimes = learningResourceDateTimes;
        return this;
    }

    @JsonProperty("learningResourceContributors")
    public List<LearningResourceContributor> getLearningResourceContributors() {
        return learningResourceContributors;
    }

    public LearningResource setLearningResourceContributors(List<LearningResourceContributor> learningResourceContributors) {
        this.learningResourceContributors = learningResourceContributors;
        return this;
    }

    @JsonProperty("learningResourceOrganizations")
    public List<LearningResourceOrganization> getLearningResourceOrganizations() {
        return learningResourceOrganizations;
    }

    public LearningResource setLearningResourceOrganizations(List<LearningResourceOrganization> learningResourceOrganizations) {
        this.learningResourceOrganizations = learningResourceOrganizations;
        return this;
    }

    @JsonProperty("learningResourceIdentifiers")
    public List<LearningResourceIdentifier> getLearningResourceIdentifiers() {
        return learningResourceIdentifiers;
    }

    public LearningResource setLearningResourceIdentifiers(List<LearningResourceIdentifier> learningResourceIdentifiers) {
        this.learningResourceIdentifiers = learningResourceIdentifiers;
        return this;
    }

    @JsonProperty("learningResourceDisciplines")
    public List<LearningResourceDiscipline> getLearningResourceDisciplines() {
        return learningResourceDisciplines;
    }

    public LearningResource setLearningResourceDisciplines(List<LearningResourceDiscipline> learningResourceDisciplines) {
        this.learningResourceDisciplines = learningResourceDisciplines;
        return this;
    }

    @JsonProperty("learningResourceSubjects")
    public List<LearningResourceSubject> getLearningResourceSubjects() {
        return learningResourceSubjects;
    }

    public LearningResource setLearningResourceSubjects(List<LearningResourceSubject> learningResourceSubjects) {
        this.learningResourceSubjects = learningResourceSubjects;
        return this;
    }

    @JsonProperty("LearningResourceKeywords")
    public List<LearningResourceKeyword> getLearningResourceKeywords() {
        return LearningResourceKeywords;
    }

    public LearningResource setLearningResourceKeywords(List<LearningResourceKeyword> learningResourceKeywords) {
        LearningResourceKeywords = learningResourceKeywords;
        return this;
    }

    @JsonProperty("lrIntendedEndUserRoles")
    public List<LrIntendedEndUserRole> getLrIntendedEndUserRoles() {
        return lrIntendedEndUserRoles;
    }

    public LearningResource setLrIntendedEndUserRoles(List<LrIntendedEndUserRole> lrIntendedEndUserRoles) {
        this.lrIntendedEndUserRoles = lrIntendedEndUserRoles;
        return this;
    }

    @JsonProperty("lrEduObjectives")
    public List<LrEduObjective> getLrEduObjectives() {
        return lrEduObjectives;
    }

    public LearningResource setLrEduObjectives(List<LrEduObjective> lrEduObjectives) {
        this.lrEduObjectives = lrEduObjectives;
        return this;
    }

    @JsonProperty("lrContentModelTypeCode")
    public String getLrContentModelTypeCode() {
        return lrContentModelTypeCode;
    }

    public LearningResource setLrContentModelTypeCode(String lrContentModelTypeCode) {
        this.lrContentModelTypeCode = lrContentModelTypeCode;
        return this;
    }

    @JsonProperty("refLanguageCode")
    public String getRefLanguageCode() {
        return refLanguageCode;
    }

    public LearningResource setRefLanguageCode(String refLanguageCode) {
        this.refLanguageCode = refLanguageCode;
        return this;
    }

    @JsonProperty("parentLearningResourceId")
    public String getParentLearningResourceId() {
        return parentLearningResourceId;
    }

    public LearningResource setParentLearningResourceId(String parentLearningResourceId) {
        this.parentLearningResourceId = parentLearningResourceId;
        return this;
    }

    @JsonProperty("asPublishedPriorActionMessageTypeCode")
    public String getAsPublishedPriorActionMessageTypeCode() {
        return asPublishedPriorActionMessageTypeCode;
    }

    public LearningResource setAsPublishedPriorActionMessageTypeCode(String asPublishedPriorActionMessageTypeCode) {
        this.asPublishedPriorActionMessageTypeCode = asPublishedPriorActionMessageTypeCode;
        return this;
    }

    @JsonProperty("asPublishedPriorActionIdentifier")
    public String getAsPublishedPriorActionIdentifier() {
        return asPublishedPriorActionIdentifier;
    }

    public LearningResource setAsPublishedPriorActionIdentifier(String asPublishedPriorActionIdentifier) {
        this.asPublishedPriorActionIdentifier = asPublishedPriorActionIdentifier;
        return this;
    }

    @JsonProperty("asPublishedPriorActionIdentifierValue")
    public String getAsPublishedPriorActionIdentifierValue() {
        return asPublishedPriorActionIdentifierValue;
    }

    public LearningResource setAsPublishedPriorActionIdentifierValue(String asPublishedPriorActionIdentifierValue) {
        this.asPublishedPriorActionIdentifierValue = asPublishedPriorActionIdentifierValue;
        return this;
    }

    @JsonProperty("asPublishedNextActionMessageTypeCode")
    public String getAsPublishedNextActionMessageTypeCode() {
        return asPublishedNextActionMessageTypeCode;
    }

    public LearningResource setAsPublishedNextActionMessageTypeCode(String asPublishedNextActionMessageTypeCode) {
        this.asPublishedNextActionMessageTypeCode = asPublishedNextActionMessageTypeCode;
        return this;
    }

    @JsonProperty("asPublishedNextActionIdentifier")
    public String getAsPublishedNextActionIdentifier() {
        return asPublishedNextActionIdentifier;
    }

    public LearningResource setAsPublishedNextActionIdentifier(String asPublishedNextActionIdentifier) {
        this.asPublishedNextActionIdentifier = asPublishedNextActionIdentifier;
        return this;
    }

    @JsonProperty("asPublishedNextActionIdentifierValue")
    public String getAsPublishedNextActionIdentifierValue() {
        return asPublishedNextActionIdentifierValue;
    }

    public LearningResource setAsPublishedNextActionIdentifierValue(String asPublishedNextActionIdentifierValue) {
        this.asPublishedNextActionIdentifierValue = asPublishedNextActionIdentifierValue;
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        LearningResource that = (LearningResource) o;
        return Objects.equals(messageTypeCode, that.messageTypeCode) &&
                Objects.equals(originatingSystemCode, that.originatingSystemCode) &&
                Objects.equals(namespaceCode, that.namespaceCode) &&
                Objects.equals(messageVersion, that.messageVersion) &&
                Objects.equals(environmentCode, that.environmentCode) &&
                Objects.equals(transactionTypeCode, that.transactionTypeCode) &&
                Objects.equals(transactionDt, that.transactionDt) &&
                Objects.equals(messageId, that.messageId) &&
                Objects.equals(learningResourceId, that.learningResourceId) &&
                Objects.equals(learningResourceVersionId, that.learningResourceVersionId) &&
                Objects.equals(learningResourceIdType, that.learningResourceIdType) &&
                Objects.equals(title, that.title) &&
                Objects.equals(version, that.version) &&
                Objects.equals(learningResourceTypeCode, that.learningResourceTypeCode) &&
                Objects.equals(timeCategorizationCodeLearning, that.timeCategorizationCodeLearning) &&
                Objects.equals(lrCreationStatusCode, that.lrCreationStatusCode) &&
                Objects.equals(lrMediaTypeCode, that.lrMediaTypeCode) &&
                Objects.equals(description, that.description) &&
                Objects.equals(deliveryPlatformCode, that.deliveryPlatformCode) &&
                Objects.equals(learningResourceComponents, that.learningResourceComponents) &&
                Objects.equals(learningResourceDateTimes, that.learningResourceDateTimes) &&
                Objects.equals(learningResourceContributors, that.learningResourceContributors) &&
                Objects.equals(learningResourceOrganizations, that.learningResourceOrganizations) &&
                Objects.equals(learningResourceIdentifiers, that.learningResourceIdentifiers) &&
                Objects.equals(learningResourceDisciplines, that.learningResourceDisciplines) &&
                Objects.equals(learningResourceSubjects, that.learningResourceSubjects) &&
                Objects.equals(LearningResourceKeywords, that.LearningResourceKeywords) &&
                Objects.equals(lrIntendedEndUserRoles, that.lrIntendedEndUserRoles) &&
                Objects.equals(lrEduObjectives, that.lrEduObjectives) &&
                Objects.equals(lrContentModelTypeCode, that.lrContentModelTypeCode) &&
                Objects.equals(refLanguageCode, that.refLanguageCode) &&
                Objects.equals(parentLearningResourceId, that.parentLearningResourceId) &&
                Objects.equals(asPublishedPriorActionMessageTypeCode, that.asPublishedPriorActionMessageTypeCode) &&
                Objects.equals(asPublishedPriorActionIdentifier, that.asPublishedPriorActionIdentifier) &&
                Objects.equals(asPublishedPriorActionIdentifierValue, that.asPublishedPriorActionIdentifierValue) &&
                Objects.equals(asPublishedNextActionMessageTypeCode, that.asPublishedNextActionMessageTypeCode) &&
                Objects.equals(asPublishedNextActionIdentifier, that.asPublishedNextActionIdentifier) &&
                Objects.equals(asPublishedNextActionIdentifierValue, that.asPublishedNextActionIdentifierValue);
    }

    @Override
    public int hashCode() {
        return Objects.hash(messageTypeCode, originatingSystemCode, namespaceCode, messageVersion, environmentCode, transactionTypeCode, transactionDt, messageId, learningResourceId, learningResourceVersionId, learningResourceIdType, title, version, learningResourceTypeCode, timeCategorizationCodeLearning, lrCreationStatusCode, lrMediaTypeCode, description, deliveryPlatformCode, learningResourceComponents, learningResourceDateTimes, learningResourceContributors, learningResourceOrganizations, learningResourceIdentifiers, learningResourceDisciplines, learningResourceSubjects, LearningResourceKeywords, lrIntendedEndUserRoles, lrEduObjectives, lrContentModelTypeCode, refLanguageCode, parentLearningResourceId, asPublishedPriorActionMessageTypeCode, asPublishedPriorActionIdentifier, asPublishedPriorActionIdentifierValue, asPublishedNextActionMessageTypeCode, asPublishedNextActionIdentifier, asPublishedNextActionIdentifierValue);
    }

    @Override
    public String toString() {
        return Json.stringify(this);
    }
}
