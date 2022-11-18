package com.smartsparrow.eval.mutation.operations;

import java.util.Collection;
import java.util.List;

public class ListMutationOperationAdd extends ListMutationOperation {

    @Override
    public List<Object> apply(List<Object> objects, Object o) {
        objects.addAll((Collection<?>) o);
        return objects;
    }
}
