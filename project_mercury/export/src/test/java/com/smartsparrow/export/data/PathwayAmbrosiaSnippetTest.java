package com.smartsparrow.export.data;

import static com.smartsparrow.export.stub.AmbrosiaSnippetStub.mockInteractiveSnippet;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.IOException;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

class PathwayAmbrosiaSnippetTest {

    private static final ObjectMapper om = new ObjectMapper();

    static {
        om.setSerializationInclusion(JsonInclude.Include.NON_NULL);
    }

    private PathwayAmbrosiaSnippet pathwayAmbrosiaSnippet;
    private ActivityAmbrosiaSnippet activityAmbrosiaSnippet;
    private InteractiveAmbrosiaSnippet interactiveAmbrosiaSnippet;

    private static final String pathwayString = "{" +
            "\"$ambrosia\": \"aero:pathway:linear\"," +
            "\"$id\": \"0d045a93-43df-47b4-8c7b-43175ade7f63\"," +
            "\"config\": {" +
                "\"props\": {}" +
            "},\n" +
            "\"children\": [" +
                "\"fbc4f8d0-73bb-11ea-b2ed-8d9c059418aa\"," +
                "\"3b0381c0-633a-11ea-a852-e1cd1b741a1d\"," +
                "\"afe0b8a0-7f9b-11ea-af65-af7754595c61\"" +
            "]}";

    private static final String activityString = "{" +
                                    "\"$ambrosia\": \"aero:activity:9417ac60-5f60-11ea-8e69-affb2224d7e8:0.*\"," +
                                    "\"$id\": \"fbc4f8d0-73bb-11ea-b2ed-8d9c059418aa\"," +
                                    "\"$workspaceId\": \"foo\"," +
                                    "\"$projectId\": \"bar\"," +
                                    "\"config\": {\"props\": {}}}";

    @BeforeEach
    void setUp() throws IOException {
        pathwayAmbrosiaSnippet = om.readValue(pathwayString, PathwayAmbrosiaSnippet.class);
        activityAmbrosiaSnippet = om.readValue(activityString, ActivityAmbrosiaSnippet.class);
        interactiveAmbrosiaSnippet = mockInteractiveSnippet();
    }

    @Test
    void reduce() throws JsonProcessingException {
        final AmbrosiaSnippet reduced = pathwayAmbrosiaSnippet.reduce(activityAmbrosiaSnippet, interactiveAmbrosiaSnippet);

        final String expected = "{" +
                "\"$ambrosia\":\"aero:pathway:linear\"," +
                "\"$id\":\"0d045a93-43df-47b4-8c7b-43175ade7f63\"," +
                "\"children\":[{" +
                        "\"$ambrosia\":\"aero:activity:9417ac60-5f60-11ea-8e69-affb2224d7e8:0.*\"," +
                        "\"$id\":\"fbc4f8d0-73bb-11ea-b2ed-8d9c059418aa\"," +
                        "\"$workspaceId\":\"foo\"," +
                        "\"$projectId\":\"bar\"," +
                        "\"config\":{" +
                            "\"props\":{}" +
                        "}" +
                    "}," +
                    "\"3b0381c0-633a-11ea-a852-e1cd1b741a1d\"," +
                    "{" +
                        "\"$ambrosia\":\"aero:interactive:9417ac60-5f60-11ea-8e69-affb2224d7e8:0.*\"," +
                        "\"$id\":\"afe0b8a0-7f9b-11ea-af65-af7754595c61\"," +
                        "\"$workspaceId\":\"foo\"," +
                        "\"$projectId\":\"bar\"," +
                        "\"config\":{" +
                            "\"props\":{}" +
                        "}" +
                "}]," +
                "\"config\":{\"props\":{}}}";

        assertEquals(expected, om.writeValueAsString(reduced));
    }

}