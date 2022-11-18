package com.smartsparrow.learner.data;

import java.text.DecimalFormat;
import java.util.function.Function;

/**
 * This class is responsible for formatting a score value
 */
public class ScoreFormatter implements Function<Double, Double> {

    @Override
    public Double apply(Double unformattedScore) {
        final DecimalFormat decimalFormat = new DecimalFormat("0.00000");

        return Double.valueOf(decimalFormat.format(unformattedScore));
    }
}
