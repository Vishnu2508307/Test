package com.smartsparrow.eval.mutation;

import java.util.Map;

import javax.annotation.Nonnull;
import javax.inject.Inject;
import javax.inject.Provider;

import com.newrelic.api.agent.Trace;
import com.smartsparrow.eval.mutation.operations.ListMutationOperation;
import com.smartsparrow.exception.UnsupportedOperationFault;
import com.smartsparrow.util.DataType;

public class MutationOperationService {

    private static final String ERROR_TYPE = "no implementation for type %s";
    private static final String ERROR_OPERATOR = "no implementation for operator %s";

    private final Map<DataType, Provider<Map<MutationOperator, MutationOperation>>> mutationOperations;
    private final Map<MutationOperator, Provider<ListMutationOperation>> listMutationOperations;

    @Inject
    public MutationOperationService(final Map<DataType, Provider<Map<MutationOperator, MutationOperation>>> mutationOperations,
                                    final Map<MutationOperator, Provider<ListMutationOperation>> listMutationOperations) {
        this.mutationOperations = mutationOperations;
        this.listMutationOperations = listMutationOperations;
    }

    /**
     * Get the mutation operator implementation for a given dataType and operator
     *
     * @param dataType the data type to find the available operators for
     * @param mutationOperator the operator to find the implementation for
     * @return the implementation of a mutation operation
     * @throws UnsupportedOperationFault when the implementations are not found for either the dataType or the operator
     */
    @Trace(async = true)
    public MutationOperation getMutationOperation(@Nonnull final DataType dataType, @Nonnull final MutationOperator mutationOperator) {

        if (!this.mutationOperations.containsKey(dataType)) {
            throw new UnsupportedOperationFault(String.format(ERROR_TYPE, dataType.name()));
        }

        Map<MutationOperator, MutationOperation> operators = this.mutationOperations.get(dataType).get();

        if (!operators.containsKey(mutationOperator)) {
            throw new UnsupportedOperationFault(String.format(ERROR_OPERATOR, mutationOperator.name()));
        }
        return operators.get(mutationOperator);
    }

    /**
     * Get the list mutation operator implementation for a given mutation operator
     *
     * @param mutationOperator the mutation operator to find the implementation for
     * @return the implementation of a list mutation operation
     * @throws UnsupportedOperationFault when the implementation is not found for the given operator
     */
    @Trace(async = true)
    public ListMutationOperation getListMutationOperation(@Nonnull final MutationOperator mutationOperator) {
        if (!this.listMutationOperations.containsKey(mutationOperator)) {
            throw new UnsupportedOperationFault(String.format(ERROR_OPERATOR, mutationOperator.name()));
        }
        return this.listMutationOperations.get(mutationOperator).get();
    }

    /**
     * Get the Mutation operation implementation given dataType and mutationOperator. Allow to specify if the expected
     * implementation is for a list type
     *
     * @param dataType the data type to find the operators for
     * @param mutationOperator the mutation operation to find the implementation for
     * @param isList defines if the implementation should be search within the listMutationOperations map
     * @return the implementation of a mutation operation
     */
    @Trace(async = true)
    public MutationOperation getMutationOperation(final DataType dataType, final MutationOperator mutationOperator,
                                                  final boolean isList) {
        if (isList) {
            return getListMutationOperation(mutationOperator);
        }
        return getMutationOperation(dataType, mutationOperator);
    }
}
