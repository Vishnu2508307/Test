package com.smartsparrow.eval.mutation.operations;

import com.smartsparrow.eval.mutation.MutationOperation;

public class MutationOperationSet implements MutationOperation<Object, Object> {

    @Override
    public Object apply(Object o, Object o2) {
        return o2;
    }
}
