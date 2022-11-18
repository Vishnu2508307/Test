package com.smartsparrow.la.mapper.pla.data;

import java.util.List;
import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.smartsparrow.util.Json;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class LearningResourceKeyword {

    private List<String> keywords;
    private String refLanguageCode;
    private String keyword;

    @JsonProperty("keywords")
    public List<String> getKeywords() {
        return keywords;
    }

    public LearningResourceKeyword setKeywords(List<String> keywords) {
        this.keywords = keywords;
        return this;
    }

    @JsonProperty("refLanguageCode")
    public String getRefLanguageCode() {
        return refLanguageCode;
    }

    public LearningResourceKeyword setRefLanguageCode(String refLanguageCode) {
        this.refLanguageCode = refLanguageCode;
        return this;
    }

    @JsonProperty("keyword")
    public String getKeyword() {
        return keyword;
    }

    public LearningResourceKeyword setKeyword(String keyword) {
        this.keyword = keyword;
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        LearningResourceKeyword that = (LearningResourceKeyword) o;
        return Objects.equals(keywords, that.keywords) &&
                Objects.equals(refLanguageCode, that.refLanguageCode) &&
                Objects.equals(keyword, that.keyword);
    }

    @Override
    public int hashCode() {
        return Objects.hash(keywords, refLanguageCode, keyword);
    }

    @Override
    public String toString() {
        return Json.stringify(this);
    }
}
