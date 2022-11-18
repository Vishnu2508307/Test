package com.smartsparrow.publication.data;

import java.util.Objects;
import java.util.UUID;

public class PublicationSummary {

    private UUID id;
    private String title;
    private String description;
    private String config;
    private PublicationOutputType outputType;

    public UUID getId() {
        return id;
    }

    public PublicationSummary setId(UUID id) {
        this.id = id;
        return this;
    }

    public String getTitle() {
        return title;
    }

    public PublicationSummary setTitle(String title) {
        this.title = title;
        return this;
    }

    public String getDescription() {
        return description;
    }

    public PublicationSummary setDescription(String description) {
        this.description = description;
        return this;
    }

    public String getConfig() {
        return config;
    }

    public PublicationSummary setConfig(String config) {
        this.config = config;
        return this;
    }

    public PublicationOutputType getOutputType() {
        return outputType;
    }

    public PublicationSummary setOutputType(PublicationOutputType outputType) {
        this.outputType = outputType;
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PublicationSummary that = (PublicationSummary) o;
        return Objects.equals(id, that.id) &&
                Objects.equals(title, that.title) &&
                Objects.equals(description, that.description) &&
                Objects.equals(config, that.config) &&
                outputType == that.outputType;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, title, description, config, outputType);
    }

    @Override
    public String toString() {
        return "PublicationSummary{" +
                "id=" + id +
                ", title=" + title +
                ", description=" + description +
                ", config=" + config +
                ", outputType=" + outputType +
                '}';
    }
}
