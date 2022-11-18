package com.smartsparrow.eval.mutation.operations;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class ListMutationOperationSet extends ListMutationOperation {

    @Override
    public List<Object> apply(List<Object> objects, Object o) {
        return new ArrayList<Object>() {
            {
                addAll((Collection<?>) o);
            }
        };
    }
}
