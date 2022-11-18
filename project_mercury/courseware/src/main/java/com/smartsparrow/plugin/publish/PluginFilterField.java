package com.smartsparrow.plugin.publish;


import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import com.smartsparrow.plugin.data.PluginFilter;
import com.smartsparrow.plugin.data.PluginFilterType;
import com.smartsparrow.plugin.lang.PluginPublishException;


public class PluginFilterField implements PluginField<List<PluginFilter>, Map<String, Object>> {

    private ObjectMapper om = new ObjectMapper();
    private String fieldName;
    private String manifestContent;


    public PluginFilterField(String fieldName, String manifestContent) {
        this.fieldName = fieldName;
        this.manifestContent = manifestContent;
    }

    @Override
    public List<PluginFilter> parse(PluginParserContext pluginParserContext) throws IOException, PluginPublishException {
        JsonNode filtersNode = om.readTree(manifestContent).at("/" + fieldName);

        List<PluginFilter> pluginFilterList = new ArrayList<>();

        // Return empty list if 'filters' node itself is missing. Field is now optional.
        if (filtersNode.isMissingNode()) {
            return pluginFilterList;
        }

        // If present, must be array to keep manifest consistent
        if (!filtersNode.isArray()) {
            throw new PluginPublishException("Field 'filters' should be an array");
        }


        for (JsonNode filterNode : filtersNode) {

            // if filter node has valid structure or not. If not throw an exception
            isValidFilterNodeStructure(filterNode);

            JsonNode filterTypeNode = filterNode.path("type");

            if (filterTypeNode.isMissingNode()) {
                throw new PluginPublishException("Filter object has missing type field");
            }
            String filterType = filterTypeNode.asText();

            if (filterType.isEmpty()) {
                throw new PluginPublishException("Filter object has missing or empty type field value");
            }

            if (PluginFilterType.allows(filterType)) {

                JsonNode filterValuesNode = filterNode.path("values");

                if (filterValuesNode.isMissingNode()) {
                    throw new PluginPublishException("Filter object has missing values field");
                }
                Set<String> filterValues = getFilterValues(filterValuesNode);

                pluginFilterList.add(new PluginFilter()
                                             .setPluginId(pluginParserContext.getPluginId())
                                             .setVersion(pluginParserContext.getVersion())
                                             .setFilterType(PluginFilterType.valueOf(filterType.toUpperCase()))
                                             .setFilterValues(filterValues));

            } else {
                throw new PluginPublishException("Filter object has not a valid type");
            }
        }
        return pluginFilterList;
    }

    /**
     * Check each filter node if it has valid structure or not, if not throw an exception
     * @param filterNode the filter node
     * @throws PluginPublishException throw exception in case of invalid structure
     */
    private void isValidFilterNodeStructure(final JsonNode filterNode) throws PluginPublishException {
        Iterator<String> filterFieldNames = filterNode.fieldNames();
        while (filterFieldNames.hasNext()) {
            String fieldName = filterFieldNames.next();
            if (!(fieldName.equalsIgnoreCase("type") || fieldName.equalsIgnoreCase("values"))) {
                throw new PluginPublishException("Filter node has invalid structure");
            }
        }
    }

    /**
     * Return set of filter values.
     * @param filterValueNode the filter values node
     * @return set of values
     * @throws IOException
     * @throws PluginPublishException
     */
    private Set<String> getFilterValues(JsonNode filterValueNode) throws IOException, PluginPublishException {

        if (!filterValueNode.isArray()) {
            throw new PluginPublishException("Field 'filters values' should be an array");
        }
        if (filterValueNode.size() == 0) {
            throw new PluginPublishException("Field 'filters values' should not be an empty array");
        }
        ObjectReader reader = om.readerFor(new TypeReference<Set<String>>() {
        });
        Set<String> set = new HashSet<>();
        set.addAll(reader.readValue(filterValueNode));

        return set;
    }
}