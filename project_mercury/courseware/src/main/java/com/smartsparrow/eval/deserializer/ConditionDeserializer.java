package com.smartsparrow.eval.deserializer;

import static com.smartsparrow.util.Warrants.affirmArgument;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.google.common.collect.Streams;
import com.newrelic.api.agent.Trace;
import com.smartsparrow.courseware.lang.ScenarioConditionParserFault;
import com.smartsparrow.eval.ScenarioField;
import com.smartsparrow.eval.operator.Operator;
import com.smartsparrow.eval.parser.BaseCondition;
import com.smartsparrow.eval.parser.ChainedCondition;
import com.smartsparrow.eval.parser.Condition;
import com.smartsparrow.eval.parser.Evaluator;
import com.smartsparrow.eval.parser.Operand;
import com.smartsparrow.eval.parser.Option;
import com.smartsparrow.eval.parser.ResolverContext;
import com.smartsparrow.exception.IllegalArgumentFault;
import com.smartsparrow.exception.UnsupportedOperationFault;
import com.smartsparrow.util.DataType;
import com.smartsparrow.util.Enums;

import reactor.core.publisher.Mono;

/**
 * Responsible for deserialization of a json string representing a scenario condition to a traversable object. The
 * following rules are enforced during deserialization:
 * <ul>
 * <li>the outer most condition is always of type {@link ChainedCondition}</li>
 * <li>a {@link ChainedCondition} can have a list of either {@link ChainedCondition} or {@link Evaluator} but never mixed</li>
 * </ul>
 */
public class ConditionDeserializer extends JsonDeserializer<ChainedCondition> {

    private final ObjectMapper mapper;

    public ConditionDeserializer() {
        this.mapper = new ObjectMapper();
        SimpleModule module = new SimpleModule();
        module.addDeserializer(ChainedCondition.class, this);
        module.addDeserializer(ResolverContext.class, new ResolverDeserializer(ResolverContext.class));
        mapper.registerModule(module);
    }

    /**
     * Deserialize a json string to a traversable {@link ChainedCondition} object
     *
     * @param json the json string to convert
     * @return a mono chained condition object
     * @throws IllegalArgumentFault             when a required entry in the json node is missing
     * @throws UnsupportedOperationFault    when an unsupported type value is found in a json entry
     * @throws ScenarioConditionParserFault when failing to parse the scenario
     */
    @Trace(async = true)
    public Mono<ChainedCondition> deserialize(String json) {

        return Mono.just(1)
                .map(ignored -> {
                    try {
                        return mapper.readValue(json, ChainedCondition.class);
                    } catch (IOException e) {
                        throw new ScenarioConditionParserFault(e);
                    }
                });
    }


