package com.smartsparrow.plugin.data;

import com.fasterxml.jackson.annotation.JsonValue;

public enum PluginType {
    COURSE,
    UNIT,
    SCREEN,
    COMPONENT,
    PATHWAY,
    LESSON;


    @JsonValue
    public String getLabel() {
        return name().toLowerCase();
    }

    @Override
    public String toString() {
        return name().toLowerCase();
    }
}


