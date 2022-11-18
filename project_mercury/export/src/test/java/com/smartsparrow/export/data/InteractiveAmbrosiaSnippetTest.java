package com.smartsparrow.export.data;

import static com.smartsparrow.export.stub.AmbrosiaSnippetStub.mockComponentSnippet;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.IOException;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockitoAnnotations;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

class InteractiveAmbrosiaSnippetTest {

    private InteractiveAmbrosiaSnippet interactiveAmbrosiaSnippet;
    private ComponentAmbrosiaSnippet componentAmbrosiaSnippetOne;
    private ComponentAmbrosiaSnippet componentAmbrosiaSnippetTwo;
    private static final ObjectMapper om = new ObjectMapper();

    static {
        om.setSerializationInclusion(JsonInclude.Include.NON_NULL);
    }

    private static final String interactiveSnippetString = "{\n" +
            "  \"$ambrosia\": \"aero:interactive:5be8d6e7-0a55-4a81-97d6-5a6d40b9ab9d:1.*\",\n" +
            "  \"$id\": \"0d045a93-43df-47b4-8c7b-43175ade7f63\",\n" +
            "  \"$workspaceId\": \"ee944c60-dbdc-4237-9dfb-4ee7fe88e7a9\",\n" +
            "  \"$projectId\": \"4aabe5be-2d81-41d5-b0b2-06e3fb1d00b1\",\n" +
            "  \"pluginPayload\":{\n" +
            "      \"pluginRepositoryPath\":\"http://www.sample.org/plugin/repository/path\",\n" +
            "      \"summary\":{}\n" +
            "  },\n" +
            "  \"config\": {\n" +
            "    \"title\": \"Mapping an activity\",\n" +
            "    \"components\": [\n" +
            "      {\n" +
            "        \"itemId\": \"770ed140-22ef-11eb-bd60-17acabf1297e\",\n" +
            "        \"localRef\": \"1\",\n" +
            "        \"pluginPayload\":{\n" +
            "           \"pluginRepositoryPath\":\"http://www.sample.org/plugin/repository/path\",\n" +
            "           \"summary\":{}\n" +
            "        }\n" +
            "      },\n" +
            "      {\n" +
            "        \"itemId\": \"1954d47c-4935-459c-a80b-2ed31c971bba\",\n" +
            "        \"localRef\": \"2\",\n" +
            "        \"pluginPayload\":{\n" +
            "           \"pluginRepositoryPath\":\"http://www.sample.org/plugin/repository/path\",\n" +
            "           \"summary\":{}\n" +
            "        }\n" +
            "      },\n" +
            "      {\n" +
            "        \"itemId\": \"a8feb0b1-dcd8-4c0a-acb0-c27ede42aa39\",\n" +
            "        \"localRef\": \"3\",\n" +
            "        \"pluginPayload\":{\n" +
            "           \"pluginRepositoryPath\":\"http://www.sample.org/plugin/repository/path\",\n" +
            "           \"summary\":{}\n" +
            "        }\n" +
            "      }\n" +
            "    ]\n" +
            "  }\n" +
            "}";

    private static final String componentTwo = "{\n" +
            "  \"$ambrosia\": \"aero:component:5be8d6e7-0a55-4a81-97d6-5a6d40b9ab9d:1.*\",\n" +
            "  \"$id\": \"1954d47c-4935-459c-a80b-2ed31c971bba\",\n" +
            "  \"config\": {\n" +
            "    \"title\": \"Mapping an activity\",\n" +
            "    \"props\": {}\n" +
            "  },\n" +
            "  \"pluginPayload\": {\n" +
            "    \"pluginRepositoryPath\": \"http://www.sample.org/plugin/repository/path\",\n" +
            "    \"summary\": {}\n" +
            "  }\n" +
            "}";

    @BeforeEach
    void setUp() throws IOException {
        MockitoAnnotations.initMocks(this);

        interactiveAmbrosiaSnippet = om.readValue(interactiveSnippetString, InteractiveAmbrosiaSnippet.class);
        componentAmbrosiaSnippetOne = mockComponentSnippet();
        componentAmbrosiaSnippetTwo = om.readValue(componentTwo, ComponentAmbrosiaSnippet.class);

    }

    @Test
    void reduce() throws JsonProcessingException {
        AmbrosiaSnippet reduced = interactiveAmbrosiaSnippet.reduce(componentAmbrosiaSnippetOne, componentAmbrosiaSnippetTwo);

        final String stringified = om.writeValueAsString(reduced);

        final String expected = "{" +
                "\"$ambrosia\":\"aero:interactive:5be8d6e7-0a55-4a81-97d6-5a6d40b9ab9d:1.*\"," +
                "\"$id\":\"0d045a93-43df-47b4-8c7b-43175ade7f63\"," +
                "\"$workspaceId\":\"ee944c60-dbdc-4237-9dfb-4ee7fe88e7a9\"," +
                "\"$projectId\":\"4aabe5be-2d81-41d5-b0b2-06e3fb1d00b1\"," +
                "\"config\":{" +
                    "\"title\":\"Mapping an activity\"," +
                    "\"components\":[{" +
                        "\"localRef\":\"1\"," +
                        "\"pluginPayload\":{" +
                            "\"pluginRepositoryPath\":\"http://www.sample.org/plugin/repository/path\"," +
                            "\"summary\":{}" +
                        "}," +
                        "\"$ambrosia\":\"aero:component:5be8d6e7-0a55-4a81-97d6-5a6d40b9ab9d:1.*\"," +
                        "\"$id\":\"770ed140-22ef-11eb-bd60-17acabf1297e\"," +
                        "\"config\":{" +
                            "\"title\":\"Mapping an activity\"," +
                            "\"props\":{}" +
                        "}" +
                "},{" +
                        "\"localRef\":\"2\"," +
                        "\"pluginPayload\":{" +
                            "\"pluginRepositoryPath\":\"http://www.sample.org/plugin/repository/path\"," +
                            "\"summary\":{}" +
                        "}," +
                        "\"$ambrosia\":\"aero:component:5be8d6e7-0a55-4a81-97d6-5a6d40b9ab9d:1.*\"," +
                        "\"$id\":\"1954d47c-4935-459c-a80b-2ed31c971bba\"," +
                        "\"config\":{" +
                            "\"title\":\"Mapping an activity\"," +
                            "\"props\":{}" +
                        "}" +
                "},{" +
                        "\"itemId\":\"a8feb0b1-dcd8-4c0a-acb0-c27ede42aa39\"," +
                        "\"localRef\":\"3\"," +
                        "\"pluginPayload\":{" +
                            "\"pluginRepositoryPath\":\"http://www.sample.org/plugin/repository/path\"," +
                            "\"summary\":{}" +
                        "}" +
                    "}]" +
                "}," +
                "\"pluginPayload\":{" +
                    "\"pluginRepositoryPath\":\"http://www.sample.org/plugin/repository/path\"," +
                    "\"summary\":{}" +
                "}" +
                "}";

        assertEquals(expected, stringified);
    }

}
