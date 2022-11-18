package com.smartsparrow.learner.redirect;

import java.util.Objects;
import java.util.UUID;

public class LearnerRedirect {

    private UUID id;
    private LearnerRedirectType type;
    private String key;
    private String destinationPath;
    private UUID version;

    public LearnerRedirect() {
    }

    /**
     * @return the id of the redirect
     */
    public UUID getId() {
        return id;
    }

    public LearnerRedirect setId(final UUID id) {
        this.id = id;
        return this;
    }

    /**
     * @return the redirect type
     */
    public LearnerRedirectType getType() {
        return type;
    }

    public LearnerRedirect setType(final LearnerRedirectType type) {
        this.type = type;
        return this;
    }

    /**
     * @return the key portion of the redirect, e.g. the Product ID.
     */
    public String getKey() {
        return key;
    }

    public LearnerRedirect setKey(final String key) {
        this.key = key;
        return this;
    }

    /**
     * @return the path that the key will be redirected to including leading slash, e.g. the /class-id/deployment-id
     */
    public String getDestinationPath() {
        return destinationPath;
    }

    public LearnerRedirect setDestinationPath(final String destinationPath) {
        this.destinationPath = destinationPath;
        return this;
    }

    /**
     * @return the version of this redirect; used to track historical data.
     */
    public UUID getVersion() {
        return version;
    }

    public LearnerRedirect setVersion(final UUID version) {
        this.version = version;
        return this;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        LearnerRedirect that = (LearnerRedirect) o;
        return Objects.equals(id, that.id) &&
                type == that.type &&
                Objects.equals(key, that.key) &&
                Objects.equals(destinationPath, that.destinationPath) &&
                Objects.equals(version, that.version);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, type, key, destinationPath, version);
    }

    @Override
    public String toString() {
        return "LearnerRedirect{" +
                "id=" + id +
                ", type=" + type +
                ", key='" + key + '\'' +
                ", destinationPath='" + destinationPath + '\'' +
                ", version=" + version +
                '}';
    }
}
