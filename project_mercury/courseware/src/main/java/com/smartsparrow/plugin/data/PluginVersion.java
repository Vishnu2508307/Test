package com.smartsparrow.plugin.data;

import java.util.Objects;
import java.util.UUID;

public class PluginVersion {

    private UUID pluginId;
    private int major;
    private int minor;
    private int patch;
    private long releaseDate;
    private String preRelease;
    private String build;
    private Boolean unpublished;

    public UUID getPluginId() {
        return pluginId;
    }

    public PluginVersion setPluginId(UUID pluginId) {
        this.pluginId = pluginId;
        return this;
    }

    public int getMajor() {
        return major;
    }

    public PluginVersion setMajor(int major) {
        this.major = major;
        return this;
    }

    public int getMinor() {
        return minor;
    }

    public PluginVersion setMinor(int minor) {
        this.minor = minor;
        return this;
    }

    public int getPatch() {
        return patch;
    }

    public PluginVersion setPatch(int patch) {
        this.patch = patch;
        return this;
    }

    public long getReleaseDate() {
        return releaseDate;
    }

    public PluginVersion setReleaseDate(long releaseDate) {
        this.releaseDate = releaseDate;
        return this;
    }

    public String getPreRelease() {
        return preRelease;
    }

    public PluginVersion setPreRelease(String preRelease) {
        this.preRelease = preRelease;
        return this;
    }

    public String getBuild() {
        return build;
    }

    public PluginVersion setBuild(String build) {
        this.build = build;
        return this;
    }

    public Boolean getUnpublished() {
        return unpublished;
    }

    public PluginVersion setUnpublished(Boolean unpublished) {
        this.unpublished = unpublished;
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PluginVersion that = (PluginVersion) o;
        return major == that.major &&
                minor == that.minor &&
                patch == that.patch &&
                releaseDate == that.releaseDate &&
                Objects.equals(pluginId, that.pluginId) &&
                Objects.equals(preRelease, that.preRelease) &&
                Objects.equals(build, that.build) &&
                Objects.equals(unpublished, that.unpublished);
    }

    @Override
    public int hashCode() {
        return Objects.hash(pluginId, major, minor, patch, releaseDate, preRelease, build, unpublished);
    }
}
