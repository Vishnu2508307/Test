package com.smartsparrow.asset.data;

public enum AudioSourceName {

    ORIGINAL("original");

    private String label;

    AudioSourceName(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}
