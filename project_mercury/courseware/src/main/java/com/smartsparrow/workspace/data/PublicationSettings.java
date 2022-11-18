package com.smartsparrow.workspace.data;

import java.util.Objects;
import com.fasterxml.jackson.annotation.JsonInclude;

import com.smartsparrow.util.Json;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class PublicationSettings {

    private String labId;
    private String title;
    private String description;
    private String discipline;
    private String estimatedTime;
    private String previewUrl;
    private Integer status = 1;

    public String getLabId() {
        return labId;
    }

    public PublicationSettings setLabId(final String labId) {
        this.labId = labId;
        return this;
    }

    public String getTitle() {
        return title;
    }

    public PublicationSettings setTitle(final String title) {
        this.title = title;
        return this;
    }

    public String getDescription() {
        return description;
    }

    public PublicationSettings setDescription(final String description) {
        this.description = description;
        return this;
    }

    public String getDiscipline() {
        return discipline;
    }

    public PublicationSettings setDiscipline(final String discipline) {
        this.discipline = discipline;
        return this;
    }

    public String getEstimatedTime() {
        return estimatedTime;
    }

    public PublicationSettings setEstimatedTime(final String estimatedTime) {
        this.estimatedTime = estimatedTime;
        return this;
    }

    public String getPreviewUrl() {
        return previewUrl;
    }

    public PublicationSettings setPreviewUrl(final String previewUrl) {
        this.previewUrl = previewUrl;
        return this;
    }

    public Integer getStatus() {
        return status;
    }

    public PublicationSettings setStatus(final Integer status) {
        this.status = status;
        return this;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PublicationSettings that = (PublicationSettings) o;
        return Objects.equals(labId, that.labId) &&
                Objects.equals(title, that.title) &&
                Objects.equals(description, that.description) &&
                Objects.equals(discipline, that.discipline) &&
                Objects.equals(estimatedTime, that.estimatedTime) &&
                Objects.equals(previewUrl, that.previewUrl) &&
                Objects.equals(status, that.status);
    }

    @Override
    public int hashCode() {
        return Objects.hash(labId, title, description, discipline, estimatedTime, previewUrl, status);
    }

    @Override
    public String toString() {
        return Json.stringify(this);
    }

}
