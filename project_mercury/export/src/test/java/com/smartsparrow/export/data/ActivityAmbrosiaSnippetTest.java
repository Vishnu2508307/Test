package com.smartsparrow.export.data;

import static com.smartsparrow.export.stub.AmbrosiaSnippetStub.mockActivitySnippet;
import static com.smartsparrow.export.stub.AmbrosiaSnippetStub.mockComponentSnippet;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.IOException;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

class ActivityAmbrosiaSnippetTest {

    private static final ObjectMapper om = new ObjectMapper();
    static {
        om.setSerializationInclusion(JsonInclude.Include.NON_NULL);
    }

    private ActivityAmbrosiaSnippet activityAmbrosiaSnippet;
    private PathwayAmbrosiaSnippet pathwayAmbrosiaSnippet;
    private ComponentAmbrosiaSnippet componentAmbrosiaSnippet;

    private static final String pathwaySnippet = "{" +
            "\"$ambrosia\": \"aero:pathway:linear\", " +
            "\"$id\": \"0d045a93-43df-47b4-8c7b-43175ade7f63\", " +
            "\"children\": [" +
                "{" +
                    "\"$ambrosia\": \"aero:activity:9417ac60-5f60-11ea-8e69-affb2224d7e8:0.*\"," +
                    "\"$id\": \"fbc4f8d0-73bb-11ea-b2ed-8d9c059418aa\", " +
                    "\"$workspaceId\": \"foo\", " +
                    "\"$projectId\": \"bar\", " +
                    "\"config\":{\"props\": {}}" +
                "}" +
            "]}";

    @BeforeEach
    void setUp() throws IOException {
        activityAmbrosiaSnippet = mockActivitySnippet();
        pathwayAmbrosiaSnippet = om.readValue(pathwaySnippet, PathwayAmbrosiaSnippet.class);
        componentAmbrosiaSnippet = mockComponentSnippet();
    }

    @Test
    void reduce() throws JsonProcessingException {
        final AmbrosiaSnippet reduced = activityAmbrosiaSnippet.reduce(pathwayAmbrosiaSnippet, componentAmbrosiaSnippet);

        final String expected = "{" +
                "\"$ambrosia\":\"aero:activity:9417ac60-5f60-11ea-8e69-affb2224d7e8:0.*\"," +
                "\"$id\":\"fbc4f8d0-73bb-11ea-b2ed-8d9c059418aa\"," +
                "\"$workspaceId\":\"foo\"," +
                "\"$projectId\":\"bar\"," +
                "\"config\":{" +
                    "\"props\":{" +
                        "\"foo\":\"bar\"" +
                    "}," +
                    "\"chapters\":{" +
                        "\"$ambrosia\":\"aero:pathway:linear\"," +
                        "\"id\":\"0d045a93-43df-47b4-8c7b-43175ade7f63\"," +
                        "\"children\":[{" +
                            "\"$ambrosia\":\"aero:activity:9417ac60-5f60-11ea-8e69-affb2224d7e8:0.*\"," +
                            "\"$id\":\"fbc4f8d0-73bb-11ea-b2ed-8d9c059418aa\"," +
                            "\"$workspaceId\":\"foo\"," +
                            "\"$projectId\":\"bar\"," +
                            "\"config\":{" +
                                "\"props\":{}" +
                            "}" +
                        "}]" +
                    "}," +
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
                    "}]" +
                "}," +
                "\"pluginPayload\":{" +
                    "\"pluginRepositoryPath\":\"http://www.sample.org/plugin/repository/path\"," +
                    "\"summary\":{}" +
                "}" +
                "}";

        assertEquals(expected, om.writeValueAsString(reduced));
    }

}
