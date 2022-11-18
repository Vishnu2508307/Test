package com.smartsparrow.asset.data;

import com.fasterxml.jackson.annotation.JsonValue;

public enum AssetMediaType {

    IMAGE("image"),
    VIDEO("video"),
    DOCUMENT("document"),
    AUDIO("audio"),
    ICON("icon");

    private String label;

    AssetMediaType(String label) {
        this.label = label;
    }

    @JsonValue
    public String getLabel() {
        return label;
    }
}
