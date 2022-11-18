package com.smartsparrow.eval.mutation.operations;

import com.smartsparrow.eval.mutation.MutationOperation;

public class SumMutationOperation implements MutationOperation<Number, Number> {

    @Override
    public Number apply(Number aNumber, Number aNumber2) {
        if(aNumber instanceof Double || aNumber2 instanceof Double) {
            return aNumber.doubleValue() + aNumber2.doubleValue();
        } else if(aNumber instanceof Float || aNumber2 instanceof Float) {
            return aNumber.floatValue() + aNumber2.floatValue();
        } else if(aNumber instanceof Long || aNumber2 instanceof Long) {
            return aNumber.longValue() + aNumber2.longValue();
        } else {
            return aNumber.intValue() + aNumber2.intValue();
        }
    }
}
