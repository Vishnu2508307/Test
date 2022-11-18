package com.smartsparrow.competency.data;

import java.util.Objects;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.smartsparrow.util.DateFormat;

import io.leangen.graphql.annotations.GraphQLIgnore;
import io.leangen.graphql.annotations.GraphQLQuery;

/**
 * This is a container for the data about a competency framework document.
 * <br/>
 * NOTE: Not all fields from database table are added here for now as we do not need them at the moment.
 * We will add them to this class later when we start to import third-party CASE documents to the mercury.
 */
public class Document {

    //according CASE id can be UUID of any version (1-5). That is why we use createdAt for time tracking purposes
    private UUID id;
    private String title;
    private UUID createdAt;
    private UUID createdBy;
    private UUID modifiedAt;
    private UUID modifiedBy;
    private UUID workspaceId;
    private String origin;

    public UUID getId() {
        return id;
    }

    public Document setId(UUID id) {
        this.id = id;
        return this;
    }

    public String getTitle() {
        return title;
    }

    public Document setTitle(String title) {
        this.title = title;
        return this;
    }

    @GraphQLIgnore
    public UUID getCreatedAt() {
        return createdAt;
    }

    public Document setCreatedAt(UUID createdAt) {
        this.createdAt = createdAt;
        return this;
    }

    @GraphQLQuery(name = "createdAt")
    @JsonProperty(value = "createdAt")
    public String getFormattedCreatedAt() {
        if (createdAt == null) {
            return null;
        }
        return DateFormat.asRFC1123(createdAt);
    }

    public UUID getCreatedBy() {
        return createdBy;
    }

    public Document setCreatedBy(UUID createdBy) {
        this.createdBy = createdBy;
        return this;
    }

    @GraphQLIgnore
    public UUID getModifiedAt() {
        return modifiedAt;
    }

    public Document setModifiedAt(UUID modifiedAt) {
        this.modifiedAt = modifiedAt;
        return this;
    }

    @GraphQLQuery(name = "modifiedAt")
    @JsonProperty(value = "modifiedAt")
    public String getFormattedModifiedAt() {
        if (modifiedAt == null) {
            return null;
        }
        return DateFormat.asRFC1123(modifiedAt);
    }

    public UUID getModifiedBy() {
        return modifiedBy;
    }

    public Document setModifiedBy(UUID modifiedBy) {
        this.modifiedBy = modifiedBy;
        return this;
    }

    public UUID getWorkspaceId() {
        return workspaceId;
    }

    public Document setWorkspaceId(UUID workspaceId) {
        this.workspaceId = workspaceId;
        return this;
    }

    public String getOrigin() {
        return origin;
    }

    public Document setOrigin(String origin) {
        this.origin = origin;
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Document that = (Document) o;
        return Objects.equals(id, that.id) &&
                Objects.equals(title, that.title) &&
                Objects.equals(createdAt, that.createdAt) &&
                Objects.equals(createdBy, that.createdBy) &&
                Objects.equals(modifiedAt, that.modifiedAt) &&
                Objects.equals(modifiedBy, that.modifiedBy) &&
                Objects.equals(workspaceId, that.workspaceId) &&
                Objects.equals(origin, that.origin);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, title, createdAt, createdBy, modifiedAt, modifiedBy, workspaceId, origin);
    }

    @Override
    public String toString() {
        return "Document{" +
                "id=" + id +
                ", title='" + title + '\'' +
                ", createdAt=" + createdAt +
                ", createdBy=" + createdBy +
                ", modifiedAt=" + modifiedAt +
                ", modifiedBy=" + modifiedBy +
                ", workspaceId=" + workspaceId +
                ", origin='" + origin + '\'' +
                '}';
    }
}
