package com.smartsparrow.export.data;

import java.util.List;
import java.util.Map;

import com.smartsparrow.courseware.data.CoursewareElement;
import com.smartsparrow.courseware.data.CoursewareElementType;

public interface AmbrosiaSnippet {

    /**
     * Provides the text description of the snippet usually with a format of:<br>
     *     `aero:activity:<plugin-id>:<plugin-version>`
     */
    String get$ambrosia();

    /**
     * Provides a string representation of the {@link CoursewareElement} id this snippets refers to
     */
    String get$id();

    /**
     * Provides the config for this snippet. This could be null for some type of Pathways
     */
    Map<String, Object> getConfig();

    /**
     * Provides the {@link CoursewareElementType} this snippets is for
     */
    CoursewareElementType getType();

    /**
     * Provides a list of annotations included in this snippet
     *
     * @return a list of object representing the annotations
     */
    List<Object> getAnnotations();

    /**
     * Reduce 2 incoming snippets into 1
     *
     * @param prev the first snippet to reduce
     * @param next the second snippet to reduce
     * @return the reduced snippet
     */
    AmbrosiaSnippet reduce(final AmbrosiaSnippet prev, final AmbrosiaSnippet next);

    AmbrosiaSnippet reduce(final AmbrosiaSnippet snippet);

    AmbrosiaSnippet setExportMetadata(final ExportMetadata exportMetadata);

    ExportMetadata getExportMetadata();
}
