package com.smartsparrow.eval.wiring;

import static com.smartsparrow.eval.operator.Operator.Type.AND;
import static com.smartsparrow.eval.operator.Operator.Type.CONTAINS;
import static com.smartsparrow.eval.operator.Operator.Type.CONTAINS_ANY_OF;
import static com.smartsparrow.eval.operator.Operator.Type.CONTAINS_ONE_OF;
import static com.smartsparrow.eval.operator.Operator.Type.DOES_NOT_CONTAIN;
import static com.smartsparrow.eval.operator.Operator.Type.DOES_NOT_CONTAIN_ANY_OF;
import static com.smartsparrow.eval.operator.Operator.Type.DOES_NOT_CONTAIN_ONE_OF;
import static com.smartsparrow.eval.operator.Operator.Type.DOES_NOT_INCLUDE;
import static com.smartsparrow.eval.operator.Operator.Type.DOES_NOT_INCLUDE_ALL_OF;
import static com.smartsparrow.eval.operator.Operator.Type.DOES_NOT_INCLUDE_ANY_OF;
import static com.smartsparrow.eval.operator.Operator.Type.ENDS_WITH;
import static com.smartsparrow.eval.operator.Operator.Type.EQUALS;
import static com.smartsparrow.eval.operator.Operator.Type.GE;
import static com.smartsparrow.eval.operator.Operator.Type.GT;
import static com.smartsparrow.eval.operator.Operator.Type.INCLUDES_ALL_OF;
import static com.smartsparrow.eval.operator.Operator.Type.INCLUDES_ANY_OF;
import static com.smartsparrow.eval.operator.Operator.Type.IS;
import static com.smartsparrow.eval.operator.Operator.Type.IS_NOT;
import static com.smartsparrow.eval.operator.Operator.Type.LE;
import static com.smartsparrow.eval.operator.Operator.Type.LT;
import static com.smartsparrow.eval.operator.Operator.Type.NOT;
import static com.smartsparrow.eval.operator.Operator.Type.NOT_EQUALS;
import static com.smartsparrow.eval.operator.Operator.Type.OR;
import static com.smartsparrow.eval.operator.Operator.Type.STARTS_WITH;

import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.AbstractModule;
import com.google.inject.TypeLiteral;
import com.google.inject.multibindings.MapBinder;
import com.smartsparrow.eval.condition.ConditionEvaluator;
import com.smartsparrow.eval.operator.BinaryOperator;
import com.smartsparrow.eval.operator.Operator;
import com.smartsparrow.eval.operator.UnaryOperator;
import com.smartsparrow.eval.operator.and.AndOperator;
import com.smartsparrow.eval.operator.contains.ContainsOperator;
import com.smartsparrow.eval.operator.containsanyof.ContainsAnyOfOperator;
import com.smartsparrow.eval.operator.containsoneof.ContainsOneOfOperator;
import com.smartsparrow.eval.operator.doesnotcontain.DoesNotContainsOperator;
import com.smartsparrow.eval.operator.doesnotcontainanyof.DoesNotContainAnyOfOperator;
import com.smartsparrow.eval.operator.doesnotcontainoneof.DoesNotContainOneOfOperator;
import com.smartsparrow.eval.operator.doesnotinclude.DoesNotIncludeOperator;
import com.smartsparrow.eval.operator.doesnotincludeallof.DoesNotIncludeAllOfOperator;
import com.smartsparrow.eval.operator.doesnotincludeanyof.DoesNotIncludeAnyOfOperator;
import com.smartsparrow.eval.operator.endswith.EndsWithOperator;
import com.smartsparrow.eval.operator.equals.EqualsOperator;
import com.smartsparrow.eval.operator.ge.GEOperatorDouble;
import com.smartsparrow.eval.operator.gt.GTOperatorDouble;
import com.smartsparrow.eval.operator.includesallof.IncludesAllOfOperator;
import com.smartsparrow.eval.operator.includesanyof.IncludesAnyOfOperator;
import com.smartsparrow.eval.operator.is.ISOperator;
import com.smartsparrow.eval.operator.isnot.IsNotOperator;
import com.smartsparrow.eval.operator.le.LEOperatorDouble;
import com.smartsparrow.eval.operator.lt.LTOperatorDouble;
import com.smartsparrow.eval.operator.not.NotOperatorBoolean;
import com.smartsparrow.eval.operator.notequals.NotEqualsOperator;
import com.smartsparrow.eval.operator.or.OrOperatorBoolean;
import com.smartsparrow.eval.operator.startswith.StartsWithOperator;
import com.smartsparrow.util.DataType;

