package com.smartsparrow.export.stub;

import java.io.IOException;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.smartsparrow.export.data.ActivityAmbrosiaSnippet;
import com.smartsparrow.export.data.ComponentAmbrosiaSnippet;
import com.smartsparrow.export.data.InteractiveAmbrosiaSnippet;
import com.smartsparrow.export.data.PathwayAmbrosiaSnippet;

public class AmbrosiaSnippetStub {

    private static final ObjectMapper om = new ObjectMapper();

    public static final String ACTIVITY_SNIPPET = "{" +
            "\"$ambrosia\": \"aero:activity:9417ac60-5f60-11ea-8e69-affb2224d7e8:0.*\"," +
            "\"$id\": \"fbc4f8d0-73bb-11ea-b2ed-8d9c059418aa\"," +
            "\"$workspaceId\": \"foo\"," +
            "\"$projectId\": \"bar\"," +
            "\"config\": {" +
                "\"props\": {\"foo\":\"bar\"}," +
                "\"chapters\": {" +
                    "\"pathwayId\": \"0d045a93-43df-47b4-8c7b-43175ade7f63\"," +
                    "\"pathwayType\": \"linear\"" +
                "}," +
                "\"components\": [{" +
                    "\"itemId\": \"770ed140-22ef-11eb-bd60-17acabf1297e\"," +
                    "\"localRef\": \"1\"," +
                    "  \"pluginPayload\": {\n" +
                    "    \"pluginRepositoryPath\": \"http://www.sample.org/plugin/repository/path\",\n" +
                    "    \"summary\": {}\n" +
                    "  }\n" +
                "}]" +
            "}," +
            "\"pluginPayload\":{" +
                "\"pluginRepositoryPath\":\"http://www.sample.org/plugin/repository/path\"," +
                "\"summary\":{}" +
            "}" +
            "}";

    public static final String INTERACTIVE_SNIPPET = "{" +
            "\"$ambrosia\": \"aero:interactive:9417ac60-5f60-11ea-8e69-affb2224d7e8:0.*\"," +
            "\"$id\": \"afe0b8a0-7f9b-11ea-af65-af7754595c61\"," +
            "\"$workspaceId\": \"foo\"," +
            "\"$projectId\": \"bar\"," +
            "\"config\": {\"props\": {}}}";

    public static final String PATHWAY_SNIPPET = "{" +
            "\"$ambrosia\": \"aero:pathway:linear\", " +
            "\"$id\": \"0d045a93-43df-47b4-8c7b-43175ade7f63\", " +
            "\"children\": [\"afe0b8a0-7f9b-11ea-af65-af7754595c61\"]}";

    public static final String COMPONENT_SNIPPET = "{\n" +
            "  \"$ambrosia\": \"aero:component:5be8d6e7-0a55-4a81-97d6-5a6d40b9ab9d:1.*\",\n" +
            "  \"$id\": \"770ed140-22ef-11eb-bd60-17acabf1297e\",\n" +
            "  \"config\": {\n" +
            "    \"title\": \"Mapping an activity\",\n" +
            "    \"props\": {}\n" +
            "  },\n" +
            "  \"pluginPayload\": {\n" +
            "    \"pluginRepositoryPath\": \"http://www.sample.org/plugin/repository/path\",\n" +
            "    \"summary\": {}\n" +
            "  }\n" +
            "}";

    public static ActivityAmbrosiaSnippet mockActivitySnippet() throws IOException {
        return om.readValue(ACTIVITY_SNIPPET, ActivityAmbrosiaSnippet.class);
    }

    public static InteractiveAmbrosiaSnippet mockInteractiveSnippet() throws IOException {
        return om.readValue(INTERACTIVE_SNIPPET, InteractiveAmbrosiaSnippet.class);
    }

    public static PathwayAmbrosiaSnippet mockPathwaySnippet() throws IOException {
        return om.readValue(PATHWAY_SNIPPET, PathwayAmbrosiaSnippet.class);
    }

    public static ComponentAmbrosiaSnippet mockComponentSnippet() throws IOException {
        return om.readValue(COMPONENT_SNIPPET, ComponentAmbrosiaSnippet.class);
    }
}