    @Override
    public ChainedCondition deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
        ObjectMapper mapper = (ObjectMapper) p.getCodec();
        JsonNode root = mapper.readTree(p);
        return parseChainedCondition(root);
    }

    /**
     * Parse a json node to a chained condition
     *
     * @param node the node to convert
     * @return a chained condition
     * @throws IOException          when failing to parse a condition
     * @throws IllegalArgumentFault when required node entry are missing from the json node
     * @throws IOException          when failing to parse the condition
     */
    private ChainedCondition parseChainedCondition(final JsonNode node) throws IOException {
        if (!node.isObject()) {
            throw new UnsupportedOperationFault("json node must be an object");
        }

        JsonNode typeNode = node.get(ScenarioField.TYPE.toString());
        JsonNode operatorNode = node.get(ScenarioField.OPERATOR.toString());

        affirmArgument(typeNode != null, "`type` node is required for a ChainedCondition");
        affirmArgument(operatorNode != null, "`operator` node is required for a ChainedCondition");

        Condition.Type type = Enums.of(Condition.Type.class, typeNode.asText());
        Operator.Type operator = Enums.of(Operator.Type.class, operatorNode.asText());

        List<BaseCondition> conditions = new ArrayList<>();

        JsonNode conditionsNode = node.get(ScenarioField.CONDITIONS.toString());

        Iterator<JsonNode> elements = conditionsNode.elements();

        while (elements.hasNext()) {
            conditions.add(parseCondition(elements.next()));
        }

        return new ChainedCondition()
                .setType(type)
                .setOperator(operator)
                .setConditions(conditions);
    }

    /**
     * Parse a json node to a traversable {@link BaseCondition} object
     *
     * @param node the node to deserialize
     * @return a base condition object
     * @throws IOException                   when failing to parse
     * @throws IllegalArgumentFault          when a required json entry is missing from the json node
     * @throws UnsupportedOperationFault when the condition type is not supported
     */
    private BaseCondition parseCondition(JsonNode node) throws IOException {
        JsonNode typeNode = node.get(ScenarioField.TYPE.toString());

        affirmArgument(typeNode != null, "`type` node is required for a BaseCondition");

        Condition.Type type = Enums.of(Condition.Type.class, typeNode.asText());

        switch (type) {
            case CHAINED_CONDITION:
                return parseChainedCondition(node);
            case EVALUATOR:
                return parseEvaluator(node);
            default:
                throw new UnsupportedOperationFault(String.format("type `%s` not supported", type));
        }
    }

    /**
     * Parse a json node to an {@link Evaluator}
     *
     * @param node the node to parse
     * @return an evaluator condition object
     * @throws IOException          when failing to parse
     * @throws IllegalArgumentFault when a json entry is missing from the json node
     */
    private Evaluator parseEvaluator(final JsonNode node) throws IOException {
        JsonNode typeNode = node.get(ScenarioField.TYPE.toString());
        JsonNode operatorNode = node.get(ScenarioField.OPERATOR.toString());
        JsonNode operandTypeNode = node.get(ScenarioField.OPERAND_TYPE.toString());

        affirmArgument(typeNode != null, "`type` node is required for Evaluator");
        affirmArgument(operatorNode != null, "`operator` node is required for Evaluator");
        affirmArgument(operandTypeNode != null, "`operandType` node is required for Evaluator");

        Condition.Type type = Enums.of(Condition.Type.class, typeNode.asText());
        Operator.Type operator = Enums.of(Operator.Type.class, operatorNode.asText());
        DataType operandType = Enums.of(DataType.class, operandTypeNode.asText());

        return new Evaluator()
                .setType(type)
                .setOperator(operator)
                .setOperandType(operandType)
                .setLhs(parseOperand(node.get(ScenarioField.LHS.toString()), operandType))
                .setRhs(parseOperand(node.get(ScenarioField.RHS.toString()), operandType))
                .setOptions(parseOptions(node.get(ScenarioField.OPTIONS.toString())));
    }

    /**
     * Parse a json node to an {@link Operand} object
     *
     * @param node the json node to parse
     * @return an operand object
     * @throws IOException          when failing to parse
     * @throws IllegalArgumentFault propagated by the {@link ResolverDeserializer#deserialize(JsonParser, DeserializationContext)} method
     */
    private Operand parseOperand(final JsonNode node, final DataType operandType) throws IOException {

        JsonNode nodeValue = node.get(ScenarioField.VALUE.toString());

        Object value = (nodeValue != null ? getStrongValue(operandType, nodeValue) : null);
        return new Operand()
                .setValue(value)
                .setResolver(mapper.readValue(node.toString(), ResolverContext.class));
    }

    private Object getStrongValue(final DataType operandType, final JsonNode nodeValue) {
        switch (operandType) {
            case BOOLEAN:
                if (nodeValue.isArray()) {
                    return Streams
                            .stream(nodeValue.iterator())
                            .map(JsonNode::asBoolean)
                            .collect(Collectors.toList());
                }
                return nodeValue.asBoolean();
            case STRING:
                if (nodeValue.isArray()) {
                    return Streams
                            .stream(nodeValue.iterator())
                            .map(JsonNode::asText)
                            .collect(Collectors.toList());
                }
                return nodeValue.asText();
            case NUMBER:
                if (nodeValue.isArray()) {
                    return Streams
                            .stream(nodeValue.iterator())
                            .map(JsonNode::asDouble)
                            .collect(Collectors.toList());
                }
                return nodeValue.asDouble();
            default:
                throw new UnsupportedOperationFault("Operand type not supported");
        }
    }

    /**
     * Parse a json node to a list of {@link Option}.
     *
     * @param node the json node to parse
     * @return a list of option or an empty array list when the json node argument is <code>null</code>
     */
    private List<Option> parseOptions(final JsonNode node) {

        if (node == null) {
            return new ArrayList<>();
        }

        Iterator<JsonNode> elements = node.elements();

        List<Option> options = new ArrayList<>();

        while (elements.hasNext()) {
            options.add(parseOption(elements.next()));
        }

        return options;
    }

    /**
     * Parse a json node to an {@link Option}
     * //TODO options are still not very well flushed out so this method is likely to be improved in the future
     *
     * @param node the node to parse
     * @return an option object
     */
    private Option parseOption(final JsonNode node) {
        Iterator<Map.Entry<String, JsonNode>> fields = node.fields();

        // FIXME find a better way to parse options
        while (fields.hasNext()) {
            Map.Entry<String, JsonNode> entry = fields.next();
            Option.Type type = Enums.of(Option.Type.class, entry.getKey());
            return new Option()
                    .setType(type)
                    .setValue(entry.getValue().asText());
        }

        throw new IllegalArgumentException("empty option could not be parsed");
    }
}

