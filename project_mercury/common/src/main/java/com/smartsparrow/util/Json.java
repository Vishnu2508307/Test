package com.smartsparrow.util;

import java.io.IOException;
import java.text.NumberFormat;
import java.text.ParseException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.annotation.Nonnull;

import com.smartsparrow.exception.IllegalArgumentFault;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

public class Json {

    /**
     * Serialize an object to a string
     *
     * @param object the object to serialize to a string
     * @return a json string representation of the object
     * @throws JSONException when failing to serialize the object to a json string
     */
    public static String stringify(Object object) {
        ObjectMapper objectMapper = new ObjectMapper();

        // configure the mapper
        objectMapper.disable(SerializationFeature.FAIL_ON_EMPTY_BEANS);

        try {
            return objectMapper.writeValueAsString(object);
        } catch (JsonProcessingException e) {
            // re-throw as some kind of runtime exception since this is not recoverable
            throw new JSONException(e);
        }
    }

    /**
     * Parse a json string into a Map. If the value of the field is a nested json object, this is returned as a string
     *
     * @param jsonString the string to parse
     * @return an Map of String -> String representation of the json object
     * @throws JSONException when failing to parse the json string
     */
    public static Map<String, String> toMap(String jsonString) {
        try {
            // get the root node
            JsonNode root = new ObjectMapper().readValue(jsonString, JsonNode.class);

            // get all fields on the root node
            Iterator<String> fields = root.fieldNames();

            // initialise the map
            Map<String, String> parsed = new HashMap<>();

            while (fields.hasNext()) {
                String field = fields.next();
                // get the field node
                JsonNode node = root.get(field);

                String value = getStringValue(node);

                // add the value to the map if not null
                if (value != null) {
                    parsed.put(field, value);
                }
            }

            return parsed;

        } catch (IOException e) {
            throw new JSONException(e);
        }
    }

    /**
     * Return the string value for a json node. For array and object nodes the stringified version of the node is
     * returned, for all other types the text value is returned, unless the node value is null then <code>null</code>
     * is returned.
     * @param node the node to return the string value for
     * @return a string value
     */
    public static String getStringValue(JsonNode node) {
        String value;

        if (node.isArray() || node.isObject()) {
            value = node.toString();
        } else if (node.isNull()) {
          value = null;
        } else {
            value = node.asText();
        }
        return value;
    }

    /**
     * Parse a string into a json object
     *
     * @param jsonString the string to parse
     * @return a json object
     * @throws JSONException if the string has an invalid json format
     */
    public static JSONObject parse(String jsonString) {
        return new JSONObject(new JSONTokener(jsonString));
    }

    /**
     * Parse a json string into a json node
     *
     * @param json the json string to parse
     * @return a JsonNode object
     * @throws JSONException when the json string is invalid
     */
    public static JsonNode toJsonNode(String json) {
        ObjectMapper objectMapper = new ObjectMapper();

        try {
            return objectMapper.readValue(json, JsonNode.class);
        } catch (IOException e) {
            throw new JSONException("invalid json", e);
        }
    }

    /**
     * parse a json string into a json node
     * @param json json the json string to parse
     * @param errorMessage the error message
     * @return a JsonNode object
     * @throws IllegalArgumentFault when the json string is invalid with error message
     */
    public static JsonNode toJsonNode(String json, String errorMessage) {
        try {
            return Json.toJsonNode(json);
        } catch (JSONException ex) {
            throw new IllegalArgumentFault(errorMessage);
        }
    }

    /**
     * Query a json string value given a list of context path
     *
     * @param jsonString the json string to query
     * @param contextPath the json path
     * @return the json object found at that path in the json string
     * @throws JSONException when the contextPath argument is empty.
     */
    public static Object query(@Nonnull String jsonString, @Nonnull List<String> contextPath) {

        if (contextPath.isEmpty()) {
            throw new JSONException("contextPath must not be empty");
        }

        JSONObject json = Json.parse(jsonString);

        return query(json, contextPath);
    }

    /**
     * Query a json string value given the context path and unwrap its value to the desired type.
     *
     * @param jsonString the json string to query
     * @param contextPath the json path
     * @param type the class type to use for casting the found value
     * @param <T> the type of value to return
     * @return the value in the expected data type
     * @throws ClassCastException when failing to cast the found value
     */
    public static <T> T query(@Nonnull String jsonString, @Nonnull List<String> contextPath, Class<T> type) {
        Object result = query(jsonString, contextPath);

        if (result instanceof JSONArray) {
            try {
                return new ObjectMapper().readValue(result.toString(), type);
            } catch (IOException e) {
                throw new JSONException("invalid json", e);
            }
        }
        return type.cast(query(jsonString, contextPath));
    }

