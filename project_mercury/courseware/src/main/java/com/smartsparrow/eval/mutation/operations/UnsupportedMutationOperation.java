package com.smartsparrow.eval.mutation.operations;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.smartsparrow.eval.mutation.MutationOperation;

public class UnsupportedMutationOperation implements MutationOperation<Object, Object> {

    private static final Logger log = LoggerFactory.getLogger(UnsupportedMutationOperation.class);

    @Override
    public Object apply(Object o, Object o2) {
        log.info("this mutation operation is unsupported. No mutation will be performed and the original object is returned");
        return o;
    }
}
