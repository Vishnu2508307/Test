package com.smartsparrow.export.data;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import javax.annotation.Nullable;

import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.smartsparrow.courseware.data.CoursewareElementType;
import com.smartsparrow.export.lang.AmbrosiaSnippetReducerException;
import com.smartsparrow.plugin.payload.ExportPluginPayload;
import com.smartsparrow.util.log.MercuryLogger;
import com.smartsparrow.util.log.MercuryLoggerFactory;
import com.smartsparrow.workspace.data.IconLibrary;
import com.smartsparrow.workspace.data.ThemePayload;

public class ActivityAmbrosiaSnippet implements AmbrosiaSnippet {

    private static final MercuryLogger log = MercuryLoggerFactory.getLogger(ActivityAmbrosiaSnippet.class);

    private String $ambrosia;
    private String $id;
    private String $workspaceId;
    private String $projectId;
    @JsonProperty("config")
    private Map<String, Object> config;
    @JsonProperty("activityTheme")
    private Map<String, Object> activityTheme;
    private List<Object> annotations;
    @JsonProperty("selectedThemePayload")
    private ThemePayload selectedThemePayload;
    @JsonProperty("scenarios")
    private List<Object> scenarios;
    @JsonProperty("pluginPayload")
    private ExportPluginPayload pluginPayload;
    @JsonProperty("activityThemeIconLibraries")
    private List<IconLibrary> activityThemeIconLibraries;

    /**
     * Only specified in a root level exportedItem. Null otherwise
     */
    private ExportMetadata exportMetadata;

    @Override
    public String get$ambrosia() {
        return $ambrosia;
    }

    public ActivityAmbrosiaSnippet set$ambrosia(String $ambrosia) {
        this.$ambrosia = $ambrosia;
        return this;
    }

    @Override
    public String get$id() {
        return $id;
    }

    public ActivityAmbrosiaSnippet set$id(String $id) {
        this.$id = $id;
        return this;
    }

    public String get$workspaceId() {
        return $workspaceId;
    }

    public ActivityAmbrosiaSnippet set$workspaceId(String $workspaceId) {
        this.$workspaceId = $workspaceId;
        return this;
    }

    public String get$projectId() {
        return $projectId;
    }

    public ActivityAmbrosiaSnippet set$projectId(String $projectId) {
        this.$projectId = $projectId;
        return this;
    }

    @Override
    @JsonProperty("config")
    public Map<String, Object> getConfig() {
        return config;
    }

    @JsonProperty("activityTheme")
    public Map<String, Object> getActivityTheme() {
        return activityTheme;
    }

    @JsonIgnore
    @Override
    public CoursewareElementType getType() {
        return CoursewareElementType.ACTIVITY;
    }

    @Override
    public List<Object> getAnnotations() {
        return annotations;
    }

    public ActivityAmbrosiaSnippet setAnnotations(List<Object> annotations) {
        this.annotations = annotations;
        return this;
    }

    @Override
    public AmbrosiaSnippet reduce(final AmbrosiaSnippet prev, final AmbrosiaSnippet next) {
        return reduce(prev)
                .reduce(next);
    }

    @SuppressWarnings({"unchecked", "Duplicates"})
    @Override
    public ActivityAmbrosiaSnippet reduce(final AmbrosiaSnippet snippet) {
        if (snippet.getType().equals(CoursewareElementType.PATHWAY)) {
            final PathwayAmbrosiaSnippet pathwaySnippet = (PathwayAmbrosiaSnippet) snippet;
            // find this pathway name
            final List<String> keys = config.keySet().stream()
                    // we are looking for an object that has a pathwayId as property
                    .filter(key -> {
                        Object object = config.get(key);
                        // we are traversing json, so we could be dealing with an array, string, number or boolean
                        // so make sure it is an object
                        if (object instanceof Map) {
                            Map<String, Objects> value = (Map<String, Objects>) object;
                            final Object pathwayId = value.get("pathwayId");

                            if (pathwayId != null) {
                                return String.valueOf(pathwayId).equals(snippet.get$id());
                            }

                        }
                        return false;
                    }).collect(Collectors.toList());

            if (keys.size() > 0) {
                // there can be only 1
                final String key = keys.get(0);

                final Map<String, Object> pathway = (Map<String, Object>) config.get(key);

                pathway.remove("pathwayId");
                pathway.remove("pathwayType");

                pathway.put("$ambrosia", pathwaySnippet.get$ambrosia());
                pathway.put("id", pathwaySnippet.get$id());
                pathway.put("config", pathwaySnippet.getConfig());
                pathway.put("annotations", pathwaySnippet.getAnnotations());
                pathway.put("children", pathwaySnippet.getChildren());

                config.put(key, pathway);
            }
            return this;
        }

        if (snippet.getType().equals(CoursewareElementType.COMPONENT)) {
            // handle the component
            // it can only be a component so find it in the components array
            try {
                // get the components prop from the config
                List<Map<String, Object>> components = (List<Map<String, Object>>) config.get("components");
                // replace the itemId with the snippet and keep the localRef
                if (components != null) {
                    List<Map<String, Object>> mapped = components.stream()
                            .peek(component -> {
                                Object componentId = component.get("itemId");

                                // replace the itemId with the snippet and keep the localRef
                                if (componentId != null) {
                                    if (String.valueOf(componentId).equals(snippet.get$id())) {
                                        component.remove("itemId");
                                        component.put("$ambrosia", snippet.get$ambrosia());
                                        component.put("$id", snippet.get$id());
                                        component.put("config", snippet.getConfig());
                                        component.put("annotations", snippet.getAnnotations());
                                        component.put("pluginPayload", ((ComponentAmbrosiaSnippet) snippet).getPluginPayload());
                                    }
                                }
                            }).collect(Collectors.toList());

                    config.put("components", mapped);
                }

            } catch (ClassCastException | NullPointerException e) {
                log.jsonError("error reducing activity {} with component snippet", new HashMap<String, Object>() {{
                    put("activityId", $id);
                }}, e);
                throw new AmbrosiaSnippetReducerException(String.format(
                        "error reducing activity [%s] with component snippet",
                        $id));
            }
        }

        return this;
    }

