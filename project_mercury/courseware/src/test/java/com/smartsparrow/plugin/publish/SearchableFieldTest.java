package com.smartsparrow.plugin.publish;

import com.fasterxml.jackson.databind.JsonNode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.MockitoAnnotations;

import java.io.IOException;
import static org.junit.jupiter.api.Assertions.assertNotNull;



public class SearchableFieldTest {

    @InjectMocks
    private SearchableField searchableField;


    @BeforeEach
    void setUp() {
        MockitoAnnotations.initMocks(this);
    }

    String manifestContent = "{\n" +
            "    \"id\": \"<insert_plugin_id>\",\n" +
            "    \"name\": \"<insert_plugin_name>\",\n" +
            "    \"description\": \"<insert_plugin_description>\",\n" +
            "    \"screenshots\": [\n" +
            "        \"img/mercury.jpg\"\n" +
            "    ],\n" +
            "    \"thumbnail\": \"img/thumbnail.png\",\n" +
            "    \"author\": \"John Kane\",\n" +
            "    \"email\": \"jkane@example.net\",\n" +
            "    \"version\": \"<insert_plugin_version>\",\n" +
            "    \"license\": \"MIT\",\n" +
            "    \"type\": \"<insert_plugin_type>\",\n" +
            "    \"configurationSchema\": \"config.schema.json\",\n" +
            "    \"views\": {\n" +
            "        \"LEARNER\": {\n" +
            "            \"contentType\": \"javascript\",\n" +
            "            \"entryPoint\": \"index.js\",\n" +
            "            \"publicDir\": \"\"\n" +
            "        }\n" +
            "    },\n" +
            "    \"websiteUrl\": \"<insert_website_url>\",\n" +
            "    \"supportUrl\": \"<insert_support_url>\",\n" +
            "    \"whatsNew\": \"<insert_whats_new>\",\n" +
            "    \"tags\": [\n" +
            "        \"smart\",\n" +
            "        \"plugin\"\n" +
            "    ],\n" +
            "    \"searchable\": [\n" +
            "        {\n" +
            "            \"contentType\": \"mcq\",\n" +
            "            \"summary\": \"title\",\n" +
            "            \"body\": \"selection\"\n" +
            "        },\n" +
            "        {\n" +
            "            \"contentType\": \"text\",\n" +
            "            \"body\": [\n" +
            "                \"options.foo\"\n" +
            "            ]\n" +
            "        },\n" +
            "        {\n" +
            "            \"contentType\": \"image\",\n" +
            "            \"body\": [\n" +
            "                \"cards.front-text\",\n" +
            "                \"cards.back-text\"\n" +
            "            ],\n" +
            "            \"source\": [\n" +
            "                \"cards.front-image\",\n" +
            "                \"cards.back-image\"\n" +
            "            ]\n" +
            "        },\n" +
            "        {\n" +
            "            \"contentType\": \"text\",\n" +
            "            \"summary\": [\n" +
            "                \"title\"\n" +
            "            ],\n" +
            "            \"body\": [\n" +
            "                \"stage.text\"\n" +
            "            ]\n" +
            "        }\n" +
            "    ],\n" +
            "    \"guide\": \"/guide.md\"\n" +
            "}\n";

    @Test
    void parse_success() throws IOException {
        searchableField = new SearchableField("searchable", manifestContent);
        JsonNode jsonNode = searchableField.parse(new PluginParserContext());
        assertNotNull(jsonNode);
    }
}
