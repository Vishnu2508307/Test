package com.smartsparrow.learner.service;

import com.smartsparrow.courseware.data.ConfigurationField;

public class ConfigurationFieldDataStub {

    public static ConfigurationField buildField(String fieldName, String fieldValue) {
        return new ConfigurationField()
                .setFieldName(fieldName)
                .setFieldValue(fieldValue);
    }
}
