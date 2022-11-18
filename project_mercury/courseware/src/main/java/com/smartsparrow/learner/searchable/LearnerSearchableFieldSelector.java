package com.smartsparrow.learner.searchable;

import static com.smartsparrow.util.Warrants.affirmArgument;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;

import javax.annotation.Nonnull;
import javax.inject.Inject;
import javax.inject.Singleton;

import org.json.JSONException;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Lists;
import com.smartsparrow.exception.IllegalArgumentFault;
import com.smartsparrow.exception.UnsupportedOperationFault;
import com.smartsparrow.plugin.data.PluginSearchableField;
import com.smartsparrow.util.log.MercuryLogger;
import com.smartsparrow.util.log.MercuryLoggerFactory;

@Singleton
public class LearnerSearchableFieldSelector {

    private static final MercuryLogger log = MercuryLoggerFactory.getLogger(LearnerSearchableFieldSelector.class);

    @Inject
    public LearnerSearchableFieldSelector() {
    }

    /**
     * Select the plugin searchable field values from the json config
     *
     * @param pluginSearchableField the searchable field to select the values for
     * @param config the config to select the values from
     * @return an object containing the selections
     * @throws IllegalArgumentFault when the required argument are not supplied.
     * @throws UnsupportedOperationFault when the value could not be selected.
     */
    public LearnerSearchableFieldValue select(final PluginSearchableField pluginSearchableField, final String config) {

        affirmArgument(pluginSearchableField != null, "pluginSearchableField is required");
        affirmArgument(config != null, "config is required");

        try {
            // select the body fields
            final String body = selectFieldValues(pluginSearchableField.getBody(), config);

            // select the tag fields
            final String tag = selectFieldValues(pluginSearchableField.getTag(), config);

            // select the preview fields
            final String preview = selectFieldValues(pluginSearchableField.getPreview(), config);

            // select the source fields
            final String source = selectFieldValues(pluginSearchableField.getSource(), config);

            // select the summary fields
            final String summary = selectFieldValues(pluginSearchableField.getSummary(), config);

            // return the selections
            return new LearnerSearchableFieldValue(summary, body, source, preview, tag);

        } catch (JSONException e) {
            log.error("error selecting the searchable field value", e);
            throw new UnsupportedOperationFault("error selecting the searchable field value");
        }
    }

    /**
     * Select list of json config values given a collection of target field names.
     *
     * @param fields collection of searchable json context paths to be found in config
     * @param config element config json value
     * @return a string representing the complete selection or the default value
     * @throws UnsupportedOperationFault when the value could not be selected.
     */
    private String selectFieldValues(final Collection<String> fields, final String config) {
        return fields.stream()
                .map(field -> {
                    List<String> path = field.contains(".") ? Arrays.asList(field.split("\\.")) : Lists.newArrayList(field);
                    return selectOrDefault(config, path);
                })
                .collect(Collectors.joining(" "))
                .trim();
    }

    /**
     * Select the string content from the json config or return the defaultValue supplied when the selection fails
     *
     * @param jsonString a string representation of a json obj
     * @param contextPath the context path
     * @return a string representing the complete selection or the default value
     */
    private String selectOrDefault(@Nonnull final String jsonString, @Nonnull final List<String> contextPath) {
        try {
            return select(jsonString, contextPath);
        } catch (JSONException e) {
            log.error("could not select contextPath in json object", e);
            return "";
        }
    }

    /**
     * Select string content from a json config given a context path. When an array is encountered the values are
     * selected from each element of the array
     *
     * @param jsonString a string representation of the json object
     * @param contextPath the context path
     * @return a string representing the complete selection
     * @throws JSONException when the selection fails
     */
    private String select(@Nonnull final String jsonString, @Nonnull final List<String> contextPath) {

        try {
            StringBuilder selection = new StringBuilder();

            final ObjectMapper om = new ObjectMapper();
            JsonNode currentNode = om.readTree(jsonString);

            // loop the context path
            for (int i = 0; i < contextPath.size(); i++) {

                // if the node is null we cannot continue, so throw
                if (currentNode == null) {
                    throw new JSONException("path not found in json object");
                }

                // get the next path
                final String path = contextPath.get(i);

                // set the current node
                if (currentNode.isObject()) {
                    currentNode = currentNode.get(path);
                    // we have got the node for this path therefore continue
                    continue;
                }

                if (currentNode.isArray()) {
                    // we are dealing with an array loop the elements
                    Iterator<JsonNode> iterator = currentNode.elements();

                    while (iterator.hasNext()) {
                        JsonNode next = iterator.next();

                        // start a recursion
                        selection
                                .append(select(om.writeValueAsString(next), contextPath.subList(i, contextPath.size())))
                                .append(" ");
                    }
                    // the recursion has been handling the remaining paths so let's just return
                    return selection.toString().trim();
                }
            }

            // if the node is null the last path was not found, therefore throw
            if (currentNode == null) {
                throw new JSONException("path not found in json object");
            }

            if (currentNode.isTextual()) {
                return currentNode.asText().trim();
            }

            // if the node value is not a string, throw
            throw new JSONException("path not found in json object");

        } catch (IOException e) {
            throw new JSONException("error parsing json string", e);
        }
    }
}
