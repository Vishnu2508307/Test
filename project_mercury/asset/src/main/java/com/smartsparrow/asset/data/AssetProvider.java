package com.smartsparrow.asset.data;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public enum AssetProvider {
    AERO("aero"),
    EXTERNAL("external"),
    ALFRESCO("alfresco"),
    MATH("math");

    private String label;

    AssetProvider(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }

    public static AssetProvider fromLabel(String label) {
        List<AssetProvider> names = Arrays.stream(AssetProvider.values())
                .filter(name -> name.getLabel().equals(label))
                .collect(Collectors.toList());
        if (names.isEmpty()) {
            throw new IllegalArgumentException("Invalid format");
        } else {
            return names.get(0);
        }
    }
}
