package com.smartsparrow.learner.data;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.smartsparrow.util.Json;
import org.apache.commons.collections4.map.HashedMap;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;


/**
 * This class should accommodate the ItemGrade schema defined in the MX Grade Sync API doc:
 *   https://one-confluence.pearson.com/pages/viewpage.action?spaceKey=M&title=MX+Generic+Grade+Sync+API
 */

@JsonInclude(JsonInclude.Include.NON_NULL)
public class GradePassbackItem {
    private Integer itemId;
    private GradePassbackProgressType itemProgress;
    private Integer itemProgressPercentage;
    private Long progressDateTime; // epoch time in millis
    private Float score;
    private List<Map<String, String>> extendedGradeProperties = new ArrayList<>();
    private String callerCode;
    private String itemURN;
    private List<GradePassbackItem> subItems = new ArrayList<>(); // for questions with sub-questions, not for other questions in a scenario


    public Integer getItemId() {
        return itemId;
    }

    public GradePassbackItem setItemId(Integer itemId) {
        this.itemId = itemId;
        return this;
    }

    public GradePassbackProgressType getItemProgress() {
        return itemProgress;
    }

    public GradePassbackItem setItemProgress(GradePassbackProgressType itemProgress) {
        this.itemProgress = itemProgress;
        return this;
    }

    public Integer getItemProgressPercentage() {
        return itemProgressPercentage;
    }

    public GradePassbackItem setItemProgressPercentage(Integer itemProgressPercentage) {
        this.itemProgressPercentage = itemProgressPercentage;
        return this;
    }

    public Long getProgressDateTime() {
        return progressDateTime;
    }

    public GradePassbackItem setProgressDateTime(Long progressDateTime) {
        this.progressDateTime = progressDateTime;
        return this;
    }

    public Float getScore() {
        return score;
    }

    public GradePassbackItem setScore(Float score) {
        this.score = score;
        return this;
    }

    public List<Map<String, String>> getExtendedGradeProperties() {
        return extendedGradeProperties;
    }

    public GradePassbackItem setExtendedGradeProperties(final List<Map<String, String>> extendedGradeProperties) {
        this.extendedGradeProperties = extendedGradeProperties;
        return this;
    }

    public String getCallerCode() {
        return callerCode;
    }

    public GradePassbackItem setCallerCode(String callerCode) {
        this.callerCode = callerCode;
        return this;
    }

    public String getItemURN() {
        return itemURN;
    }

    public GradePassbackItem setItemURN(String itemURN) {
        this.itemURN = itemURN;
        return this;
    }

    public List<GradePassbackItem> getSubItems() {
        return subItems;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        GradePassbackItem that = (GradePassbackItem) o;
        return Objects.equals(itemId, that.itemId) &&
                Objects.equals(itemProgress, that.itemProgress) &&
                Objects.equals(itemProgressPercentage, that.itemProgressPercentage) &&
                Objects.equals(progressDateTime, that.progressDateTime) &&
                Objects.equals(score, that.score) &&
                Objects.equals(extendedGradeProperties, that.extendedGradeProperties) &&
                Objects.equals(callerCode, that.callerCode) &&
                Objects.equals(itemURN, that.itemURN) &&
                Objects.equals(subItems, that.subItems);
    }

    @Override
    public int hashCode() {
        return Objects.hash(itemId, itemProgress, itemProgressPercentage, progressDateTime,
                score, extendedGradeProperties, callerCode, itemURN, subItems);
    }

    @Override
    public String toString() {
        return Json.stringify(this);
    }
}
