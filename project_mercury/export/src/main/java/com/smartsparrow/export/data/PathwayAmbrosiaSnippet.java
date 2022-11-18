package com.smartsparrow.export.data;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.smartsparrow.courseware.data.CoursewareElementType;

public class PathwayAmbrosiaSnippet implements AmbrosiaSnippet {

    private String $ambrosia;
    private String $id;
    @JsonProperty("config")
    private Map<String, Object> config;
    private List<Object> children;
    private List<Object> annotations;

    /**
     * Only specified in a root level exportedItem. Null otherwise
     */
    private ExportMetadata exportMetadata;

    @Override
    public String get$ambrosia() {
        return $ambrosia;
    }

    public PathwayAmbrosiaSnippet set$ambrosia(String $ambrosia) {
        this.$ambrosia = $ambrosia;
        return this;
    }

    @Override
    public String get$id() {
        return $id;
    }

    public PathwayAmbrosiaSnippet set$id(String $id) {
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
        return CoursewareElementType.PATHWAY;
    }

    @Override
    public List<Object> getAnnotations() {
        return annotations;
    }

    public PathwayAmbrosiaSnippet setAnnotations(List<Object> annotations) {
        this.annotations = annotations;
        return this;
    }

    @Override
    public AmbrosiaSnippet reduce(final AmbrosiaSnippet prev, final AmbrosiaSnippet next) {
        return reduce(prev)
                .reduce(next);
    }

    @Override
    public PathwayAmbrosiaSnippet reduce(AmbrosiaSnippet snippet) {
        // it can be an activity or an interactive, it doesn't really matter
        // traverse and replace the id with the snippet
        children = children.stream()
                .map(child -> {
                    // just add the snippet inside the children array in the config
                    if (String.valueOf(snippet.get$id()).equals(String.valueOf(child))) {
                        return snippet;
                    }

                    return child;
                })
                .collect(Collectors.toList());

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
    public PathwayAmbrosiaSnippet setConfig(Map<String, Object> config) {
        this.config = config;
        return this;
    }

    public List<Object> getChildren() {
        return children;
    }

    public PathwayAmbrosiaSnippet setChildren(List<Object> children) {
        this.children = children;
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PathwayAmbrosiaSnippet that = (PathwayAmbrosiaSnippet) o;
        return Objects.equals($ambrosia, that.$ambrosia) &&
                Objects.equals($id, that.$id) &&
                Objects.equals(config, that.config) &&
                Objects.equals(children, that.children) &&
                Objects.equals(annotations, that.annotations) &&
                Objects.equals(exportMetadata, that.exportMetadata);
    }

    @Override
    public int hashCode() {
        return Objects.hash($ambrosia, $id, config, children, annotations, exportMetadata);
    }
}
