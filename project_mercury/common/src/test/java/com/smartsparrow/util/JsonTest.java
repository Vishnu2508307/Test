package com.smartsparrow.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Map;

import org.json.JSONException;
import org.json.JSONObject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.databind.JsonNode;
import com.google.common.collect.Lists;

class JsonTest {

    private final static String jsonString = "{\n" +
                                        "\"action\": \"CHANGE_SCOPE\",\n" +
                                        "\"resolver\": {\n" +
                                            "\"type\": \"LITERAL\"\n" +
                                        "},\n" +
                                        "\"context\": {\n" +
                                            "\"studentScopeURN\": \"<scope-urn>\",\n" +
                                            "\"sourceId\": \"<source-id>\",\n" +
                                            "\"schemaProperty\": {" +
                                                "\"type\":\"list\"" +
                                            "},\n" +
                                            "\"dataType\": \"STRING\",\n" +
                                            "\"operator\": \"SET\",\n" +
                                            "\"context\": [\n" +
                                                "\"selection\"\n" +
                                            "],\n" +
                                            "\"value\": \"a\"\n" +
                                        "}\n" +
                                        "}";
    private JSONObject jsonObject;

    @BeforeEach
    void setUp() {
        jsonObject = Json.parse(jsonString);
    }

    @Test
    void replace_success() {
        List<String> contextPath = Lists.newArrayList("context", "schemaProperty", "type");

        JSONObject replaced = Json.replace(jsonObject, contextPath, "newVal");

        assertNotNull(replaced);

        Object value = Json.query(replaced, contextPath);

        assertNotNull(value);
        assertEquals("newVal", value.toString());
    }

    @Test
    void replace_invalidPath() {
        List<String> contextPath = Lists.newArrayList("context", "schemaProperty", "type", "foo");

        JSONException e = assertThrows(JSONException.class, () -> Json.replace(jsonObject, contextPath, "newVal"));

        assertEquals("context path not found in json object", e.getMessage());
    }

    @Test
    void parse_success() {
        JSONObject parsed = Json.parse(jsonString);
        assertNotNull(parsed);
    }

    @Test
    void parse_invalid() {
        assertThrows(JSONException.class, ()-> Json.parse("{invalid json string}"));
    }

    @Test
    void query_success() {
        Object value = Json.query(jsonString, Lists.newArrayList("action"));

        assertNotNull(value);
        assertEquals("CHANGE_SCOPE", value);
    }

    @Test
    void query_fail_emptyPath() {
        JSONException e = assertThrows(JSONException.class, () -> Json.query(jsonString, Lists.newArrayList()));
        assertNotNull(e);
        assertEquals("contextPath must not be empty", e.getMessage());
    }

    @Test
    void query_fail_notFoundPath() {
        assertThrows(JSONException.class, () -> Json.query(jsonString, Lists.newArrayList("foo")));
    }
    @Test
    void query_fail_invalidPath() {
        assertThrows(JSONException.class, () -> Json.query(jsonString, Lists.newArrayList("action", "foo")));
    }

    @Test
    void unwrapValue_single() {
        Object value = Json.query(jsonString, Lists.newArrayList("action"));
        Object unwrapped = Json.unwrapValue(value, DataType.STRING);

        assertEquals("CHANGE_SCOPE", unwrapped);
        assertTrue(unwrapped instanceof String);
    }

    @Test
    @SuppressWarnings("unchecked")
    void unwrapValue_list() {
        Object value = Json.query(jsonString, Lists.newArrayList("context", "context"));
        Object unwrapped = Json.unwrapValue(value, DataType.STRING);

        assertTrue(unwrapped instanceof List);
        List<String> values = (List<String>) unwrapped;
        assertEquals(1, values.size());
        assertEquals("selection", values.get(0));
    }

    @Test
    void parse_generic() {
        String json = "{" +
                        "\"bool\": true," +
                        "\"number\": 2.0," +
                        "\"text\": \"Some text\"," +
                        "\"arr\": [1,2,3]," +
                        "\"arrText\":[\"foo\",\"bar\"]," +
                        "\"obj\":{\"foo\":\"bar\"}," +
                        "\"null\": null" +
                      "}";
        Map<String, String> map = Json.toMap(json);

        assertNotNull(map);
        assertEquals(6, map.size());
        assertEquals("true", map.get("bool"));
        assertEquals("2.0", map.get("number"));
        assertEquals("Some text", map.get("text"));
        assertEquals("[1,2,3]", map.get("arr"));
        assertEquals("[\"foo\",\"bar\"]", map.get("arrText"));
        assertEquals("{\"foo\":\"bar\"}", map.get("obj"));
    }

    @Test
    void parse_generic_invalidJson() {
        assertThrows(JSONException.class, () -> Json.toMap("invalid json input"));
        assertThrows(JSONException.class, () -> Json.toMap("{\"foo\":[\"bar\"}"));
        assertThrows(JSONException.class, () -> Json.toMap("{\"foo\":\"bar\""));
    }

    @Test
    void toJsonNode() {
        JsonNode jsonNode = Json.toJsonNode(jsonString);

        assertNotNull(jsonNode);
    }

    @Test
    void toJsonNode_error() {
        assertThrows(JSONException.class, () -> Json.toJsonNode("invalid json string {}"));
    }

    @Test
    void stringify() {
        Item item = new Item("watch", "a limited edition");

        String jsonString = Json.stringify(item);

        assertNotNull(jsonString);
        assertEquals("{\"name\":\"watch\",\"description\":\"a limited edition\"}", jsonString);
    }

    private static class Item {
        private final String name;
        private final String description;


        private Item(String name, String description) {
            this.name = name;
            this.description = description;
        }

        public String getName() {
            return name;
        }

        public String getDescription() {
            return description;
        }
    }
}
