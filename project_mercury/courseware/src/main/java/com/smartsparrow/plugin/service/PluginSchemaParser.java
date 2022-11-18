package com.smartsparrow.plugin.service;

import static com.google.common.base.Preconditions.checkArgument;
import static com.smartsparrow.util.Warrants.affirmNotNull;

import java.util.Iterator;
import java.util.Set;

import javax.inject.Singleton;

import org.apache.commons.lang.StringUtils;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.JsonNode;
import com.google.common.collect.Sets;
import com.newrelic.api.agent.Trace;
import com.smartsparrow.exception.IllegalArgumentFault;
import com.smartsparrow.learner.data.LearnerInteractiveGateway;
import com.smartsparrow.learner.lang.InvalidFieldsException;
import com.smartsparrow.util.Json;
import com.smartsparrow.util.log.MercuryLogger;
import com.smartsparrow.util.log.MercuryLoggerFactory;

@Singleton
public class PluginSchemaParser {

    private static final MercuryLogger log = MercuryLoggerFactory.getLogger(PluginSchemaParser.class);

    private static final String LEARNER_EDITABLE = "learnerEditable";
    private static final String PROPERTIES = "properties";
    private static final String GROUP = "group";
    private static final String DEFAULT = "default";

    /**
     * Extract plugin schema entries which can be set at learn time.
     * It extracts only top level entries and group type entries
     *
     * @param configurationSchema plugin schema
     * @return output schema (sub-set of plugin schema)
     * @throws JSONException when plugin schema json is invalid
     * @throws IllegalStateException if argument schema is <code>null</code>
     */
    public String extractOutputSchema(String configurationSchema) {
        // parse the configuration schema
        JsonNode schemaObject = Json.toJsonNode(configurationSchema);

        // initialise the output schema object
        JSONObject outputSchema = new JSONObject();

        Iterator<String> fields = schemaObject.fieldNames();

        while (fields.hasNext()) {
            // extract the learner editable fields
            String field = fields.next();

            String editable = extractLearnerEditableField(schemaObject.get(field));

            if (editable != null) {
                // add to schema object
                outputSchema.put(field, new JSONObject(new JSONTokener(editable)));
            }
        }
        return outputSchema.toString();
    }

    /**
     * Extract the learner editable fields from the supplied json node
     *
     * @param jsonNode the json node to extract the learnerEditable fields from
     * @return a stringified json representing the extracted nodes or <code>null</code> when either no learnerEditable
     * entries are found or the node is not an object node
     * @throws IllegalArgumentFault when the type field is not found in the json node
     */
    private String extractLearnerEditableField(JsonNode jsonNode) {
        if (jsonNode.isArray()) {
            // skip arrays for now
            return null;
        }

        if (!jsonNode.isObject()) {
            // skip non objects
            return null;
        }

        // check that the node has a type
        JsonNode typeNode = jsonNode.get("type");

        affirmNotNull(typeNode, "type node cannot be null");

        String type = typeNode.asText();

        if (type.equals(GROUP)) {
            // handle group case
            return extractOutputSchemaFromGroup(jsonNode);
        }
        // handle any other case
        return extractOutputSchema(jsonNode);
    }

    private String extractOutputSchema(JsonNode jsonNode) {
        if (jsonNode.has(LEARNER_EDITABLE)) {
            if (!jsonNode.get(LEARNER_EDITABLE).isBoolean()) {
                throw new JSONException("boolean type expected for property " + LEARNER_EDITABLE);
            }
            if (jsonNode.get(LEARNER_EDITABLE).asBoolean()) {
                return jsonNode.toString();
            }
        }
        return null;
    }

    /**
     * Traverse a 'group' type json node and for each node in the properties it extracts the learner editable nodes
     *
     * @param jsonNode the group jsonNode to extract the learnerEditable entries from
     * @return a stringified json representing the extracted properties or <code>null</code> when there are no
     * learnerEditable entries to extract
     * @throws IllegalArgumentFault when the properties node is missing from a type group node
     */
    private String extractOutputSchemaFromGroup(JsonNode jsonNode) {
        JSONObject extracted = new JSONObject();

        JsonNode propertiesNode = jsonNode.get(PROPERTIES);

        affirmNotNull(propertiesNode, "properties node missing from group type node");

        Iterator<String> fields = propertiesNode.fieldNames();

        while (fields.hasNext()) {
            String field = fields.next();

            String editable = extractOutputSchema(propertiesNode.get(field));

            if (editable != null) {
                extracted.put(field, new JSONObject(new JSONTokener(editable)));
            }
        }

        if (!extracted.isEmpty()) {
            return extracted.toString();
        }
        return null;
    }

