package com.smartsparrow.asset.data;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public enum ImageSourceName {

    ORIGINAL("original", Double.MAX_VALUE),
    THUMB("thumb", 150.0),
    SMALL("small", 300.0),
    MEDIUM("medium", 600.0),
    LARGE("large", 1500.0),
    EXTRA_LARGE("extra-large", 2500.0);

    private String label;
    private Double threshold;

    ImageSourceName(String label, Double threshold) {
        this.label = label;
        this.threshold = threshold;
    }

    public String getLabel() {
        return label;
    }

    public Double getThreshold() {
        return threshold;
    }

    public static ImageSourceName fromLabel(String label) {
        List<ImageSourceName> names = Arrays.stream(ImageSourceName.values())
                .filter(name -> name.getLabel().equals(label))
                .collect(Collectors.toList());
        if (names.isEmpty()) {
            return ORIGINAL;
        } else {
            return names.get(0);
        }
    }
}
