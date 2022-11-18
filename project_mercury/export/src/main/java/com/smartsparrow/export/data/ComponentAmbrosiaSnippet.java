package com.smartsparrow.export.data;

import java.util.List;
import java.util.Map;
import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.smartsparrow.courseware.data.CoursewareElementType;
import com.smartsparrow.plugin.payload.ExportPluginPayload;

public class ComponentAmbrosiaSnippet implements AmbrosiaSnippet {

    private String $ambrosia;
    private String $id;
    @JsonProperty("config")
    private Map<String, Object> config;
    private List<Object> annotations;
    @JsonProperty("pluginPayload")
    private ExportPluginPayload pluginPayload;

    /**
     * Only specified in a root level exportedItem. Null otherwise
     */
    private ExportMetadata exportMetadata;

    @Override
    public String get$ambrosia() {
        return $ambrosia;
    }

    public ComponentAmbrosiaSnippet set$ambrosia(String $ambrosia) {
        this.$ambrosia = $ambrosia;
        return this;
    }

    @Override
    public String get$id() {
        return $id;
    }

    public ComponentAmbrosiaSnippet set$id(String $id) {
        this.$id = $id;
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
        return CoursewareElementType.COMPONENT;
    }

    @Override
    public List<Object> getAnnotations() {
        return annotations;
    }

    public ComponentAmbrosiaSnippet setAnnotations(List<Object> annotations) {
        this.annotations = annotations;
        return this;
    }
    @JsonProperty("pluginPayload")
    public ExportPluginPayload getPluginPayload() {
        return pluginPayload;
    }

    @JsonProperty("pluginPayload")
    @JsonAnySetter
    public ComponentAmbrosiaSnippet setPluginPayload(final ExportPluginPayload pluginPayload) {
        this.pluginPayload = pluginPayload;
        return this;
    }

    @Override
    public AmbrosiaSnippet reduce(final AmbrosiaSnippet prev, final AmbrosiaSnippet next) {
        // this method will never be invoked because components don't have children
        // throw an exception if it is invoked
        throw new UnsupportedOperationException("cannot reduce snippets into a component snippet");
    }

    @Override
    public AmbrosiaSnippet reduce(AmbrosiaSnippet snippet) {
        // this method will never be invoked because components don't have children
        // throw an exception if it is invoked
        throw new UnsupportedOperationException("cannot reduce snippets into a component snippet");
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
    public ComponentAmbrosiaSnippet setConfig(Map<String, Object> config) {
        this.config = config;
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ComponentAmbrosiaSnippet that = (ComponentAmbrosiaSnippet) o;
        return Objects.equals($ambrosia, that.$ambrosia) &&
                Objects.equals($id, that.$id) &&
                Objects.equals(config, that.config) &&
                Objects.equals(exportMetadata, that.exportMetadata) &&
                Objects.equals(annotations, that.annotations)&&
                Objects.equals(pluginPayload, that.pluginPayload);
    }

    @Override
    public int hashCode() {
        return Objects.hash($ambrosia, $id, config, annotations, exportMetadata,
                            pluginPayload);
    }
}
