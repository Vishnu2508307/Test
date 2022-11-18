package com.smartsparrow.courseware.service;

import com.smartsparrow.plugin.service.SchemaValidationService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.mockito.InjectMocks;
import org.mockito.MockitoAnnotations;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;


public class SchemaValidationServiceTest {

    @InjectMocks
    SchemaValidationService schemaValidationService;

    private static String manifestSchemaWithAdditionalProps = "{\n" +
            "\t\"title\": {\n" +
            "      \"type\":\"text\"\n" +
            "    },\n" +
            "    \"description\":{\n" +
            "      \"type\": \"rich-text\",\n" +
            "      \"label\": \"items\"\n" +      //new prop
            "    },\n" +
            "    \"items\": {\n" +
            "      \"type\": \"list\",\n" +
            "      \"listType\": \"text\",\n" +
            "      \"label\": \"items\"\n" +
            "    },\n" +
            "    \"selection\": {\n" +
            "      \"type\": \"list\",\n" +
            "      \"listType\": \"list\",\n" +
            "      \"learnerEditable\": true,\n" +
            "      \"hidden\": true,\n" +          //new prop
            "      \"label\": \"selection\",\n" +
            "      \"items\": {\n" +
            "       \"type\": \"list\",\n" +
            "       \"listType\": \"text\",\n" +
            "       \"label\": \"items\"\n" +
            "    }\n" +
            "    },\n" +
            "  \"pathway\": {\n" +                //new prop
            "    \"type\": \"pathway\",\n" +
            "    \"hidden\": true,\n" +
            "    \"allowedCourseware\": [\"screen\"]\n" +
            "  }" +
            "}";

    private static String manifestSchemaWithChangedProps = "{\n" +
            "\t\"title\": {\n" +
            "      \"type\":\"text\"\n" +
            "    },\n" +
            "    \"description\":{\n" +
            "      \"type\": \"text\"\n" +  // changed prop
            "    },\n" +
            "    \"items\": {\n" +
            "      \"type\": \"list\",\n" +
            "      \"listType\": \"text\",\n" +
            "      \"label\": \"items\"\n" +
            "    },\n" +
            "    \"selection\": {\n" +
            "      \"type\": \"list\",\n" +
            "      \"listType\": \"list\",\n" +
            "      \"learnerEditable\": false,\n" +
            "      \"label\": \"selection\"\n" +
            "    }\n" +
            "}";

    private static String changedPropsOtherThanTypeField = "{\n" +
            "\t\"title\": {\n" +
            "      \"type\":\"text\"\n" +
            "    },\n" +
            "    \"description\":{\n" +
            "      \"type\": \"rich-text\"\n" +
            "    },\n" +
            "    \"items\": {\n" +
            "      \"type\": \"list\",\n" +
            "      \"listType\": \"rich-text\",\n" + // prop changed from text to rich-text
            "      \"label\": \"items\"\n" +
            "    },\n" +
            "    \"selection\": {\n" +
            "      \"type\": \"list\",\n" +
            "      \"listType\": \"list\",\n" +
            "      \"learnerEditable\": true,\n" +
            "      \"label\": \"selection\",\n" +
            "      \"items\": {\n" +
            "       \"type\": \"list\",\n" +
            "       \"listType\": \"text\",\n" +
            "       \"label\": \"items\"\n" +
            "    }\n" +
            "    }\n" +
            "}";

    private static String manifestSchemaWithMissingProps = "{\n" +
            "\t\"title\": {\n" +
            "      \"type\":\"text\"\n" +
            "    },\n" +
            "    \"description\":{\n" +
            "      \"type\": \"rich-text\"\n" +
            "    },\n" +
            "    \"items\": {\n" +
            "      \"listType\": \"text\",\n" +
            "      \"label\": \"items\"\n" + // here property  'type' is missing
            "    },\n" +
            "    \"selection\": {\n" +
            "      \"type\": \"list\",\n" +
            "      \"listType\": \"list\",\n" +
            "      \"learnerEditable\": true,\n" +
            "      \"label\": \"selection\",\n" +
            "      \"items\": {\n" +
            "       \"type\": \"list\",\n" +
            "       \"listType\": \"text\",\n" +
            "       \"label\": \"items\"\n" +
            "    }\n" +
            "    }\n" +
            "}";

    private static String manifestSchemaWithMissingElement = "{\n" +
            "\t\"title\": {\n" +
            "      \"type\":\"text\"\n" +
            "    },\n" +
            "    \"description\":{\n" +
            "      \"type\": \"rich-text\"\n" +
            "    },\n" +
            "    \"selection\": {\n" +
            "      \"type\": \"list\",\n" +
            "      \"listType\": \"list\",\n" +
            "      \"learnerEditable\": true,\n" +
            "      \"label\": \"selection\",\n" +
            "      \"items\": {\n" +
            "       \"type\": \"list\",\n" +
            "       \"listType\": \"text\",\n" +
            "       \"label\": \"items\"\n" +
            "    }\n" +
            "    }\n" +
            "}";

