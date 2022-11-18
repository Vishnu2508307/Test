package com.smartsparrow.plugin.schema;

import static com.google.common.base.Preconditions.checkArgument;

import org.everit.json.schema.Schema;
import org.everit.json.schema.ValidationException;
import org.everit.json.schema.loader.SchemaLoader;
import org.json.JSONObject;
import org.json.JSONTokener;

public class SchemaValidator {

    private JSONObject json;
    private Schema schema;

    private SchemaValidator() {

    }

    public static class Builder {

        private String json;
        private String schema;

        /**
         * Allows to set the json content that requires to be validated
         *
         * @param json a string representation of a json object
         * @return the builder
         */
        public Builder forJson(String json) {
            this.json = json;
            return this;
        }

        /**
         * Allows to set the schema content that the json should be validated against
         *
         * @param schema a string representation of a json schema
         * @return the builder
         */
        public Builder withSchema(String schema) {
            this.schema = schema;
            return this;
        }

        /**
         * Build a schema validator object that can be used to validate the json via the {@link SchemaValidator#validate()}
         * method.
         *
         * @throws IllegalArgumentException when either <b>json</b> or <b>schema</b> properties are <code>null</code>
         * @return the schema validator object
         */
        public SchemaValidator build() {
            checkArgument(this.json != null, "json argument is required");
            checkArgument(this.schema != null, "schema argument is required");

            SchemaValidator schemaValidator = new SchemaValidator();
            schemaValidator.json = new JSONObject(new JSONTokener(this.json));

            JSONObject schema = new JSONObject(new JSONTokener(this.schema));

            schemaValidator.schema = SchemaLoader.load(schema);

            return schemaValidator;
        }
    }

    /**
     * Validate the json object against the json schema
     *
     * @throws ValidationException when the validation fails. Error messages available via
     * {@link ValidationException#getAllMessages()}
     */
    public void validate() {
        this.schema.validate(json);
    }

}