    /**
     * Query a json object value given the context path in a list
     *
     * @param json the json object to query the value from
     * @param contextPath the context path to query
     * @return a json object representing the wanted value
     * @throws JSONException when the supplied contextPath does not exists in the json string
     */
    public static Object query(@Nonnull JSONObject json, @Nonnull List<String> contextPath) {
        String jsonPath = "/" + String.join("/", contextPath);

        Object result = json.query(jsonPath);

        if (result == null) {
            throw new JSONException(String.format("could not find path %s", contextPath.toString()));
        }

        return result;
    }

    /**
     * Unwrap the object value ensuring that the appropriate type is returned.
     * This method is used when applying MutationOperation for change scope action
     *
     * @param value the value to unwrap
     * @param dataType the data type to unwrap the value to
     * @return the unwrapped value either a {@link String}, {@link Boolean} or a {@link Number}
     * @throws JSONException when the data type is not supported
     */
    public static Object unwrapValue(@Nonnull Object value, @Nonnull DataType dataType) {
        if (JSONObject.NULL.equals(value)) {
            return null;
        }

        // if an array return the first element
        if (value instanceof JSONArray) {
            List<Object> values = ((JSONArray) value).toList();
            switch (dataType) {
                case STRING:
                    return values.stream().map(Object::toString).collect(Collectors.toList());
                case BOOLEAN:
                    return values.stream().map(item -> (Boolean) item).collect(Collectors.toList());
                case NUMBER:
                    return values.stream().map(item -> {
                        try {
                            return NumberFormat.getInstance().parse(item.toString());
                        } catch (ParseException e) {
                            throw new JSONException(e);
                        }
                    }).collect(Collectors.toList());
                default:
                    throw new JSONException(String.format("dataType %s not supported", dataType));
            }
        }

        // return the appropriate type
        switch (dataType) {
            case STRING:
                return value.toString();
            case BOOLEAN:
                return Boolean.valueOf(value.toString());
            case NUMBER:
                try {
                    return NumberFormat.getInstance().parse(value.toString());
                } catch (ParseException e) {
                    throw new JSONException(e);
                }
            default:
                throw new JSONException(String.format("dataType %s not supported", dataType));
        }
    }

    /**
     * Unwrap the object value ensuring that the appropriate type is returned. The numeric value is forced to a Double.
     * This method is used on OperandScopeResolver and ActionScopeResolver.
     *
     * @implNote this method exists so that condition operators do not fail during evaluation matching. For new implementations
     * the use of {@link Json#unwrapValue(Object, DataType)} should be favoured instead of this method.
     *
     * @param value the value to unwrap
     * @param dataType the data type to unwrap the value to
     * @return the unwrapped value either a {@link String}, {@link Boolean} or a {@link Double}
     * @throws JSONException when the data type is not supported
     */
    public static Object _unwrapValue(@Nonnull Object value, @Nonnull DataType dataType) {
        if (JSONObject.NULL.equals(value)) {
            return null;
        }

        // if an array return the first element
        if (value instanceof JSONArray) {
            List<Object> values = ((JSONArray) value).toList();
            switch (dataType) {
                case STRING:
                    return values.stream().map(Object::toString).collect(Collectors.toList());
                case BOOLEAN:
                    return values.stream().map(item -> (Boolean) item).collect(Collectors.toList());
                case NUMBER:
                    return values.stream().map(item -> (Double) item).collect(Collectors.toList());
                default:
                    throw new JSONException(String.format("dataType %s not supported", dataType));
            }
        }

        // return the appropriate type
        switch (dataType) {
            case STRING:
                return value.toString();
            case BOOLEAN:
                return Boolean.valueOf(value.toString());
            case NUMBER:
                return Double.valueOf(value.toString());
            default:
                throw new JSONException(String.format("dataType %s not supported", dataType));
        }
    }

    /**
     * Perform a mutation on the supplied json object. Replace the value at the contextPath with the supplied
     * value.<br>
     * Given the following json:
     * <pre>
     * {
     *   "object": {
     *     "foo": "bar"
     *    }
     * }</pre>
     * - and context path: <code>["object", "foo"]</code><br>
     * - and value: <code>"replaced"</code><br>
     * the output will be:
     * <pre>
     * {
     *   "object": {
     *     "foo": "replaced"
     *    }
     * }</pre>
     * @param json the json object to replace a value for
     * @param contextPath the context path where the value will be replaced
     * @param value the new value to place at the specific context path
     * @return the replaced json object
     * @throws JSONException when the context path is not found in the json
     */
    public static JSONObject replace(JSONObject json, List<String> contextPath, Object value) {

        JSONObject current = json;

        for (int i = 0; i < contextPath.size(); i++) {
            String path = contextPath.get(i);
            Object obj = current.get(path);

            if (obj instanceof JSONObject) {
                current = (JSONObject) obj;
                if (i < (contextPath.size() - 1)) {
                    continue;
                }
            }

            if (i < (contextPath.size() - 1)) {
                throw new JSONException("context path not found in json object");
            }

            if (i == (contextPath.size() - 1)) {
                current.put(path, value);
            }
        }

        return json;
    }
}
