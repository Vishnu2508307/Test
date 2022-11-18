package com.smartsparrow.la.mapper.pla.data;

import java.util.List;
import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.smartsparrow.util.Json;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class AssessmentItemMaster extends BronteMessageEnvelope {
    private static final String MESSAGE_TYPE_CODE = "AssessmentItemMaster";
    private static final String MESSAGE_VERSION = "1.6.0";
    private static final String APP_ID = "Bronte";
    private String messageTypeCode;
    private String originatingSystemCode;
    private String namespaceCode;
    private String messageVersion;
    private String environmentCode;
    private String transactionTypeCode;
    private String transactionDt;
    private String messageId;
    private String assessmentItemId;
    private String assessmentItemIdType;
    private String learningResourceId;
    private String learningResourceIdType;
    private String appId;
    private String itemTitleText;
    private String itemBodyText;
    private String itemBodyEquationTemplate;
    private String itemActivityType;
    private String assessmentItemType;
    private List<ItemEduObjective> itemEduObjectives;
    private List<ItemUrl> itemUrls;
    private List<ItemLearningAid> itemLearningAids;
    private List<String> itemHeaderDispOptions;
    private List<String> itemProblemStemDispOptions;
    private Integer itemPartCount;
    private Double itemPossiblePoints;
    private List<ItemPartDefinition> itemPartDefinitions;
    private ItemAlgorithmicAnswerInputs itemAlgorithmicAnswerInputs;
    private String languageCode;
    private String effectiveDt;
    private String creationDt;
    private String createByPearsonId;

    @Override
    @JsonProperty("messageTypeCode")
    String getMessageTypeCode() {
        return MESSAGE_TYPE_CODE;
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

    @Override
    @JsonProperty("originatingSystemCode")
    public String getOriginatingSystemCode() {
        return super.getOriginatingSystemCode();
    }

    public AssessmentItemMaster setOriginatingSystemCode(String originatingSystemCode) {
        this.originatingSystemCode = originatingSystemCode;
        return this;
    }

    @Override
    @JsonProperty("namespaceCode")
    public String getNamespaceCode() {
        return super.getNamespaceCode();
    }

    public AssessmentItemMaster setMessageTypeCode(String messageTypeCode) {
        this.messageTypeCode = messageTypeCode;
        return this;
    }

    public AssessmentItemMaster setNamespaceCode(String namespaceCode) {
        this.namespaceCode = namespaceCode;
        return this;
    }

    public AssessmentItemMaster setMessageVersion(String messageVersion) {
        this.messageVersion = messageVersion;
        return this;
    }

    public AssessmentItemMaster setEnvironmentCode(String environmentCode) {
        this.environmentCode = environmentCode;
        return this;
    }

    public AssessmentItemMaster setTransactionTypeCode(String transactionTypeCode) {
        this.transactionTypeCode = transactionTypeCode;
        return this;
    }

    public AssessmentItemMaster setTransactionDt(String transactionDt) {
        this.transactionDt = transactionDt;
        return this;
    }

    public AssessmentItemMaster setMessageId(String messageId) {
        this.messageId = messageId;
        return this;
    }

    @JsonProperty("assessmentItemId")
    public String getAssessmentItemId() {
        return assessmentItemId;
    }

    public AssessmentItemMaster setAssessmentItemId(String assessmentItemId) {
        this.assessmentItemId = assessmentItemId;
        return this;
    }

    @JsonProperty("assessmentItemIdType")
    public String getAssessmentItemIdType() {
        return assessmentItemIdType;
    }

    public AssessmentItemMaster setAssessmentItemIdType(String assessmentItemIdType) {
        this.assessmentItemIdType = assessmentItemIdType;
        return this;
    }

    @JsonProperty("learningResourceId")
    public String getLearningResourceId() {
        return learningResourceId;
    }

    public AssessmentItemMaster setLearningResourceId(String learningResourceId) {
        this.learningResourceId = learningResourceId;
        return this;
    }

    @JsonProperty("learningResourceIdType")
    public String getLearningResourceIdType() {
        return learningResourceIdType;
    }

    public AssessmentItemMaster setLearningResourceIdType(String learningResourceIdType) {
        this.learningResourceIdType = learningResourceIdType;
        return this;
    }

    @JsonProperty("appId")
    public String getAppId() {
        return APP_ID;
    }

    public AssessmentItemMaster setAppId(String appId) {
        this.appId = appId;
        return this;
    }

    @JsonProperty("itemTitleText")
    public String getItemTitleText() {
        return itemTitleText;
    }

    public AssessmentItemMaster setItemTitleText(String itemTitleText) {
        this.itemTitleText = itemTitleText;
        return this;
    }

    @JsonProperty("itemBodyText")
    public String getItemBodyText() {
        return itemBodyText;
    }

    public AssessmentItemMaster setItemBodyText(String itemBodyText) {
        this.itemBodyText = itemBodyText;
        return this;
    }

    @JsonProperty("itemBodyEquationTemplate")
    public String getItemBodyEquationTemplate() {
        return itemBodyEquationTemplate;
    }

    public AssessmentItemMaster setItemBodyEquationTemplate(String itemBodyEquationTemplate) {
        this.itemBodyEquationTemplate = itemBodyEquationTemplate;
        return this;
    }

    @JsonProperty("itemActivityType")
    public String getItemActivityType() {
        return itemActivityType;
    }

    public AssessmentItemMaster setItemActivityType(String itemActivityType) {
        this.itemActivityType = itemActivityType;
        return this;
    }

    @JsonProperty("assessmentItemType")
    public String getAssessmentItemType() {
        return assessmentItemType;
    }

    public AssessmentItemMaster setAssessmentItemType(String assessmentItemType) {
        this.assessmentItemType = assessmentItemType;
        return this;
    }

    @JsonProperty("itemEduObjectives")
    public List<ItemEduObjective> getItemEduObjectives() {
        return itemEduObjectives;
    }

    public AssessmentItemMaster setItemEduObjectives(List<ItemEduObjective> itemEduObjectives) {
        this.itemEduObjectives = itemEduObjectives;
        return this;
    }

    @JsonProperty("itemUrls")
    public List<ItemUrl> getItemUrls() {
        return itemUrls;
    }

    public AssessmentItemMaster setItemUrls(List<ItemUrl> itemUrls) {
        this.itemUrls = itemUrls;
        return this;
    }

    @JsonProperty("itemLearningAids")
    public List<ItemLearningAid> getItemLearningAids() {
        return itemLearningAids;
    }

    public AssessmentItemMaster setItemLearningAids(List<ItemLearningAid> itemLearningAids) {
        this.itemLearningAids = itemLearningAids;
        return this;
    }

    @JsonProperty("itemHeaderDispOptions")
    public List<String> getItemHeaderDispOptions() {
        return itemHeaderDispOptions;
    }

    public AssessmentItemMaster setItemHeaderDispOptions(List<String> itemHeaderDispOptions) {
        this.itemHeaderDispOptions = itemHeaderDispOptions;
        return this;
    }

    @JsonProperty("itemProblemStemDispOptions")
    public List<String> getItemProblemStemDispOptions() {
        return itemProblemStemDispOptions;
    }

    public AssessmentItemMaster setItemProblemStemDispOptions(List<String> itemProblemStemDispOptions) {
        this.itemProblemStemDispOptions = itemProblemStemDispOptions;
        return this;
    }

    @JsonProperty("itemPartCount")
    public Integer getItemPartCount() {
        return itemPartCount;
    }

    public AssessmentItemMaster setItemPartCount(Integer itemPartCount) {
        this.itemPartCount = itemPartCount;
        return this;
    }

    @JsonProperty("itemPossiblePoints")
    public Double getItemPossiblePoints() {
        return itemPossiblePoints;
    }

    public AssessmentItemMaster setItemPossiblePoints(Double itemPossiblePoints) {
        this.itemPossiblePoints = itemPossiblePoints;
        return this;
    }

    @JsonProperty("itemPartDefinitions")
    public List<ItemPartDefinition> getItemPartDefinitions() {
        return itemPartDefinitions;
    }

    public AssessmentItemMaster setItemPartDefinitions(List<ItemPartDefinition> itemPartDefinitions) {
        this.itemPartDefinitions = itemPartDefinitions;
        return this;
    }

    @JsonProperty("itemAlgorithmicAnswerInputs")
    public ItemAlgorithmicAnswerInputs getItemAlgorithmicAnswerInputs() {
        return itemAlgorithmicAnswerInputs;
    }

    public AssessmentItemMaster setItemAlgorithmicAnswerInputs(ItemAlgorithmicAnswerInputs itemAlgorithmicAnswerInputs) {
        this.itemAlgorithmicAnswerInputs = itemAlgorithmicAnswerInputs;
        return this;
    }

    @JsonProperty("languageCode")
    public String getLanguageCode() {
        return languageCode;
    }

    public AssessmentItemMaster setLanguageCode(String languageCode) {
        this.languageCode = languageCode;
        return this;
    }

    @JsonProperty("effectiveDt")
    public String getEffectiveDt() {
        return effectiveDt;
    }

    public AssessmentItemMaster setEffectiveDt(String effectiveDt) {
        this.effectiveDt = effectiveDt;
        return this;
    }

    @JsonProperty("creationDt")
    public String getCreationDt() {
        return creationDt;
    }

    public AssessmentItemMaster setCreationDt(String creationDt) {
        this.creationDt = creationDt;
        return this;
    }

    @JsonProperty("createByPearsonId")
    public String getCreateByPearsonId() {
        return createByPearsonId;
    }

    public AssessmentItemMaster setCreateByPearsonId(String createByPearsonId) {
        this.createByPearsonId = createByPearsonId;
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AssessmentItemMaster that = (AssessmentItemMaster) o;
        return Objects.equals(messageTypeCode, that.messageTypeCode) &&
                Objects.equals(originatingSystemCode, that.originatingSystemCode) &&
                Objects.equals(namespaceCode, that.namespaceCode) &&
                Objects.equals(messageVersion, that.messageVersion) &&
                Objects.equals(environmentCode, that.environmentCode) &&
                Objects.equals(transactionTypeCode, that.transactionTypeCode) &&
                Objects.equals(transactionDt, that.transactionDt) &&
                Objects.equals(messageId, that.messageId) &&
                Objects.equals(assessmentItemId, that.assessmentItemId) &&
                Objects.equals(assessmentItemIdType, that.assessmentItemIdType) &&
                Objects.equals(learningResourceId, that.learningResourceId) &&
                Objects.equals(learningResourceIdType, that.learningResourceIdType) &&
                Objects.equals(appId, that.appId) &&
                Objects.equals(itemTitleText, that.itemTitleText) &&
                Objects.equals(itemBodyText, that.itemBodyText) &&
                Objects.equals(itemBodyEquationTemplate, that.itemBodyEquationTemplate) &&
                Objects.equals(itemActivityType, that.itemActivityType) &&
                Objects.equals(assessmentItemType, that.assessmentItemType) &&
                Objects.equals(itemEduObjectives, that.itemEduObjectives) &&
                Objects.equals(itemUrls, that.itemUrls) &&
                Objects.equals(itemLearningAids, that.itemLearningAids) &&
                Objects.equals(itemHeaderDispOptions, that.itemHeaderDispOptions) &&
                Objects.equals(itemProblemStemDispOptions, that.itemProblemStemDispOptions) &&
                Objects.equals(itemPartCount, that.itemPartCount) &&
                Objects.equals(itemPossiblePoints, that.itemPossiblePoints) &&
                Objects.equals(itemPartDefinitions, that.itemPartDefinitions) &&
                Objects.equals(itemAlgorithmicAnswerInputs, that.itemAlgorithmicAnswerInputs) &&
                Objects.equals(languageCode, that.languageCode) &&
                Objects.equals(effectiveDt, that.effectiveDt) &&
                Objects.equals(creationDt, that.creationDt) &&
                Objects.equals(createByPearsonId, that.createByPearsonId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(messageTypeCode, originatingSystemCode, namespaceCode, messageVersion, environmentCode, transactionTypeCode, transactionDt, messageId, assessmentItemId, assessmentItemIdType, learningResourceId, learningResourceIdType, appId, itemTitleText, itemBodyText, itemBodyEquationTemplate, itemActivityType, assessmentItemType, itemEduObjectives, itemUrls, itemLearningAids, itemHeaderDispOptions, itemProblemStemDispOptions, itemPartCount, itemPossiblePoints, itemPartDefinitions, itemAlgorithmicAnswerInputs, languageCode, effectiveDt, creationDt, createByPearsonId);
    }

    @Override
    public String toString() {
        return Json.stringify(this);
    }
}