public class EvaluationOperationsModule extends AbstractModule {

    private static final Logger log = LoggerFactory.getLogger(EvaluationOperationsModule.class);

    private MapBinder<Operator.Type, BinaryOperator> binaryEvaluatorsByOperator;

    private MapBinder<DataType, Map<Operator.Type, BinaryOperator>> binaryEvaluatorsByOperandTypeOperator;

    private MapBinder<Operator.Type, UnaryOperator> unaryEvaluatorByOperator;

    @Override
    protected void configure() {

        bind(ConditionEvaluator.class);
        // setup the binders for the pathway types
        binaryEvaluatorsByOperator = MapBinder.newMapBinder(binder(),  //
                                              new TypeLiteral<Operator.Type>() {}, //
                                              new TypeLiteral<BinaryOperator>() {
                                              });
        unaryEvaluatorByOperator = MapBinder.newMapBinder(binder(),
                                                          new TypeLiteral<Operator.Type>(){},
                                                          new TypeLiteral<UnaryOperator>(){});

        binaryEvaluatorsByOperandTypeOperator = MapBinder.newMapBinder(binder(),
                                                                       new TypeLiteral<DataType>(){},
                                                                       new TypeLiteral<Map<Operator.Type, BinaryOperator>>(){});

        /**
         * Setup all the binary evaluators with default implementations for a given binary operator
         */
        binaryEvaluatorsByOperator.addBinding(IS).to(ISOperator.class);
        binaryEvaluatorsByOperator.addBinding(AND).to(AndOperator.class);
        binaryEvaluatorsByOperator.addBinding(OR).to(OrOperatorBoolean.class);
        binaryEvaluatorsByOperator.addBinding(EQUALS).to(EqualsOperator.class);
        binaryEvaluatorsByOperator.addBinding(IS_NOT).to(IsNotOperator.class);
        binaryEvaluatorsByOperator.addBinding(NOT_EQUALS).to(NotEqualsOperator.class);
        binaryEvaluatorsByOperator.addBinding(DOES_NOT_INCLUDE).to(DoesNotIncludeOperator.class);
        binaryEvaluatorsByOperator.addBinding(INCLUDES_ALL_OF).to(IncludesAllOfOperator.class);
        binaryEvaluatorsByOperator.addBinding(DOES_NOT_INCLUDE_ALL_OF).to(DoesNotIncludeAllOfOperator.class);
        binaryEvaluatorsByOperator.addBinding(INCLUDES_ANY_OF).to(IncludesAnyOfOperator.class);
        binaryEvaluatorsByOperator.addBinding(DOES_NOT_INCLUDE_ANY_OF).to(DoesNotIncludeAnyOfOperator.class);
        binaryEvaluatorsByOperator.addBinding(CONTAINS).to(ContainsOperator.class);

        /**
         * Setup specific binary evaluators matching an operand type and operator type
         */

        binaryEvaluatorsByOperandTypeOperator.addBinding(DataType.STRING).toInstance(new HashMap<Operator.Type, BinaryOperator>() {
            {
                put(CONTAINS_ANY_OF, new ContainsAnyOfOperator());
                put(CONTAINS_ONE_OF, new ContainsOneOfOperator());
                put(DOES_NOT_CONTAIN, new DoesNotContainsOperator());
                put(DOES_NOT_CONTAIN_ANY_OF, new DoesNotContainAnyOfOperator());
                put(DOES_NOT_CONTAIN_ONE_OF, new DoesNotContainOneOfOperator());
                put(STARTS_WITH, new StartsWithOperator());
                put(ENDS_WITH, new EndsWithOperator());
            }
        });

        binaryEvaluatorsByOperandTypeOperator.addBinding(DataType.NUMBER).toInstance(new HashMap<Operator.Type, BinaryOperator>() {
            {
                put(GE, new GEOperatorDouble());
                put(LE, new LEOperatorDouble());
                put(LT, new LTOperatorDouble());
                put(GT, new GTOperatorDouble());
            }
        });

        /**
         * All unary operators need to be plugged in here
         */
        unaryEvaluatorByOperator.addBinding(NOT).to(NotOperatorBoolean.class);
    }
}
