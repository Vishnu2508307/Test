package com.smartsparrow.plugin.service;

import java.util.Iterator;
import java.util.Map;
import java.util.Objects;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.fasterxml.jackson.databind.JsonNode;
import com.smartsparrow.plugin.data.PluginType;
import com.smartsparrow.plugin.lang.S3BucketLoadFileException;
import com.smartsparrow.plugin.schema.SchemaValidator;
import com.smartsparrow.util.Json;

@Singleton
public class SchemaValidationService {

    private static final String PLUGIN_COURSEWARE_SCHEMA = "plugin.courseware.json";
    private static final String PLUGIN_COMPONENT_SCHEMA = "plugin.component.json";
    private static final String PLUGIN_GENERIC_SCHEMA = "plugin.generic.json";

    private final S3Bucket s3Bucket;

    @Inject
    public SchemaValidationService(S3Bucket s3Bucket) {
        this.s3Bucket = s3Bucket;
    }

    /**
     * Validate a json string against the schema file provided as argument. The file is currently loaded from an s3
     * bucket
     *
     * @param json the json to validate
     * @param schemaFileName the schema file to validate the json against
     * @throws S3BucketLoadFileException when failing to load and read the content of the schema file
     * @throws org.everit.json.schema.ValidationException when the json is invalid
     */
    public void validateWithFile(final String json, final String schemaFileName) throws S3BucketLoadFileException {
        String schema = s3Bucket.loadSchemaFile(schemaFileName);
        validate(json, schema);
    }

    /**
     * Validate the supplied json content against the json schema. Allows to inject a <b>$schema</b> entry to the
     * provided json schema
     *
     * @param json a string representation of a json object to validate
     * @param schema a string representation of a json schema
     */
    public void validate(final String json, final String schema) {
        buildValidatorFor(json, schema)
                .validate();
    }

    /**
     * Build a schema validator that can be reused to compute the validation of a json content against a schema
     *
     * @param json a string representation of a json object to validate
     * @param schema a string representation of a json schema
     * @return a schema validator object
     */
    private SchemaValidator buildValidatorFor(final String json, final String schema) {
        return new SchemaValidator.Builder()
                .forJson(json)
                .withSchema(schema)
                .build();
    }

    /**
     * Get the plugin schema file name depending upon the type. {@link PluginType#PATHWAY} are currently not supported
     * as plugin. When they will the {@link SchemaValidationService#PLUGIN_GENERIC_SCHEMA} should be used for
     * validating their schema.
     *
     * @param type the plugin type to find the schema file name for
     * @return the name of the schema file
     * @throws UnsupportedOperationException when the type is not supported by any plugin schema
     */
    public String getSchemaFileName(final PluginType type) {
        switch (type) {
            case COURSE:
                return PLUGIN_COURSEWARE_SCHEMA;
            case UNIT:
                return PLUGIN_COURSEWARE_SCHEMA;
            case LESSON:
                return PLUGIN_COURSEWARE_SCHEMA;
            case PATHWAY:
                throw new UnsupportedOperationException("Pathway plugin currently not supported");
            case SCREEN:
                return PLUGIN_COURSEWARE_SCHEMA;
            case COMPONENT:
                return PLUGIN_COMPONENT_SCHEMA;
            default:
                throw new UnsupportedOperationException(String.format("unsupported type %s", type));
        }
    }

    /**
     * Validate plugin manifest schema against latest previous schema from the db
     *
     * @param previousConfigSchema the plugin manifest latest config schema string content from db
     * @param manifestConfigSchema the plugin manifest config schema string content
     */
    public void validateLatestSchemaAgainstManifestSchema(final String previousConfigSchema, final String manifestConfigSchema) {
        JsonNode manifestSchemaJsonNode = Json.toJsonNode(manifestConfigSchema);
        JsonNode previousSchemaJsonNode = Json.toJsonNode(previousConfigSchema);
        Iterator<Map.Entry<String, JsonNode>> previousSchemaFields = previousSchemaJsonNode.fields();

        while (previousSchemaFields.hasNext()) {
            Map.Entry<String, JsonNode> next = previousSchemaFields.next();
            String field = next.getKey();
            JsonNode previousSchemaNode = previousSchemaJsonNode.get(field);
            JsonNode manifestSchemaNode = manifestSchemaJsonNode.get(field);
            if(Objects.isNull(manifestSchemaNode)){
                validateManifestSchema(previousSchemaNode, manifestSchemaNode, field, field);
            }
            Iterator<String> previousSchemaJsonNodeFields = next.getValue().fieldNames();
            validate(manifestSchemaNode, previousSchemaNode, previousSchemaJsonNodeFields, field);
        }
    }

    /** Recursively calling method to check all the children node and validating one by one
     * @param manifestSchemaJsonNode manifest schema json Node
     * @param previousSchemaJsonNode latest schema json node
     * @param previousSchemaFields   fields of specific json node fetched from db
     * @param parentJsonNode         parent json node
     */
    private void validate(final JsonNode manifestSchemaJsonNode, final JsonNode previousSchemaJsonNode, final Iterator<String> previousSchemaFields, final String parentJsonNode) {
        while (previousSchemaFields.hasNext()) {
            String field = previousSchemaFields.next();
            JsonNode previousSchemaNode = previousSchemaJsonNode.get(field);
            JsonNode manifestSchemaNode = manifestSchemaJsonNode.get(field);
            if (previousSchemaNode.fieldNames().hasNext()) {
                validate(manifestSchemaNode, previousSchemaNode, previousSchemaNode.fieldNames(), parentJsonNode);
            } else {
                validateManifestSchema(previousSchemaNode, manifestSchemaNode, parentJsonNode, field);
            }
        }
    }

    /** Validating each node to the previous node and throw exception in case 'type' field is modified or removed.
     * And throw exception for any missing fields.
     * @param previousSchemaNode latest schema json node from db
     * @param manifestSchemaNode manifest schema json Node
     * @param parentJsonNode     parent json node
     */
    private void validateManifestSchema(final JsonNode previousSchemaNode, final JsonNode manifestSchemaNode, final String parentJsonNode, final String field) {
        if (manifestSchemaNode == null || (field.equalsIgnoreCase("type") && !previousSchemaNode.equals(manifestSchemaNode))) {
            throw new IllegalArgumentException(String.format("A property of type" + " " + parentJsonNode + "has been changed in schema. " +
                    "You either need to bump your plugin to a new major version or revert your changes as it might break live courseware relying on it."));
        }
    }
}
