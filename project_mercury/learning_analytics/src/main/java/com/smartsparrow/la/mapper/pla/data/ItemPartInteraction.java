package com.smartsparrow.la.mapper.pla.data;

import java.util.List;
import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.smartsparrow.util.Json;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class ItemPartInteraction {
    private Integer itemPartIntNumber;
    private String itemPartInteractionId;
    private Integer itemPartIntSequenceNum;
    private String itemPartIntName;
    private String itemPartIntActivityType;
    private String itemPartIntPresentFmtCode;
    private String itemPartIntActivityText;
    private String itemPartIntQuestionFmtType;
    private String itemPartIntAnswerFmtType;
    private Double itemPartPossiblePoints;
    private Integer itemPartIntMaxTriesAllowed;
    private Integer itemPartIntMaxChoicesNum;
    private String itemPartIntShuffleMovableAnsCd;
    private String itemPartIntCanonicalAnswer;
    private String itemPartIntCorrectResp;
    private List<ItemPartIntAnswer> itemPartIntAnswers;
    private List<ItemPartIntCorrectRespnse> itemPartIntCorrectRespnses;

    @JsonProperty("itemPartIntNumber")
    public Integer getItemPartIntNumber() {
        return itemPartIntNumber;
    }

    public ItemPartInteraction setItemPartIntNumber(Integer itemPartIntNumber) {
        this.itemPartIntNumber = itemPartIntNumber;
        return this;
    }

    @JsonProperty("itemPartInteractionId")
    public String getItemPartInteractionId() {
        return itemPartInteractionId;
    }

    public ItemPartInteraction setItemPartInteractionId(String itemPartInteractionId) {
        this.itemPartInteractionId = itemPartInteractionId;
        return this;
    }

    @JsonProperty("itemPartIntSequenceNum")
    public Integer getItemPartIntSequenceNum() {
        return itemPartIntSequenceNum;
    }

    public ItemPartInteraction setItemPartIntSequenceNum(Integer itemPartIntSequenceNum) {
        this.itemPartIntSequenceNum = itemPartIntSequenceNum;
        return this;
    }

    @JsonProperty("itemPartIntName")
    public String getItemPartIntName() {
        return itemPartIntName;
    }

    public ItemPartInteraction setItemPartIntName(String itemPartIntName) {
        this.itemPartIntName = itemPartIntName;
        return this;
    }

    @JsonProperty("itemPartIntActivityType")
    public String getItemPartIntActivityType() {
        return itemPartIntActivityType;
    }

    public ItemPartInteraction setItemPartIntActivityType(String itemPartIntActivityType) {
        this.itemPartIntActivityType = itemPartIntActivityType;
        return this;
    }

    @JsonProperty("itemPartIntPresentFmtCode")
    public String getItemPartIntPresentFmtCode() {
        return itemPartIntPresentFmtCode;
    }

    public ItemPartInteraction setItemPartIntPresentFmtCode(String itemPartIntPresentFmtCode) {
        this.itemPartIntPresentFmtCode = itemPartIntPresentFmtCode;
        return this;
    }

    @JsonProperty("itemPartIntActivityText")
    public String getItemPartIntActivityText() {
        return itemPartIntActivityText;
    }

    public ItemPartInteraction setItemPartIntActivityText(String itemPartIntActivityText) {
        this.itemPartIntActivityText = itemPartIntActivityText;
        return this;
    }

    @JsonProperty("itemPartIntQuestionFmtType")
    public String getItemPartIntQuestionFmtType() {
        return itemPartIntQuestionFmtType;
    }

    public ItemPartInteraction setItemPartIntQuestionFmtType(String itemPartIntQuestionFmtType) {
        this.itemPartIntQuestionFmtType = itemPartIntQuestionFmtType;
        return this;
    }

    @JsonProperty("itemPartIntAnswerFmtType")
    public String getItemPartIntAnswerFmtType() {
        return itemPartIntAnswerFmtType;
    }

    public ItemPartInteraction setItemPartIntAnswerFmtType(String itemPartIntAnswerFmtType) {
        this.itemPartIntAnswerFmtType = itemPartIntAnswerFmtType;
        return this;
    }

    @JsonProperty("itemPartPossiblePoints")
    public Double getItemPartPossiblePoints() {
        return itemPartPossiblePoints;
    }

    public ItemPartInteraction setItemPartPossiblePoints(Double itemPartPossiblePoints) {
        this.itemPartPossiblePoints = itemPartPossiblePoints;
        return this;
    }

    @JsonProperty("itemPartIntMaxTriesAllowed")
    public Integer getItemPartIntMaxTriesAllowed() {
        return itemPartIntMaxTriesAllowed;
    }

    public ItemPartInteraction setItemPartIntMaxTriesAllowed(Integer itemPartIntMaxTriesAllowed) {
        this.itemPartIntMaxTriesAllowed = itemPartIntMaxTriesAllowed;
        return this;
    }

    @JsonProperty("itemPartIntMaxChoicesNum")
    public Integer getItemPartIntMaxChoicesNum() {
        return itemPartIntMaxChoicesNum;
    }

    public ItemPartInteraction setItemPartIntMaxChoicesNum(Integer itemPartIntMaxChoicesNum) {
        this.itemPartIntMaxChoicesNum = itemPartIntMaxChoicesNum;
        return this;
    }

    @JsonProperty("itemPartIntShuffleMovableAnsCd")
    public String getItemPartIntShuffleMovableAnsCd() {
        return itemPartIntShuffleMovableAnsCd;
    }

    public ItemPartInteraction setItemPartIntShuffleMovableAnsCd(String itemPartIntShuffleMovableAnsCd) {
        this.itemPartIntShuffleMovableAnsCd = itemPartIntShuffleMovableAnsCd;
        return this;
    }

    @JsonProperty("itemPartIntCanonicalAnswer")
    public String getItemPartIntCanonicalAnswer() {
        return itemPartIntCanonicalAnswer;
    }

    public ItemPartInteraction setItemPartIntCanonicalAnswer(String itemPartIntCanonicalAnswer) {
        this.itemPartIntCanonicalAnswer = itemPartIntCanonicalAnswer;
        return this;
    }

    @JsonProperty("itemPartIntCorrectResp")
    public String getItemPartIntCorrectResp() {
        return itemPartIntCorrectResp;
    }

    public ItemPartInteraction setItemPartIntCorrectResp(String itemPartIntCorrectResp) {
        this.itemPartIntCorrectResp = itemPartIntCorrectResp;
        return this;
    }

    @JsonProperty("itemPartIntAnswers")
    public List<ItemPartIntAnswer> getItemPartIntAnswers() {
        return itemPartIntAnswers;
    }

    public ItemPartInteraction setItemPartIntAnswers(List<ItemPartIntAnswer> itemPartIntAnswers) {
        this.itemPartIntAnswers = itemPartIntAnswers;
        return this;
    }

    @JsonProperty("itemPartIntCorrectRespnses")
    public List<ItemPartIntCorrectRespnse> getItemPartIntCorrectRespnses() {
        return itemPartIntCorrectRespnses;
    }

    public ItemPartInteraction setItemPartIntCorrectRespnses(List<ItemPartIntCorrectRespnse> itemPartIntCorrectRespnses) {
        this.itemPartIntCorrectRespnses = itemPartIntCorrectRespnses;
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ItemPartInteraction that = (ItemPartInteraction) o;
        return Objects.equals(itemPartIntNumber, that.itemPartIntNumber) &&
                Objects.equals(itemPartInteractionId, that.itemPartInteractionId) &&
                Objects.equals(itemPartIntSequenceNum, that.itemPartIntSequenceNum) &&
                Objects.equals(itemPartIntName, that.itemPartIntName) &&
                Objects.equals(itemPartIntActivityType, that.itemPartIntActivityType) &&
                Objects.equals(itemPartIntPresentFmtCode, that.itemPartIntPresentFmtCode) &&
                Objects.equals(itemPartIntActivityText, that.itemPartIntActivityText) &&
                Objects.equals(itemPartIntQuestionFmtType, that.itemPartIntQuestionFmtType) &&
                Objects.equals(itemPartIntAnswerFmtType, that.itemPartIntAnswerFmtType) &&
                Objects.equals(itemPartPossiblePoints, that.itemPartPossiblePoints) &&
                Objects.equals(itemPartIntMaxTriesAllowed, that.itemPartIntMaxTriesAllowed) &&
                Objects.equals(itemPartIntMaxChoicesNum, that.itemPartIntMaxChoicesNum) &&
                Objects.equals(itemPartIntShuffleMovableAnsCd, that.itemPartIntShuffleMovableAnsCd) &&
                Objects.equals(itemPartIntCanonicalAnswer, that.itemPartIntCanonicalAnswer) &&
                Objects.equals(itemPartIntCorrectResp, that.itemPartIntCorrectResp) &&
                Objects.equals(itemPartIntAnswers, that.itemPartIntAnswers) &&
                Objects.equals(itemPartIntCorrectRespnses, that.itemPartIntCorrectRespnses);
    }

    @Override
    public int hashCode() {
        return Objects.hash(itemPartIntNumber, itemPartInteractionId, itemPartIntSequenceNum, itemPartIntName, itemPartIntActivityType, itemPartIntPresentFmtCode, itemPartIntActivityText, itemPartIntQuestionFmtType, itemPartIntAnswerFmtType, itemPartPossiblePoints, itemPartIntMaxTriesAllowed, itemPartIntMaxChoicesNum, itemPartIntShuffleMovableAnsCd, itemPartIntCanonicalAnswer, itemPartIntCorrectResp, itemPartIntAnswers, itemPartIntCorrectRespnses);
    }

    @Override
    public String toString() {
        return Json.stringify(this);
    }
}