    @JsonProperty("config")
    @JsonAnySetter
    public ActivityAmbrosiaSnippet setConfig(Map<String, Object> config) {
        this.config = config;
        return this;
    }

    @JsonProperty("activityTheme")
    @JsonAnySetter
    public ActivityAmbrosiaSnippet setActivityTheme(Map<String, Object> activityTheme) {
        this.activityTheme = activityTheme;
        return this;
    }

    /**
     * Only specified in a root level activity. Null otherwise
     *
     * @return the exportMetadata
     */
    @Nullable
    public ExportMetadata getExportMetadata() {
        return exportMetadata;
    }

    public ActivityAmbrosiaSnippet setExportMetadata(ExportMetadata exportMetadata) {
        this.exportMetadata = exportMetadata;
        return this;
    }

    @JsonProperty("selectedThemePayload")
    public ThemePayload getSelectedThemePayload() {
        return selectedThemePayload;
    }

    @JsonProperty("selectedThemePayload")
    @JsonAnySetter
    public ActivityAmbrosiaSnippet setSelectedThemePayload(final ThemePayload selectedThemePayload) {
        this.selectedThemePayload = selectedThemePayload;
        return this;
    }

    @JsonProperty("scenarios")
    public List<Object> getScenarios() {
        return scenarios;
    }

    @JsonProperty("scenarios")
    @JsonAnySetter
    public ActivityAmbrosiaSnippet setScenarios(final List<Object> scenarios) {
        this.scenarios = scenarios;
        return this;
    }

    @JsonProperty("pluginPayload")
    public ExportPluginPayload getPluginPayload() {
        return pluginPayload;
    }

    @JsonProperty("pluginPayload")
    @JsonAnySetter
    public ActivityAmbrosiaSnippet setPluginPayload(final ExportPluginPayload pluginPayload) {
        this.pluginPayload = pluginPayload;
        return this;
    }

    @JsonProperty("activityThemeIconLibraries")
    public List<IconLibrary> getActivityThemeIconLibraries() {
        return activityThemeIconLibraries;
    }

    @JsonProperty("activityThemeIconLibraries")
    @JsonAnySetter
    public ActivityAmbrosiaSnippet setActivityThemeIconLibraries(final List<IconLibrary> activityThemeIconLibraries) {
        this.activityThemeIconLibraries = activityThemeIconLibraries;
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ActivityAmbrosiaSnippet that = (ActivityAmbrosiaSnippet) o;
        return Objects.equals($ambrosia, that.$ambrosia) &&
                Objects.equals($id, that.$id) &&
                Objects.equals($workspaceId, that.$workspaceId) &&
                Objects.equals($projectId, that.$projectId) &&
                Objects.equals(config, that.config) &&
                Objects.equals(activityTheme, that.activityTheme) &&
                Objects.equals(annotations, that.annotations) &&
                Objects.equals(exportMetadata, that.exportMetadata) &&
                Objects.equals(selectedThemePayload, that.selectedThemePayload) &&
                Objects.equals(scenarios, that.scenarios) &&
                Objects.equals(pluginPayload, that.pluginPayload) &&
                Objects.equals(activityThemeIconLibraries, that.activityThemeIconLibraries);
    }

    @Override
    public int hashCode() {
        return Objects.hash($ambrosia,
                            $id,
                            $workspaceId,
                            $projectId,
                            config,
                            activityTheme,
                            annotations,
                            exportMetadata,
                            selectedThemePayload,
                            scenarios,
                            pluginPayload,
                            activityThemeIconLibraries);
    }
}
