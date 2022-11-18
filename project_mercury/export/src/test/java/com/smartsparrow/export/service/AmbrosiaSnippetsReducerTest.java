package com.smartsparrow.export.service;

import static com.smartsparrow.export.stub.AmbrosiaSnippetStub.ACTIVITY_SNIPPET;
import static com.smartsparrow.export.stub.AmbrosiaSnippetStub.COMPONENT_SNIPPET;
import static com.smartsparrow.export.stub.AmbrosiaSnippetStub.INTERACTIVE_SNIPPET;
import static com.smartsparrow.export.stub.AmbrosiaSnippetStub.PATHWAY_SNIPPET;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.assertj.core.util.Lists;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.smartsparrow.courseware.data.CoursewareElement;
import com.smartsparrow.courseware.data.CoursewareElementAncestry;
import com.smartsparrow.courseware.data.CoursewareElementType;
import com.smartsparrow.courseware.data.tree.CoursewareElementNode;
import com.smartsparrow.courseware.service.CoursewareService;
import com.smartsparrow.export.data.AmbrosiaSnippet;
import com.smartsparrow.export.data.ExportAmbrosiaSnippet;
import com.smartsparrow.export.data.ExportMetadata;
import com.smartsparrow.export.data.ExportSummary;
import com.smartsparrow.export.data.ExportType;
import com.smartsparrow.export.lang.AmbrosiaSnippetReducerException;
import com.smartsparrow.util.DateFormat;

import reactor.core.publisher.Mono;

class AmbrosiaSnippetsReducerTest {

    @InjectMocks
    private AmbrosiaSnippetsReducer reducer;

    @Mock
    private CoursewareService coursewareService;

    private final Map<UUID, ExportAmbrosiaSnippet> snippets = new HashMap<>();
    private CoursewareElementNode courseware;

    private static final UUID activityId = UUID.fromString("fbc4f8d0-73bb-11ea-b2ed-8d9c059418aa");
    private static final UUID componentId = UUID.fromString("770ed140-22ef-11eb-bd60-17acabf1297e");
    private static final UUID pathwayId = UUID.fromString("0d045a93-43df-47b4-8c7b-43175ade7f63");
    private static final UUID interactiveId = UUID.fromString("afe0b8a0-7f9b-11ea-af65-af7754595c61");
    private static final UUID exportId = UUID.fromString("886abc10-7489-11eb-8610-53fa399b8e5c");
    private static final ExportSummary exportSummary = new ExportSummary()
            .setElementId(activityId)
            .setElementType(CoursewareElementType.ACTIVITY)
            .setId(exportId)
            .setExportType(ExportType.EPUB_PREVIEW);


    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        // mock the snippets map
        snippets.put(activityId, new ExportAmbrosiaSnippet()
                .setAmbrosiaSnippet(ACTIVITY_SNIPPET));
        snippets.put(componentId, new ExportAmbrosiaSnippet()
                .setAmbrosiaSnippet(COMPONENT_SNIPPET));
        snippets.put(pathwayId, new ExportAmbrosiaSnippet()
                .setAmbrosiaSnippet(PATHWAY_SNIPPET));
        snippets.put(interactiveId, new ExportAmbrosiaSnippet()
                .setAmbrosiaSnippet(INTERACTIVE_SNIPPET));

        // mock the courseware with the following structure
        //         A
        //        / \
        //       C   P
        //            \
        //             I

        CoursewareElementNode componentNode = new CoursewareElementNode()
                .setElementId(componentId)
                .setType(CoursewareElementType.COMPONENT)
                .setParentId(activityId);

        CoursewareElementNode interactiveNode = new CoursewareElementNode()
                .setElementId(interactiveId)
                .setType(CoursewareElementType.INTERACTIVE)
                .setParentId(pathwayId);

