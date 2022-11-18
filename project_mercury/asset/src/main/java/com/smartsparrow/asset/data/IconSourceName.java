package com.smartsparrow.asset.data;

public enum IconSourceName {

    ORIGINAL("original"),
    SMALL("small"),
    MEDIUM("medium"),
    LARGE("large");

    private String label;

    IconSourceName(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}
