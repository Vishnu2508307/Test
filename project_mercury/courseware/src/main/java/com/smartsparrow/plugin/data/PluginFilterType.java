package com.smartsparrow.plugin.data;

import java.util.Arrays;


public enum PluginFilterType {
    ID,
    TAGS;

    public static boolean allows(String filterType) {
        return Arrays.stream(PluginFilterType.values())
                .anyMatch(one -> one.toString().equalsIgnoreCase(filterType));
    }
}