        CoursewareElementNode pathwayNode = new CoursewareElementNode()
                .setElementId(pathwayId)
                .setType(CoursewareElementType.PATHWAY)
                .setParentId(activityId);

        courseware = new CoursewareElementNode()
                .setElementId(activityId)
                .setType(CoursewareElementType.ACTIVITY);

        pathwayNode.addChild(interactiveNode);
        courseware.addChild(componentNode);
        courseware.addChild(pathwayNode);

        // mock any element to be returned to the ancestry list to verify this is set correctly
        when(coursewareService.findCoursewareElementAncestry(CoursewareElement.from(exportSummary.getElementId(), exportSummary.getElementType())))
                .thenReturn(Mono.just(new CoursewareElementAncestry()
                .setElementId(exportSummary.getElementId())
                .setType(exportSummary.getElementType())
                .setAncestry(Lists.newArrayList(CoursewareElement.from(exportSummary.getElementId(), exportSummary.getElementType())))));
    }

    @Test
    void reduce_topLevelSnippetNotFound() {
        courseware = new CoursewareElementNode()
                .setElementId(UUID.randomUUID())
                .setType(CoursewareElementType.ACTIVITY);

        AmbrosiaSnippetReducerException e = assertThrows(AmbrosiaSnippetReducerException.class,
                () -> reducer.reduce(snippets, courseware, exportSummary));

        assertEquals("snippet not found for top level exported element", e.getMessage());
    }

    @Test
    void reduce_hasNewElements() throws IOException {
        courseware.addChild(new CoursewareElementNode()
                .setElementId(UUID.randomUUID())
                .setType(CoursewareElementType.COMPONENT)
                .setParentId(activityId));

        final AmbrosiaSnippet snippet = reducer.reduce(snippets, courseware, exportSummary)
                .block();

        assertNotNull(snippet);

        final ExportMetadata exportMetadata = snippet.getExportMetadata();

        assertNotNull(exportMetadata);
        assertNotNull(exportMetadata.getCompletedAt());
        assertNotNull(exportMetadata.getCompletedId());
        assertEquals(exportId, exportMetadata.getExportId());
        assertEquals(4, exportMetadata.getElementsExportedCount());

        final String reduced = Files.readString(reducer.serialize(snippet).toPath());

        final String expected = "{" +
                "\"$ambrosia\":\"aero:activity:9417ac60-5f60-11ea-8e69-affb2224d7e8:0.*\"," +
                "\"$id\":\"fbc4f8d0-73bb-11ea-b2ed-8d9c059418aa\"," +
                "\"$workspaceId\":\"foo\"," +
                "\"$projectId\":\"bar\"," +
                "\"exportMetadata\":{" +
                    "\"exportId\":\"886abc10-7489-11eb-8610-53fa399b8e5c\"," +
                    "\"startedAt\":\"Sun, 21 Feb 2021 21:12:41 GMT\"," +
                    "\"completedAt\":\"" + DateFormat.asRFC1123(snippet.getExportMetadata().getCompletedId()) + "\"," +
                    "\"elementsExportedCount\":4," +
                    "\"exportType\":\"EPUB_PREVIEW\"," +
                    "\"ancestry\":[{\"elementId\":\"fbc4f8d0-73bb-11ea-b2ed-8d9c059418aa\",\"elementType\":\"ACTIVITY\"}]" +
                "}," +
                "\"config\":{" +
                    "\"props\":{" +
                        "\"foo\":\"bar\"" +
                    "}," +
                    "\"chapters\":{" +
                        "\"$ambrosia\":\"aero:pathway:linear\"," +
                        "\"id\":\"0d045a93-43df-47b4-8c7b-43175ade7f63\"," +
                        "\"children\":[{" +
                            "\"$ambrosia\":\"aero:interactive:9417ac60-5f60-11ea-8e69-affb2224d7e8:0.*\"," +
                            "\"$id\":\"afe0b8a0-7f9b-11ea-af65-af7754595c61\"," +
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

        assertEquals(expected, reduced);
    }

    @Test
    void reduce_hasLessElements() throws IOException {
        CoursewareElementNode componentNode = new CoursewareElementNode()
                .setElementId(componentId)
                .setType(CoursewareElementType.COMPONENT)
                .setParentId(activityId);

        CoursewareElementNode pathwayNode = new CoursewareElementNode()
                .setElementId(pathwayId)
                .setType(CoursewareElementType.PATHWAY)
                .setParentId(activityId);

        courseware = new CoursewareElementNode()
                .setElementId(activityId)
                .setType(CoursewareElementType.ACTIVITY);

        courseware.addChild(componentNode);
        courseware.addChild(pathwayNode);

        final AmbrosiaSnippet snippet = reducer.reduce(snippets, courseware, exportSummary)
                .block();

        assertNotNull(snippet);

        final ExportMetadata exportMetadata = snippet.getExportMetadata();

        assertNotNull(exportMetadata);
        assertNotNull(exportMetadata.getCompletedAt());
        assertNotNull(exportMetadata.getCompletedId());
        assertEquals(exportId, exportMetadata.getExportId());
        assertEquals(4, exportMetadata.getElementsExportedCount());

        final String reduced = Files.readString(reducer.serialize(snippet).toPath());


        final String expected = "{" +
                "\"$ambrosia\":\"aero:activity:9417ac60-5f60-11ea-8e69-affb2224d7e8:0.*\"," +
                "\"$id\":\"fbc4f8d0-73bb-11ea-b2ed-8d9c059418aa\"," +
                "\"$workspaceId\":\"foo\"," +
                "\"$projectId\":\"bar\"," +
                "\"exportMetadata\":{" +
                    "\"exportId\":\"886abc10-7489-11eb-8610-53fa399b8e5c\"," +
                    "\"startedAt\":\"Sun, 21 Feb 2021 21:12:41 GMT\"," +
                    "\"completedAt\":\"" + DateFormat.asRFC1123(snippet.getExportMetadata().getCompletedId()) + "\"," +
                    "\"elementsExportedCount\":4," +
                    "\"exportType\":\"EPUB_PREVIEW\"," +
                    "\"ancestry\":[{\"elementId\":\"fbc4f8d0-73bb-11ea-b2ed-8d9c059418aa\",\"elementType\":\"ACTIVITY\"}]" +
                "}," +
                "\"config\":{" +
                    "\"props\":{" +
                        "\"foo\":\"bar\"" +
                    "}," +
                    "\"chapters\":{" +
                        "\"$ambrosia\":\"aero:pathway:linear\"," +
                        "\"id\":\"0d045a93-43df-47b4-8c7b-43175ade7f63\"," +
                        "\"children\":[\"afe0b8a0-7f9b-11ea-af65-af7754595c61\"]" +
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

        assertEquals(expected, reduced);
    }

    @Test
    void reduce_success() throws IOException {
        final UUID pathwayTwoId = UUID.randomUUID();
        final UUID activityTwoId = UUID.randomUUID();
        final UUID componentIdTwo = UUID.randomUUID();
        final UUID componentThreeId = UUID.randomUUID();
        final UUID interactiveTwoId = UUID.randomUUID();
        final UUID interactiveThreeId = UUID.randomUUID();

        final String pathwaySnippet = "{" +
                "\"$ambrosia\": \"aero:pathway:linear\", " +
                        "\"$id\": \"0d045a93-43df-47b4-8c7b-43175ade7f63\", " +
                        "\"children\": [\"afe0b8a0-7f9b-11ea-af65-af7754595c61\", \"" + activityTwoId + "\"]}";

        final String interactiveSnippet = "{" +
                "\"$ambrosia\": \"aero:interactive:9417ac60-5f60-11ea-8e69-affb2224d7e8:0.*\"," +
                        "\"$id\": \"afe0b8a0-7f9b-11ea-af65-af7754595c61\"," +
                        "\"$workspaceId\": \"foo\"," +
                        "\"$projectId\": \"bar\"," +
                        "\"config\": {" +
                            "\"props\": {}," +
                            "\"components\": [{" +
                                "\"itemId\": \""+ componentIdTwo +"\"," +
                                "\"localRef\": \"2\"" +
                            "}]" +
                        "}}";

        final String componentTwoSnippet = "{\n" +
        "  \"$ambrosia\": \"aero:component:5be8d6e7-0a55-4a81-97d6-5a6d40b9ab9d:1.*\",\n" +
                "  \"$id\": \"" + componentIdTwo + "\",\n" +
                "  \"pluginPayload\": {\n" +
                "       \"pluginRepositoryPath\": \"http://www.sample.org/plugin/repository/path\",\n" +
                "       \"summary\":{}\n" +
                "  },\n" +
                "  \"config\": {\n" +
                "    \"title\": \"component 2\",\n" +
                "    \"props\": {}\n" +
                "  }\n" +
                "}";

        final String componentThreeSnippet = "{\n" +
                "  \"$ambrosia\": \"aero:component:5be8d6e7-0a55-4a81-97d6-5a6d40b9ab9d:1.*\",\n" +
                "  \"$id\": \"" + componentThreeId + "\",\n" +
                "  \"config\": {\n" +
                "    \"title\": \"component 3\",\n" +
                "    \"props\": {}\n" +
                "  },\n" +
                "  \"pluginPayload\": {\n" +
                "       \"pluginRepositoryPath\": \"http://www.sample.org/plugin/repository/path\",\n" +
                "       \"summary\":{}\n" +
                "  }\n" +
                "}";

        final String activityTwoSnippet = "{" +
                "\"$ambrosia\": \"aero:activity:9417ac60-5f60-11ea-8e69-affb2224d7e8:0.*\"," +
                "\"$id\": \""+ activityTwoId +"\"," +
                "\"$workspaceId\": \"foo\"," +
                "\"$projectId\": \"bar\"," +
                    "\"config\": {" +
                        "\"props\": {\"foo\":\"bar\"}," +
                        "\"units\": {" +
                            "\"pathwayId\": \"" + pathwayTwoId + "\"," +
                            "\"pathwayType\": \"linear\"" +
                        "}," +
                        "\"components\": [{" +
                            "\"itemId\": \""+ componentThreeId +"\"," +
                            "\"localRef\": \"1\"" +
                        "}]" +
                    "}}";

        final String pathwayTwoSnippet = "{" +
                "\"$ambrosia\": \"aero:pathway:linear\", " +
                "\"$id\": \""+ pathwayTwoId +"\", " +
                "\"children\": [\""+ interactiveTwoId +"\", \"" + interactiveThreeId + "\"]}";

        final String interactiveTwoSnippet = "{" +
                "\"$ambrosia\": \"aero:interactive:9417ac60-5f60-11ea-8e69-affb2224d7e8:0.*\"," +
                        "\"$id\": \""+ interactiveTwoId +"\"," +
                        "\"$workspaceId\": \"foo\"," +
                        "\"$projectId\": \"bar\"," +
                        "\"config\": {" +
                        "\"props\": {}" +
                        "}};";

        final String interactiveThreeSnippet = "{" +
                "\"$ambrosia\": \"aero:interactive:9417ac60-5f60-11ea-8e69-affb2224d7e8:0.*\"," +
                        "\"$id\": \""+ interactiveThreeId +"\"," +
                        "\"$workspaceId\": \"foo\"," +
                        "\"$projectId\": \"bar\"," +
                        "\"config\": {" +
                        "\"props\": {}" +
                        "}};";


        snippets.put(activityId, new ExportAmbrosiaSnippet()
                .setAmbrosiaSnippet(ACTIVITY_SNIPPET));
        snippets.put(componentId, new ExportAmbrosiaSnippet()
                .setAmbrosiaSnippet(COMPONENT_SNIPPET));
        snippets.put(pathwayId, new ExportAmbrosiaSnippet()
                .setAmbrosiaSnippet(pathwaySnippet));
        snippets.put(interactiveId, new ExportAmbrosiaSnippet()
                .setAmbrosiaSnippet(interactiveSnippet));
        snippets.put(componentIdTwo, new ExportAmbrosiaSnippet()
                .setAmbrosiaSnippet(componentTwoSnippet));
        snippets.put(componentThreeId, new ExportAmbrosiaSnippet()
                .setAmbrosiaSnippet(componentThreeSnippet));
        snippets.put(activityTwoId, new ExportAmbrosiaSnippet()
                .setAmbrosiaSnippet(activityTwoSnippet));
        snippets.put(pathwayTwoId, new ExportAmbrosiaSnippet()
                .setAmbrosiaSnippet(pathwayTwoSnippet));
        snippets.put(interactiveTwoId, new ExportAmbrosiaSnippet()
                .setAmbrosiaSnippet(interactiveTwoSnippet));
        snippets.put(interactiveThreeId, new ExportAmbrosiaSnippet()
                .setAmbrosiaSnippet(interactiveThreeSnippet));


        // mock the courseware with the following structure
        //         A
        //        / \
        //       C   P
        //          / \
        //         I   A
        //        /   / \
        //       C   C   P
        //              / \
        //             I   I

        CoursewareElementNode componentOne = new CoursewareElementNode()
                .setElementId(componentId)
                .setType(CoursewareElementType.COMPONENT);

        CoursewareElementNode componentTwo = new CoursewareElementNode()
                .setElementId(componentIdTwo)
                .setType(CoursewareElementType.COMPONENT);

        CoursewareElementNode componentThree = new CoursewareElementNode()
                .setElementId(componentThreeId)
                .setType(CoursewareElementType.COMPONENT);

        CoursewareElementNode pathwayOne = new CoursewareElementNode()
                .setElementId(pathwayId)
                .setType(CoursewareElementType.PATHWAY);

        CoursewareElementNode pathwayTwo = new CoursewareElementNode()
                .setElementId(pathwayTwoId)
                .setType(CoursewareElementType.PATHWAY);

        CoursewareElementNode interactiveOne = new CoursewareElementNode()
                .setElementId(interactiveId)
                .setType(CoursewareElementType.INTERACTIVE);

        CoursewareElementNode interactiveTwo = new CoursewareElementNode()
                .setElementId(interactiveTwoId)
                .setType(CoursewareElementType.INTERACTIVE);

        CoursewareElementNode interactiveThree = new CoursewareElementNode()
                .setElementId(interactiveThreeId)
                .setType(CoursewareElementType.INTERACTIVE);

        CoursewareElementNode activityTwo = new CoursewareElementNode()
                .setElementId(activityTwoId)
                .setType(CoursewareElementType.ACTIVITY);

        courseware = new CoursewareElementNode()
                .setElementId(activityId)
                .setType(CoursewareElementType.ACTIVITY);

        pathwayTwo.addChild(interactiveTwo);
        pathwayTwo.addChild(interactiveThree);

        activityTwo.addChild(componentThree);
        activityTwo.addChild(pathwayTwo);

        interactiveOne.addChild(componentTwo);
        pathwayOne.addChild(interactiveOne);
        pathwayOne.addChild(activityTwo);

        courseware.addChild(componentOne)
                .addChild(pathwayOne);

        // mock an empty ancestry list to verify this is set correctly
        when(coursewareService.findCoursewareElementAncestry(CoursewareElement.from(exportSummary.getElementId(), exportSummary.getElementType())))
                .thenReturn(Mono.just(new CoursewareElementAncestry()
                        .setElementId(exportSummary.getElementId())
                        .setType(exportSummary.getElementType())
                        .setAncestry(new ArrayList<>())));

        final AmbrosiaSnippet snippet = reducer.reduce(snippets, courseware, exportSummary)
                .block();

        assertNotNull(snippet);

        final ExportMetadata exportMetadata = snippet.getExportMetadata();

        assertNotNull(exportMetadata);
        assertNotNull(exportMetadata.getCompletedAt());
        assertNotNull(exportMetadata.getCompletedId());
        assertEquals(exportId, exportMetadata.getExportId());
        assertEquals(10, exportMetadata.getElementsExportedCount());

        final String reduced = Files.readString(reducer.serialize(snippet).toPath());


        final String expected = "{" +
                "\"$ambrosia\":\"aero:activity:9417ac60-5f60-11ea-8e69-affb2224d7e8:0.*\"," +
                "\"$id\":\"fbc4f8d0-73bb-11ea-b2ed-8d9c059418aa\"," +
                "\"$workspaceId\":\"foo\"," +
                "\"$projectId\":\"bar\"," +
                "\"exportMetadata\":{" +
                    "\"exportId\":\"886abc10-7489-11eb-8610-53fa399b8e5c\"," +
                    "\"startedAt\":\"Sun, 21 Feb 2021 21:12:41 GMT\"," +
                    "\"completedAt\":\"" + DateFormat.asRFC1123(snippet.getExportMetadata().getCompletedId()) + "\"," +
                    "\"elementsExportedCount\":10," +
                    "\"exportType\":\"EPUB_PREVIEW\"," +
                    "\"ancestry\":[]" +
                "}," +
                "\"config\":{" +
                    "\"props\":{" +
                        "\"foo\":\"bar\"" +
                    "}," +
                    "\"chapters\":{" +
                        "\"$ambrosia\":\"aero:pathway:linear\"," +
                        "\"id\":\"0d045a93-43df-47b4-8c7b-43175ade7f63\"," +
                        "\"children\":[{" +
                            "\"$ambrosia\":\"aero:interactive:9417ac60-5f60-11ea-8e69-affb2224d7e8:0.*\"," +
                            "\"$id\":\"afe0b8a0-7f9b-11ea-af65-af7754595c61\"," +
                            "\"$workspaceId\":\"foo\"," +
                            "\"$projectId\":\"bar\"," +
                            "\"config\":{" +
                                "\"props\":{}," +
                                "\"components\":[{" +
                                    "\"localRef\":\"2\"," +
                                    "\"$ambrosia\":\"aero:component:5be8d6e7-0a55-4a81-97d6-5a6d40b9ab9d:1.*\"," +
                                    "\"$id\":\""+ componentIdTwo +"\"," +
                                    "\"config\":{" +
                                        "\"title\":\"component 2\"," +
                                        "\"props\":{}" +
                                    "}," +
                                    "\"pluginPayload\":{" +
                                        "\"pluginRepositoryPath\":\"http://www.sample.org/plugin/repository/path\"," +
                                        "\"summary\":{}" +
                                    "}" +
                                "}]" +
                            "}" +
                        "},{" +
                            "\"$ambrosia\":\"aero:activity:9417ac60-5f60-11ea-8e69-affb2224d7e8:0.*\"," +
                            "\"$id\":\""+ activityTwoId +"\"," +
                            "\"$workspaceId\":\"foo\"," +
                            "\"$projectId\":\"bar\"," +
                            "\"config\":{" +
                                "\"props\":{" +
                                    "\"foo\":\"bar\"" +
                                "}," +
                                "\"units\":{" +
                                    "\"$ambrosia\":\"aero:pathway:linear\"," +
                                    "\"id\":\""+ pathwayTwoId +"\"," +
                                    "\"children\":[{" +
                                        "\"$ambrosia\":\"aero:interactive:9417ac60-5f60-11ea-8e69-affb2224d7e8:0.*\"," +
                                        "\"$id\":\""+ interactiveTwoId +"\"," +
                                        "\"$workspaceId\":\"foo\"," +
                                        "\"$projectId\":\"bar\"," +
                                        "\"config\":{" +
                                            "\"props\":{}" +
                                        "}" +
                                    "},{" +
                                        "\"$ambrosia\":\"aero:interactive:9417ac60-5f60-11ea-8e69-affb2224d7e8:0.*\"," +
                                        "\"$id\":\""+ interactiveThreeId +"\"," +
                                        "\"$workspaceId\":\"foo\"," +
                                        "\"$projectId\":\"bar\"," +
                                        "\"config\":{" +
                                            "\"props\":{}" +
                                        "}" +
                                    "}]" +
                                "}," +
                                "\"components\":[{" +
                                    "\"localRef\":\"1\"," +
                                    "\"$ambrosia\":\"aero:component:5be8d6e7-0a55-4a81-97d6-5a6d40b9ab9d:1.*\"," +
                                    "\"$id\":\""+ componentThreeId +"\"," +
                                    "\"config\":{" +
                                        "\"title\":\"component 3\"," +
                                        "\"props\":{}" +
                                    "}," +
                                    "\"pluginPayload\":{" +
                                        "\"pluginRepositoryPath\":\"http://www.sample.org/plugin/repository/path\"," +
                                        "\"summary\":{}" +
                                    "}" +
                                "}]" +
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

        assertEquals(expected, reduced);
    }

    @Test
    void reduce_exportingInteractive() throws IOException {

        final UUID componentIdTwo = UUID.randomUUID();

        final String interactiveSnippet = "{" +
                "\"$ambrosia\": \"aero:interactive:9417ac60-5f60-11ea-8e69-affb2224d7e8:0.*\"," +
                "\"$id\": \"afe0b8a0-7f9b-11ea-af65-af7754595c61\"," +
                "\"$workspaceId\": \"foo\"," +
                "\"$projectId\": \"bar\"," +
                "\"config\": {" +
                    "\"props\": {}," +
                    "\"components\": [{" +
                        "\"itemId\": \""+ componentIdTwo +"\"," +
                        "\"pluginPayload\":{" +
                            "\"pluginRepositoryPath\":\"http://www.sample.org/plugin/repository/path\"," +
                            "\"summary\":{}" +
                        "},\n" +
                        "\"localRef\": \"2\"" +
                    "}," +
                    "{" +
                        "\"itemId\": \"" + componentId + "\"," +
                        "\"pluginPayload\":{" +
                            "\"pluginRepositoryPath\":\"http://www.sample.org/plugin/repository/path\"," +
                            "\"summary\":{}" +
                        "}," +
                        "\"localRef\": \"3\"" +
                    "}]" +
                "}}";

        final String componentTwoSnippet = "{\n" +
                "  \"$ambrosia\": \"aero:component:5be8d6e7-0a55-4a81-97d6-5a6d40b9ab9d:1.*\",\n" +
                "  \"$id\": \"" + componentIdTwo + "\",\n" +
                "  \"pluginPayload\":{\n" +
                "       \"pluginRepositoryPath\":\"http://www.sample.org/plugin/repository/path\",\n" +
                "       \"summary\":{}\n" +
                "  },\n" +
                "  \"config\": {\n" +
                "    \"title\": \"component 2\",\n" +
                "    \"props\": {}\n" +
                "  }\n" +
                "}";

        final HashMap<UUID, ExportAmbrosiaSnippet> snippetsMap = new HashMap<>();

        snippetsMap.put(componentId, new ExportAmbrosiaSnippet()
                .setAmbrosiaSnippet(COMPONENT_SNIPPET));
        snippetsMap.put(interactiveId, new ExportAmbrosiaSnippet()
                .setAmbrosiaSnippet(interactiveSnippet));
        snippetsMap.put(componentIdTwo, new ExportAmbrosiaSnippet()
                .setAmbrosiaSnippet(componentTwoSnippet));

        CoursewareElementNode componentOne = new CoursewareElementNode()
                .setElementId(componentId)
                .setType(CoursewareElementType.COMPONENT);

        CoursewareElementNode componentTwo = new CoursewareElementNode()
                .setElementId(componentIdTwo)
                .setType(CoursewareElementType.COMPONENT);

        courseware = new CoursewareElementNode()
                .setElementId(interactiveId)
                .setType(CoursewareElementType.INTERACTIVE);

        courseware.addChild(componentOne)
                .addChild(componentTwo);

        final AmbrosiaSnippet snippet = reducer.reduce(snippetsMap, courseware, exportSummary)
                .block();

        assertNotNull(snippet);

        final ExportMetadata interactiveExportMetadata = snippet.getExportMetadata();

        assertNotNull(interactiveExportMetadata);
        assertNotNull(interactiveExportMetadata.getCompletedAt());
        assertNotNull(interactiveExportMetadata.getCompletedId());
        assertEquals(exportId, interactiveExportMetadata.getExportId());
        assertEquals(3, interactiveExportMetadata.getElementsExportedCount());

        final String reduced = Files.readString(reducer.serialize(snippet).toPath());


        final String expected = "{" +
                "\"$ambrosia\":\"aero:interactive:9417ac60-5f60-11ea-8e69-affb2224d7e8:0.*\"," +
                "\"$id\":\"afe0b8a0-7f9b-11ea-af65-af7754595c61\"," +
                "\"$workspaceId\":\"foo\"," +
                "\"$projectId\":\"bar\"," +
                "\"exportMetadata\":{" +
                    "\"exportId\":\"886abc10-7489-11eb-8610-53fa399b8e5c\"," +
                    "\"startedAt\":\"Sun, 21 Feb 2021 21:12:41 GMT\"," +
                    "\"completedAt\":\"" + DateFormat.asRFC1123(snippet.getExportMetadata().getCompletedId()) + "\"," +
                    "\"elementsExportedCount\":3," +
                    "\"exportType\":\"EPUB_PREVIEW\"," +
                    "\"ancestry\":[{\"elementId\":\"fbc4f8d0-73bb-11ea-b2ed-8d9c059418aa\",\"elementType\":\"ACTIVITY\"}]" +
                "}," +
                "\"config\":{" +
                    "\"props\":{}," +
                    "\"components\":[{" +
                        "\"pluginPayload\":{" +
                            "\"pluginRepositoryPath\":\"http://www.sample.org/plugin/repository/path\"," +
                            "\"summary\":{}" +
                        "}," +
                        "\"localRef\":\"2\"," +
                        "\"$ambrosia\":\"aero:component:5be8d6e7-0a55-4a81-97d6-5a6d40b9ab9d:1.*\"," +
                        "\"$id\":\"" + componentIdTwo + "\"," +
                        "\"config\":{" +
                            "\"title\":\"component 2\"," +
                            "\"props\":{}" +
                        "}" +
                    "},{" +
                        "\"pluginPayload\":{" +
                            "\"pluginRepositoryPath\":\"http://www.sample.org/plugin/repository/path\"," +
                            "\"summary\":{}" +
                        "}," +
                        "\"localRef\":\"3\"," +
                        "\"$ambrosia\":\"aero:component:5be8d6e7-0a55-4a81-97d6-5a6d40b9ab9d:1.*\"," +
                        "\"$id\":\"770ed140-22ef-11eb-bd60-17acabf1297e\"," +
                        "\"config\":{" +
                            "\"title\":\"Mapping an activity\"," +
                            "\"props\":{}" +
                        "}" +
                    "}]" +
                "}}";

        assertEquals(expected, reduced);
    }
}