    /**
     * Construct config only for values from output schema. If any of the parameters is empty the method returns empty string.
     * If field from output schema does not have value in config, then this field will have null value in output config.
     * <p>Example:
     * <br/>
     * outputSchema:
     * {
     * "selection": {
     *     "type": "list",
     *     "listType": "text",
     *     "learnerEditable": true,
     *     "label": "selection"
     *   }
     * }
     * <br/>
     * config: {}
     * <br/>
     * The result output config will be : {"selection":null}
     * </p>
     *
     * @param outputSchema the output schema
     * @param config       the full config with values
     * @return the config with only values for output schema properties
     * @throws JSONException         if outputSchema or config is invalid json
     * @throws IllegalStateException if outputSchema or config is null
     */
    public String extractOutputConfig(String outputSchema, String config) {
        checkArgument(outputSchema != null, "outputSchema can not be null");
        checkArgument(config != null, "outputSchema can not be null");

        if (StringUtils.isEmpty(outputSchema) || StringUtils.isEmpty(config)) {
            return StringUtils.EMPTY;
        }

        JSONObject schemaObject = Json.parse(outputSchema);
        JSONObject configObject = Json.parse(config);
        JSONObject scope = new JSONObject();

        schemaObject.keySet().forEach(key -> {
            if (configObject.has(key)) {
                scope.put(key, configObject.get(key));
                return;
            }
            JsonNode current = Json.toJsonNode(schemaObject.get(key).toString());
            Object defaultValue = extractDefaultValue(current);
            if (defaultValue != null) {
                scope.put(key, defaultValue);
                return;
            }
            scope.put(key, JSONObject.NULL);
        });

        return scope.toString();
    }

    /**
     * Validate that data and output schema has exactly the same set of fields. Throw error fi it is not.
     * @param data the data object to validate the fields for
     * @param schema the output schema object to validate the fields against
     *
     * @throws JSONException when either data or schema are not valid json objects
     * @throws InvalidFieldsException if not all fields from schema present in data, or data has extra fields
     */
    @Trace(async = true)
    public void validateDataAgainstSchema(String data, String schema) {
        JSONObject dataObject = Json.parse(data);
        JSONObject outputSchemaObject = Json.parse(schema);

        final Set<String> dataFields = dataObject.keySet();

        final Set<String> schemaFields = outputSchemaObject.keySet();

        //verify if data has fields which does not present in schema
        Set<String> fieldsNotFoundInSchema = Sets.newHashSet(dataFields);
        fieldsNotFoundInSchema.removeAll(schemaFields);
        if (!fieldsNotFoundInSchema.isEmpty()) {
            throw new InvalidFieldsException("data has fields which no found in output schema", fieldsNotFoundInSchema);
        }

        //verify that data has all fields from schema
        Set<String> fieldsNotFoundInData = Sets.newHashSet(schemaFields);
        fieldsNotFoundInData.removeAll(dataFields);
        if (!fieldsNotFoundInData.isEmpty()) {
            throw new InvalidFieldsException("data does not have all fields from output schema", fieldsNotFoundInData);
        }
    }

    /**
     * Extract the default value from the supplied json node
     *
     * @param jsonNode the json node to extract the default value from
     * @return a stringified json representing the default value or <code>null</code> when either no default
     * value is found or the node is not an object node
     * @throws IllegalArgumentFault when the type field is not found in the json node
     */
    private Object extractDefaultValue(JsonNode jsonNode) {
        if (jsonNode.isArray()) {
            // skip arrays for now
            log.info("Default value not supported for array nodes");
            return null;
        }

        if (!jsonNode.isObject()) {
            // skip non objects
            log.info("Default value not supported for non object nodes");
            return null;
        }

        // check that the node has a type
        JsonNode typeNode = jsonNode.get("type");

        if (typeNode == null) {
            // handle group case
            return extractDefaultValueFromGroup(jsonNode);
        }
        // handle any other case
        return extractObjectDefaultValue(jsonNode);
    }

    private Object extractObjectDefaultValue(JsonNode jsonNode) {
        if (jsonNode.has(DEFAULT)) {
            if (jsonNode.get(DEFAULT).isValueNode()) {
                return jsonNode.get(DEFAULT).asText();
            }
            return jsonNode.get(DEFAULT);
        }
        return null;
    }


    /**
     * Traverse a 'group' type json node and for each node in the properties it extracts the default value
     *
     * @param jsonNode the group jsonNode to extract the default value from
     * @return a stringified json representing the default value or <code>null</code> when either no default
     *      * value is found or the node is not an object node
     * @throws IllegalArgumentFault when the properties node is missing from a type group node
     */
    private JSONObject extractDefaultValueFromGroup(JsonNode jsonNode) {
        JSONObject extracted = new JSONObject();

        Iterator<String> fields = jsonNode.fieldNames();

        while (fields.hasNext()) {
            String field = fields.next();

            Object value = extractObjectDefaultValue(jsonNode.get(field));

            if (value != null) {
                extracted.put(field, value);
            }
        }

        if (!extracted.isEmpty()) {
            return extracted;
        }
        return null;
    }
}
