package com.smartsparrow.learner.data;

import java.util.function.Function;

/**
 * This class responsibility is to ensure that a negative value is never returned. The function takes in a double
 * and if this is less than 0 then 0 is returned
 */
public class AdditiveInverser implements Function<Double, Double> {

    @Override
    public Double apply(Double value) {
        if (Double.doubleToRawLongBits(value) < 0) {
            // same as: value + value*-1;
            return 0d;
        }
        return value;
    }
}
