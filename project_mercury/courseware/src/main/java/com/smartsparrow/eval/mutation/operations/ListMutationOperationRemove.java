package com.smartsparrow.eval.mutation.operations;

import java.util.Collection;
import java.util.List;

public class ListMutationOperationRemove extends ListMutationOperation {

    @Override
    public List<Object> apply(List<Object> objects, Object o) {
        objects.removeAll((Collection<?>) o);
        return objects;
    }
}
