package com.smartsparrow.eval.condition;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.inject.Inject;
import javax.inject.Provider;

import com.google.inject.multibindings.MapBinder;
import com.newrelic.api.agent.Trace;
import com.smartsparrow.eval.evaluator.BinaryEvaluator;
import com.smartsparrow.eval.evaluator.UnaryEvaluator;
import com.smartsparrow.eval.operator.BinaryOperator;
import com.smartsparrow.eval.operator.Operator;
import com.smartsparrow.eval.operator.UnaryOperator;
import com.smartsparrow.eval.parser.BaseCondition;
import com.smartsparrow.eval.parser.ChainedCondition;
import com.smartsparrow.eval.parser.Condition;
import com.smartsparrow.eval.parser.Evaluator;
import com.smartsparrow.eval.wiring.EvaluationOperationsModule;
import com.smartsparrow.util.DataType;

public class ConditionEvaluator {



    private final Map<Operator.Type, Provider<BinaryOperator>> genericImplementations;
    private final Map<DataType, Provider<Map<Operator.Type, BinaryOperator>>> specificImplementations;
    private final Map<Operator.Type, Provider<UnaryOperator>> unaryEvaluatorByOperator;



    @Inject
    public ConditionEvaluator(Map<Operator.Type, Provider<BinaryOperator>> genericImplementations,
                              Map<DataType, Provider<Map<Operator.Type, BinaryOperator>>> specificImplementations,
                              Map<Operator.Type, Provider<UnaryOperator>> unaryEvaluatorByOperator) {
        this.genericImplementations = genericImplementations;
        this.specificImplementations = specificImplementations;
        this.unaryEvaluatorByOperator = unaryEvaluatorByOperator;
    }

    /**
     * Recursively evaluate a {@link ChainedCondition}
     *
     * @param chainedCondition the condition to evaluate
     * @return a boolean representing the result of the evaluated condition
     * @throws UnsupportedOperationException when ChainedConditions' operator type is not AND or OR
     */
    @Trace(async = true)
    public boolean evaluate(ChainedCondition chainedCondition) {

        List<Boolean> results = chainedCondition.getConditions().stream()
                .map(this::evaluate)
                .collect(Collectors.toList());

        Operator.Type operatorType = chainedCondition.getOperator();

        switch (operatorType) {
            case AND:
                return results.stream().allMatch(one -> one.equals(Boolean.TRUE));
            case OR:
                return results.stream().anyMatch(one -> one.equals(Boolean.TRUE));
            default:
                throw new UnsupportedOperationException("Operator not supported for chained condition");
        }
    }

    /**
     * Recursive evaluation of base condition - call appropriate methods based on the condition type
     *
     * @param baseCondition
     * @return boolean representing the evaluated condition
     * @throws RuntimeException when the condition type is not {@link Evaluator} or {@link ChainedCondition}
     */
    @Trace(async = true)
    protected boolean evaluate(BaseCondition baseCondition) {

        Condition.Type type = baseCondition.getType();

        switch (type) {
            case EVALUATOR:
                return evaluate((Evaluator) baseCondition);
            case CHAINED_CONDITION:
                return evaluate((ChainedCondition) baseCondition);
            default:
                throw new RuntimeException();
        }
    }

    /**
     * Evaluation of a simple condition containing LHS and/or RHS, operator
     *
     * @param baseCondition
     * @return boolean representing the evaluated condition
     */
    @Trace(async = true)
    protected boolean evaluate(Evaluator baseCondition) {
        if (baseCondition.getLhs() == null && baseCondition.getRhs() == null) {
            throw new UnsupportedOperationException("Both LHS and RHS cannot be null");
        }
        if (baseCondition.getLhs() != null && baseCondition.getRhs() == null) {
            //Unary operation
            return processUnaryEvaluation(baseCondition);
        } else {
            //Binary Operation
            return processBinaryEvaluation(baseCondition);
        }
    }

    /**
     * Evaluate a unary expression.
     * Gets the corresponding operator implementation for the mentioned operator or throws an exception if not found
     *
     * @param condition
     * @return boolean representing the result of the evaluated unary condition
     * @throws UnsupportedOperationException when the corresponding operator implementation is not found
     */
    @Trace(async = true)
    protected boolean processUnaryEvaluation(Evaluator condition) {

        if (unaryEvaluatorByOperator.get(condition.getOperator()) != null) {
            if (condition.getLhs().getResolvedValue() == null) {
                return false;
            }
            return new UnaryEvaluator(condition.getLhs(),
                    unaryEvaluatorByOperator.get(condition.getOperator()).get(),
                    null)
                    .evaluate();
        }
        throw new UnsupportedOperationException("No operator found");
    }

    /**
     * Evaluates a binary expression.
     * The method first checks for specific implementations of operators (based on operator type and operand type),
     * if not found then checks for generic implementations of operators (based on operator type)
     * otherwise throws an Exception
     *
     * @param condition
     * @return boolean - result of evaluation
     * @throws UnsupportedOperationException - When no operator implementation is found
     */
    @Trace(async = true)
    protected boolean processBinaryEvaluation(Evaluator condition) {

        if (condition.getLhs().getResolvedValue() == null || condition.getRhs().getResolvedValue() == null) {
            return false;
        }

        if (specificImplementations.get(condition.getOperandType()) != null &&
                specificImplementations.get(condition.getOperandType()).get() != null &&
                specificImplementations.get(condition.getOperandType()).get().get(condition.getOperator()) != null) {
            //There is a specific implementation
            BinaryOperator binaryOperator = specificImplementations
                    .get(condition.getOperandType())
                    .get().get(condition.getOperator());

            return new BinaryEvaluator(condition.getLhs(),
                    condition.getRhs(),
                    binaryOperator,
                    null)
                    .evaluate();
        }
        if (genericImplementations.get(condition.getOperator()) != null) {
            //There is a generic implementation
            BinaryOperator binaryOperator = genericImplementations.get(condition.getOperator()).get();

            return new BinaryEvaluator(condition.getLhs(),
                    condition.getRhs(),
                    binaryOperator,
                    null)
                    .evaluate();

        }
        throw new UnsupportedOperationException("No operator found");
    }
}
