package com.smartsparrow.plugin.publish;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.smartsparrow.util.log.MercuryLogger;
import com.smartsparrow.util.log.MercuryLoggerFactory;

import javax.inject.Inject;
import java.io.IOException;


public class SearchableField implements PluginField<JsonNode, String> {
    private static final MercuryLogger log = MercuryLoggerFactory.getLogger(SearchableField.class);

    ObjectMapper om = new ObjectMapper();
    private String fieldName;
    private String manifestContent;

    @Inject
    public SearchableField(String fieldName, String manifestContent) {
        this.fieldName = fieldName;
        this.manifestContent = manifestContent;
    }

    @Override
    public JsonNode parse(PluginParserContext pluginParserContext) throws IOException {
       return om.readTree(manifestContent).at("/"+fieldName);
    }
}
