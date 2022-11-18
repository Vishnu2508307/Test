package com.smartsparrow.learner.schema;

import java.io.IOException;

import javax.inject.Inject;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.smartsparrow.plugin.schema.SchemaValidator;

public class ScenarioSchemaValidator {

    private Logger log = LoggerFactory.getLogger(getClass().getName());
    private final String scenarioSchema;

    @Inject
    public ScenarioSchemaValidator() throws IOException {
        this.scenarioSchema = IOUtils
                .toString(getClass()
                                .getResourceAsStream("/schema/scenario-condition.json"),
                        "UTF-8");
    }

    public void validate(String scenarioCondition) {

        if (log.isDebugEnabled()) {
            log.debug("Scenario Condition {}", scenarioCondition);
        }

        SchemaValidator validator = new SchemaValidator
                .Builder()
                .forJson(scenarioCondition)
                .withSchema(scenarioSchema)
                .build();
        validator.validate();
    }
}