    private static String manifestSchemaWithMissingPropNotType = "{\n" +
            "\t\"title\": {\n" +
            "      \"type\":\"text\"\n" +
            "    },\n" +
            "    \"description\":{\n" +
            "      \"type\": \"rich-text\"\n" +
            "    },\n" +
            "    \"items\": {\n" +
            "      \"type\": \"list\",\n" +
            "      \"label\": \"items\"\n" + // here property 'listType' is missing
            "    },\n" +
            "    \"selection\": {\n" +
            "      \"type\": \"list\",\n" +
            "      \"listType\": \"list\",\n" +
            "      \"learnerEditable\": true,\n" +
            "      \"label\": \"selection\",\n" +
            "      \"items\": {\n" +
            "       \"type\": \"list\",\n" +
            "       \"listType\": \"text\",\n" +
            "       \"label\": \"items\"\n" +
            "    }\n" +
            "    }\n" +
            "}";

    private static String manifestChangedPropInSelectionNode= "{\n" +
            "\t\"title\": {\n" +
            "      \"type\":\"text\"\n" +
            "    },\n" +
            "    \"description\":{\n" +
            "      \"type\": \"rich-text\"\n" +
            "    },\n" +
            "    \"items\": {\n" +
            "      \"type\": \"list\",\n" +
            "      \"listType\": \"text\",\n" +
            "      \"label\": \"items\"\n" +
            "    },\n" +
            "    \"selection\": {\n" +
            "      \"type\": \"list\",\n" +
            "      \"listType\": \"list\",\n" +
            "      \"learnerEditable\": true,\n" +
            "      \"label\": \"selection\",\n" +
            "      \"items\": {\n" +
            "       \"type\": \"map\",\n" +  // changed prop from list to map
            "       \"listType\": \"rich-text\",\n" +
            "       \"label\": \"selection\"\n" +
            "    }\n" +
            "    }\n" +
            "}";

    private static String latestSchema = "{\n" +
            "\t\"title\": {\n" +
            "      \"type\":\"text\"\n" +
            "    },\n" +
            "    \"description\":{\n" +
            "      \"type\": \"rich-text\"\n" +
            "    },\n" +
            "    \"items\": {\n" +
            "      \"type\": \"list\",\n" +
            "      \"listType\": \"text\",\n" +
            "      \"label\": \"items\"\n" +
            "    },\n" +
            "    \"selection\": {\n" +
            "      \"type\": \"list\",\n" +
            "      \"listType\": \"list\",\n" +
            "      \"learnerEditable\": true,\n" +
            "      \"label\": \"selection\",\n" +
            "      \"items\": {\n" +
            "       \"type\": \"list\",\n" +
            "       \"listType\": \"text\",\n" +
            "       \"label\": \"items\"\n" +
            "    }\n" +
            "    }\n" +
            "}";

    @BeforeEach
    void setUp() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    void validateLatestSchemaAgainstManifestSchema_Success() {
        assertDoesNotThrow(() -> schemaValidationService.validateLatestSchemaAgainstManifestSchema(latestSchema, manifestSchemaWithAdditionalProps));
    }

    @Test
    void validateLatestSchemaAgainstManifestSchema_Success_ChangedPropNotTypeField() {
        assertDoesNotThrow(() -> schemaValidationService.validateLatestSchemaAgainstManifestSchema(latestSchema, changedPropsOtherThanTypeField));
    }

    @Test
    void validateLatestSchemaAgainstManifestSchema_Error() {
        Throwable t = assertThrows(IllegalArgumentException.class, () -> {
            schemaValidationService.validateLatestSchemaAgainstManifestSchema(latestSchema, manifestSchemaWithChangedProps);
        });
        assertTrue(t.getMessage().contains("A property of type description"));
    }
    @Test
    void validateLatestSchemaAgainstManifestSchema_Error_MissingAnyPropInManifest() {
        Throwable t = assertThrows(IllegalArgumentException.class, () -> {
            schemaValidationService.validateLatestSchemaAgainstManifestSchema(latestSchema, manifestSchemaWithMissingElement);
        });
        assertTrue(t.getMessage().contains("A property of type items"));
    }

    @Test
    void validateLatestSchemaAgainstManifestSchema_Error_InsideSelectionNode() {
        Throwable t = assertThrows(IllegalArgumentException.class, () -> {
            schemaValidationService.validateLatestSchemaAgainstManifestSchema(latestSchema, manifestChangedPropInSelectionNode);
        });
        assertTrue(t.getMessage().contains("A property of type selection"));
    }

    @Test
    void validateLatestSchemaAgainstManifestSchema_Error_MissingPropInManifest() {
        Throwable t = assertThrows(IllegalArgumentException.class, () -> {
            schemaValidationService.validateLatestSchemaAgainstManifestSchema(latestSchema, manifestSchemaWithMissingProps);
        });
        assertTrue(t.getMessage().contains("A property of type items"));
    }

    @Test
    void validateLatestSchemaAgainstManifestSchema_Error_MissingPropListTypeInManifest() {
        Throwable t = assertThrows(IllegalArgumentException.class, () -> {
            schemaValidationService.validateLatestSchemaAgainstManifestSchema(latestSchema, manifestSchemaWithMissingPropNotType);
        });
        assertTrue(t.getMessage().contains("A property of type items"));
    }
}
