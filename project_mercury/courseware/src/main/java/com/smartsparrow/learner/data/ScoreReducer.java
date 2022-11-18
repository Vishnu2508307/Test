package com.smartsparrow.learner.data;

import java.util.function.BiFunction;

/**
 * This class is responsible for reducing all the score adjustment values to a single value.
 * Perform a sum of all values respecting the following rules:
 * <ul>
 *     <li>Sets the previous value to <b>0</b> when negative</li>
 * </ul>
 */
public class ScoreReducer implements BiFunction<Double, Double, Double> {

    @Override
    public Double apply(Double prev, Double next) {
        Double previousAdjustmentValue = prev;

        // if the previous value is less than 0 then set it to 0
        if (Double.doubleToRawLongBits(previousAdjustmentValue) < 0) {
            previousAdjustmentValue = 0d;
        }

        return previousAdjustmentValue + next;
    }
}
