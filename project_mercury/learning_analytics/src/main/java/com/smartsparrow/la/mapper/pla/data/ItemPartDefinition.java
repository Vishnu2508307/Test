package com.smartsparrow.la.mapper.pla.data;

import java.util.List;
import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.smartsparrow.util.Json;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class ItemPartDefinition {
    private Integer itemPartNumber;
    private String itemPartId;
    private Integer itemPartSequenceNum;
    private String itemPartActivityText;
    private String itemPartEquationTemplate;
    private String itemPartScoringRuleCode;
    private Double itemPartPossiblePoints;
    private Integer itemPartMaxTriesAllowed;
    private String itemPartPresentFmtCode;
    private ItemPartAnswerChoiceDisplay itemPartAnswerChoiceDisplay;
    private String itemPartShuffleMovableAnsCd;
    private String itemPartIntsShareAnswersCd;
    private List<ItemPartAnswer> itemPartAnswers;
    private List<ItemPartPopup> itemPartPopups;
    private List<ItemPartEduObjective> itemPartEduObjectives;
    private List<ItemPartUrl> itemPartUrls;
    private List<ItemPartLearningAid> itemPartLearningAids;
    private List<ItemPartAnswerFeedback> itemPartAnswerFeedbacks;
    private List<ItemPartInteraction> itemPartInteractions;
    private List<ItemPartScenario> itemPartScenarios;
    private String languageCode;
    private String effectiveDt;
    private String creationDt;
    private String createdByPearsonId;

    @JsonProperty("itemPartNumber")
    public Integer getItemPartNumber() {
        return itemPartNumber;
    }

    public ItemPartDefinition setItemPartNumber(Integer itemPartNumber) {
        this.itemPartNumber = itemPartNumber;
        return this;
    }

    @JsonProperty("itemPartId")
    public String getItemPartId() {
        return itemPartId;
    }

    public ItemPartDefinition setItemPartId(String itemPartId) {
        this.itemPartId = itemPartId;
        return this;
    }

    @JsonProperty("itemPartSequenceNum")
    public Integer getItemPartSequenceNum() {
        return itemPartSequenceNum;
    }

    public ItemPartDefinition setItemPartSequenceNum(Integer itemPartSequenceNum) {
        this.itemPartSequenceNum = itemPartSequenceNum;
        return this;
    }

    @JsonProperty("itemPartActivityText")
    public String getItemPartActivityText() {
        return itemPartActivityText;
    }

    public ItemPartDefinition setItemPartActivityText(String itemPartActivityText) {
        this.itemPartActivityText = itemPartActivityText;
        return this;
    }

    @JsonProperty("itemPartEquationTemplate")
    public String getItemPartEquationTemplate() {
        return itemPartEquationTemplate;
    }

    public ItemPartDefinition setItemPartEquationTemplate(String itemPartEquationTemplate) {
        this.itemPartEquationTemplate = itemPartEquationTemplate;
        return this;
    }

    @JsonProperty("itemPartScoringRuleCode")
    public String getItemPartScoringRuleCode() {
        return itemPartScoringRuleCode;
    }

    public ItemPartDefinition setItemPartScoringRuleCode(String itemPartScoringRuleCode) {
        this.itemPartScoringRuleCode = itemPartScoringRuleCode;
        return this;
    }

    @JsonProperty("itemPartPossiblePoints")
    public Double getItemPartPossiblePoints() {
        return itemPartPossiblePoints;
    }

    public ItemPartDefinition setItemPartPossiblePoints(Double itemPartPossiblePoints) {
        this.itemPartPossiblePoints = itemPartPossiblePoints;
        return this;
    }

    @JsonProperty("itemPartMaxTriesAllowed")
    public Integer getItemPartMaxTriesAllowed() {
        return itemPartMaxTriesAllowed;
    }

    public ItemPartDefinition setItemPartMaxTriesAllowed(Integer itemPartMaxTriesAllowed) {
        this.itemPartMaxTriesAllowed = itemPartMaxTriesAllowed;
        return this;
    }

    @JsonProperty("itemPartPresentFmtCode")
    public String getItemPartPresentFmtCode() {
        return itemPartPresentFmtCode;
    }

    public ItemPartDefinition setItemPartPresentFmtCode(String itemPartPresentFmtCode) {
        this.itemPartPresentFmtCode = itemPartPresentFmtCode;
        return this;
    }

    @JsonProperty("itemPartAnswerChoiceDisplay")
    public ItemPartAnswerChoiceDisplay getItemPartAnswerChoiceDisplay() {
        return itemPartAnswerChoiceDisplay;
    }

    public ItemPartDefinition setItemPartAnswerChoiceDisplay(ItemPartAnswerChoiceDisplay itemPartAnswerChoiceDisplay) {
        this.itemPartAnswerChoiceDisplay = itemPartAnswerChoiceDisplay;
        return this;
    }

    @JsonProperty("itemPartShuffleMovableAnsCd")
    public String getItemPartShuffleMovableAnsCd() {
        return itemPartShuffleMovableAnsCd;
    }

    public ItemPartDefinition setItemPartShuffleMovableAnsCd(String itemPartShuffleMovableAnsCd) {
        this.itemPartShuffleMovableAnsCd = itemPartShuffleMovableAnsCd;
        return this;
    }

    @JsonProperty("itemPartIntsShareAnswersCd")
    public String getItemPartIntsShareAnswersCd() {
        return itemPartIntsShareAnswersCd;
    }

    public ItemPartDefinition setItemPartIntsShareAnswersCd(String itemPartIntsShareAnswersCd) {
        this.itemPartIntsShareAnswersCd = itemPartIntsShareAnswersCd;
        return this;
    }

    @JsonProperty("itemPartAnswers")
    public List<ItemPartAnswer> getItemPartAnswers() {
        return itemPartAnswers;
    }

    public ItemPartDefinition setItemPartAnswers(List<ItemPartAnswer> itemPartAnswers) {
        this.itemPartAnswers = itemPartAnswers;
        return this;
    }

    @JsonProperty("itemPartPopups")
    public List<ItemPartPopup> getItemPartPopups() {
        return itemPartPopups;
    }

    public ItemPartDefinition setItemPartPopups(List<ItemPartPopup> itemPartPopups) {
        this.itemPartPopups = itemPartPopups;
        return this;
    }

    @JsonProperty("itemPartEduObjectives")
    public List<ItemPartEduObjective> getItemPartEduObjectives() {
        return itemPartEduObjectives;
    }

    public ItemPartDefinition setItemPartEduObjectives(List<ItemPartEduObjective> itemPartEduObjectives) {
        this.itemPartEduObjectives = itemPartEduObjectives;
        return this;
    }

    @JsonProperty("itemPartUrls")
    public List<ItemPartUrl> getItemPartUrls() {
        return itemPartUrls;
    }

    public ItemPartDefinition setItemPartUrls(List<ItemPartUrl> itemPartUrls) {
        this.itemPartUrls = itemPartUrls;
        return this;
    }

    @JsonProperty("itemPartLearningAids")
    public List<ItemPartLearningAid> getItemPartLearningAids() {
        return itemPartLearningAids;
    }

    public ItemPartDefinition setItemPartLearningAids(List<ItemPartLearningAid> itemPartLearningAids) {
        this.itemPartLearningAids = itemPartLearningAids;
        return this;
    }

    @JsonProperty("itemPartAnswerFeedbacks")
    public List<ItemPartAnswerFeedback> getItemPartAnswerFeedbacks() {
        return itemPartAnswerFeedbacks;
    }

    public ItemPartDefinition setItemPartAnswerFeedbacks(List<ItemPartAnswerFeedback> itemPartAnswerFeedbacks) {
        this.itemPartAnswerFeedbacks = itemPartAnswerFeedbacks;
        return this;
    }

    @JsonProperty("itemPartInteractions")
    public List<ItemPartInteraction> getItemPartInteractions() {
        return itemPartInteractions;
    }

    public ItemPartDefinition setItemPartInteractions(List<ItemPartInteraction> itemPartInteractions) {
        this.itemPartInteractions = itemPartInteractions;
        return this;
    }

    @JsonProperty("itemPartScenarios")
    public List<ItemPartScenario> getItemPartScenarios() {
        return itemPartScenarios;
    }

    public ItemPartDefinition setItemPartScenarios(List<ItemPartScenario> itemPartScenarios) {
        this.itemPartScenarios = itemPartScenarios;
        return this;
    }

    @JsonProperty("languageCode")
    public String getLanguageCode() {
        return languageCode;
    }

    public ItemPartDefinition setLanguageCode(String languageCode) {
        this.languageCode = languageCode;
        return this;
    }

    @JsonProperty("effectiveDt")
    public String getEffectiveDt() {
        return effectiveDt;
    }

    public ItemPartDefinition setEffectiveDt(String effectiveDt) {
        this.effectiveDt = effectiveDt;
        return this;
    }

    @JsonProperty("creationDt")
    public String getCreationDt() {
        return creationDt;
    }

    public ItemPartDefinition setCreationDt(String creationDt) {
        this.creationDt = creationDt;
        return this;
    }

    @JsonProperty("createdByPearsonId")
    public String getCreatedByPearsonId() {
        return createdByPearsonId;
    }

    public ItemPartDefinition setCreatedByPearsonId(String createdByPearsonId) {
        this.createdByPearsonId = createdByPearsonId;
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ItemPartDefinition that = (ItemPartDefinition) o;
        return Objects.equals(itemPartNumber, that.itemPartNumber) &&
                Objects.equals(itemPartId, that.itemPartId) &&
                Objects.equals(itemPartSequenceNum, that.itemPartSequenceNum) &&
                Objects.equals(itemPartActivityText, that.itemPartActivityText) &&
                Objects.equals(itemPartEquationTemplate, that.itemPartEquationTemplate) &&
                Objects.equals(itemPartScoringRuleCode, that.itemPartScoringRuleCode) &&
                Objects.equals(itemPartPossiblePoints, that.itemPartPossiblePoints) &&
                Objects.equals(itemPartMaxTriesAllowed, that.itemPartMaxTriesAllowed) &&
                Objects.equals(itemPartPresentFmtCode, that.itemPartPresentFmtCode) &&
                Objects.equals(itemPartAnswerChoiceDisplay, that.itemPartAnswerChoiceDisplay) &&
                Objects.equals(itemPartShuffleMovableAnsCd, that.itemPartShuffleMovableAnsCd) &&
                Objects.equals(itemPartIntsShareAnswersCd, that.itemPartIntsShareAnswersCd) &&
                Objects.equals(itemPartAnswers, that.itemPartAnswers) &&
                Objects.equals(itemPartPopups, that.itemPartPopups) &&
                Objects.equals(itemPartEduObjectives, that.itemPartEduObjectives) &&
                Objects.equals(itemPartUrls, that.itemPartUrls) &&
                Objects.equals(itemPartLearningAids, that.itemPartLearningAids) &&
                Objects.equals(itemPartAnswerFeedbacks, that.itemPartAnswerFeedbacks) &&
                Objects.equals(itemPartInteractions, that.itemPartInteractions) &&
                Objects.equals(itemPartScenarios, that.itemPartScenarios) &&
                Objects.equals(languageCode, that.languageCode) &&
                Objects.equals(effectiveDt, that.effectiveDt) &&
                Objects.equals(creationDt, that.creationDt) &&
                Objects.equals(createdByPearsonId, that.createdByPearsonId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(itemPartNumber, itemPartId, itemPartSequenceNum, itemPartActivityText, itemPartEquationTemplate, itemPartScoringRuleCode, itemPartPossiblePoints, itemPartMaxTriesAllowed, itemPartPresentFmtCode, itemPartAnswerChoiceDisplay, itemPartShuffleMovableAnsCd, itemPartIntsShareAnswersCd, itemPartAnswers, itemPartPopups, itemPartEduObjectives, itemPartUrls, itemPartLearningAids, itemPartAnswerFeedbacks, itemPartInteractions, itemPartScenarios, languageCode, effectiveDt, creationDt, createdByPearsonId);
    }

    @Override
    public String toString() {
        return Json.stringify(this);
    }
}
