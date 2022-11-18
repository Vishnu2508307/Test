package com.smartsparrow.export.data;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.smartsparrow.courseware.data.CoursewareElementType;
import com.smartsparrow.export.lang.AmbrosiaSnippetReducerException;
import com.smartsparrow.asset.service.AssetPayload;
import com.smartsparrow.plugin.payload.ExportPluginPayload;
import com.smartsparrow.util.log.MercuryLogger;
import com.smartsparrow.util.log.MercuryLoggerFactory;
import com.smartsparrow.workspace.data.IconLibrary;
import com.smartsparrow.workspace.data.ThemePayload;

public class InteractiveAmbrosiaSnippet implements AmbrosiaSnippet {

    private static final MercuryLogger log = MercuryLoggerFactory.getLogger(InteractiveAmbrosiaSnippet.class);

    private String $ambrosia;
    private String $id;
    private String $workspaceId;
    private String $projectId;
    @JsonProperty("config")
    private Map<String, Object> config;
    @JsonProperty("assets")
    private List<AssetPayload> assets;
    private List<Object> annotations;
    @JsonProperty("activityTheme")
    private Map<String, Object> activityTheme;
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

    public InteractiveAmbrosiaSnippet set$ambrosia(String $ambrosia) {
        this.$ambrosia = $ambrosia;
        return this;
    }

    @Override
    public String get$id() {
        return $id;
    }

    public InteractiveAmbrosiaSnippet set$id(String $id) {
        this.$id = $id;
        return this;
    }

    public String get$workspaceId() {
        return $workspaceId;
    }

    public InteractiveAmbrosiaSnippet set$workspaceId(String $workspaceId) {
        this.$workspaceId = $workspaceId;
        return this;
    }

    public String get$projectId() {
        return $projectId;
    }

    public InteractiveAmbrosiaSnippet set$projectId(String $projectId) {
        this.$projectId = $projectId;
        return this;
    }

    @Override
    @JsonProperty("config")
    public Map<String, Object> getConfig() {
        return config;
    }

    @JsonIgnore
    @Override
    public CoursewareElementType getType() {
        return CoursewareElementType.INTERACTIVE;
    }

    @Override
    public List<Object> getAnnotations() {
        return annotations;
    }

    @JsonProperty("activityTheme")
    public Map<String, Object> getActivityTheme() {
        return activityTheme;
    }

    public InteractiveAmbrosiaSnippet setAnnotations(List<Object> annotations) {
        this.annotations = annotations;
        return this;
    }

    @JsonProperty("activityTheme")
    @JsonAnySetter
    public InteractiveAmbrosiaSnippet setActivityTheme(Map<String, Object> activityTheme) {
        this.activityTheme = activityTheme;
        return this;
    }

    @JsonProperty("selectedThemePayload")
    public ThemePayload getSelectedThemePayload() {
        return selectedThemePayload;
    }

    @JsonProperty("selectedThemePayload")
    @JsonAnySetter
    public InteractiveAmbrosiaSnippet setSelectedThemePayload(final ThemePayload selectedThemePayload) {
        this.selectedThemePayload = selectedThemePayload;
        return this;
    }

    @Override
    public AmbrosiaSnippet reduce(final AmbrosiaSnippet prev, final AmbrosiaSnippet next) {
        return reduce(prev)
                .reduce(next);
    }

    @SuppressWarnings({"unchecked", "Duplicates"})
    @Override
    public InteractiveAmbrosiaSnippet reduce(AmbrosiaSnippet snippet) {
        if (snippet.getType().equals(CoursewareElementType.COMPONENT)) {
            // it can only be a component so find it in the components array
            try {
                // get the components prop from the config
                List<Map<String, Object>> components = (List<Map<String, Object>>) config.get("components");

                if (components != null) {
                    // replace the itemId with the snippet for the target component
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
                log.jsonError("error reducing interactive {} with component snippet", new HashMap<String, Object>() {{
                    put("interactiveId", $id);
                }}, e);
                throw new AmbrosiaSnippetReducerException(String.format(
                        "error reducing interactive [%s] with component snippet",
                        $id));
            }
        }

        return this;
    }

    @Override
    public AmbrosiaSnippet setExportMetadata(ExportMetadata exportMetadata) {
        this.exportMetadata = exportMetadata;
        return this;
    }

    @Override
    public ExportMetadata getExportMetadata() {
        return exportMetadata;
    }

    @JsonProperty("config")
    @JsonAnySetter
    public InteractiveAmbrosiaSnippet setConfig(Map<String, Object> config) {
        this.config = config;
        return this;
    }

    @JsonProperty("assets")
    public List<AssetPayload> getAssets() {
        return assets;
    }

    @JsonProperty("assets")
    @JsonAnySetter
    public InteractiveAmbrosiaSnippet setAssets(final List<AssetPayload> assets) {
        this.assets = assets;
        return this;
    }

    @JsonProperty("scenarios")
    public List<Object> getScenarios() {
        return scenarios;
    }

    @JsonProperty("scenarios")
    @JsonAnySetter
    public InteractiveAmbrosiaSnippet setScenarios(final List<Object> scenarios) {
        this.scenarios = scenarios;
        return this;
    }

    @JsonProperty("pluginPayload")
    public ExportPluginPayload getPluginPayload() {
        return pluginPayload;
    }

    @JsonProperty("pluginPayload")
    @JsonAnySetter
    public InteractiveAmbrosiaSnippet setPluginPayload(final ExportPluginPayload pluginPayload) {
        this.pluginPayload = pluginPayload;
        return this;
    }

    @JsonProperty("activityThemeIconLibraries")
    public List<IconLibrary> getActivityThemeIconLibraries() {
        return activityThemeIconLibraries;
    }

    @JsonProperty("activityThemeIconLibraries")
    @JsonAnySetter
    public InteractiveAmbrosiaSnippet setActivityThemeIconLibraries(final List<IconLibrary> activityThemeIconLibraries) {
        this.activityThemeIconLibraries = activityThemeIconLibraries;
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        InteractiveAmbrosiaSnippet that = (InteractiveAmbrosiaSnippet) o;
        return Objects.equals($ambrosia, that.$ambrosia) &&
                Objects.equals($id, that.$id) &&
                Objects.equals($workspaceId, that.$workspaceId) &&
                Objects.equals($projectId, that.$projectId) &&
                Objects.equals(config, that.config) &&
                Objects.equals(assets, that.assets) &&
                Objects.equals(activityTheme, that.activityTheme) &&
                Objects.equals(exportMetadata, that.exportMetadata) &&
                Objects.equals(annotations, that.annotations) &&
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
                            assets,
                            annotations,
                            activityTheme,
                            exportMetadata,
                            selectedThemePayload,
                            scenarios,
                            pluginPayload,
                            activityThemeIconLibraries);
    }
}
