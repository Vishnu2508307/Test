package com.smartsparrow.plugin.payload;

/**
 * This class is aimed to represent data which should be sent as a response to a client.
 */
public class PluginVersionPayload {

    private String version;
    private String releaseDate; //an RFC 1123 formatted date
    private Boolean unpublished;

    public String getVersion() {
        return version;
    }

    public PluginVersionPayload setVersion(String version) {
        this.version = version;
        return this;
    }

    public String getReleaseDate() {
        return releaseDate;
    }

    public PluginVersionPayload setReleaseDate(String releaseDate) {
        this.releaseDate = releaseDate;
        return this;
    }

    public Boolean getUnpublished() {
        return unpublished;
    }

    public PluginVersionPayload setUnpublished(Boolean unpublished) {
        this.unpublished = unpublished;
        return this;
    }
}
