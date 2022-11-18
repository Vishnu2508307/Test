package com.smartsparrow.asset.data;

public enum VideoSourceName {

    ORIGINAL("original");

    private String label;

    VideoSourceName(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}
