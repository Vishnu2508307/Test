package com.smartsparrow.export.service;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import javax.inject.Inject;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.smartsparrow.courseware.data.CoursewareElement;
import com.smartsparrow.courseware.data.CoursewareElementType;
import com.smartsparrow.courseware.data.tree.CoursewareElementNode;
import com.smartsparrow.courseware.service.CoursewareService;
import com.smartsparrow.export.data.ActivityAmbrosiaSnippet;
import com.smartsparrow.export.data.AmbrosiaSnippet;
import com.smartsparrow.export.data.ComponentAmbrosiaSnippet;
import com.smartsparrow.export.data.ExportAmbrosiaSnippet;
import com.smartsparrow.export.data.ExportMetadata;
import com.smartsparrow.export.data.ExportSummary;
import com.smartsparrow.export.data.InteractiveAmbrosiaSnippet;
import com.smartsparrow.export.data.PathwayAmbrosiaSnippet;
import com.smartsparrow.export.lang.AmbrosiaSnippetReducerException;
import com.smartsparrow.util.DateFormat;
import com.smartsparrow.util.UUIDs;
import com.smartsparrow.util.log.MercuryLogger;
import com.smartsparrow.util.log.MercuryLoggerFactory;

import reactor.core.publisher.Mono;

public class AmbrosiaSnippetsReducer {

    private static final ObjectMapper objectMapper = new ObjectMapper();

    // do not include null fields
    static {
        objectMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
    }

    private static final MercuryLogger log = MercuryLoggerFactory.getLogger(AmbrosiaSnippetsReducer.class);

    private final CoursewareService coursewareService;

    @Inject
    public AmbrosiaSnippetsReducer(CoursewareService coursewareService) {
        this.coursewareService = coursewareService;
    }

    /**
     * Traverse the courseware structure with a depth-first strategy and reduces each element ambrosia snippet found.
     * It's important to know that the {@link CoursewareElementNode} structure could be different from since the
     * export request was first issued. This is the nature of the author environment that keeps changing. The reduced
     * result could not include elements that have been deleted between the time of requesting an export and now.
     * TODO: possible fix for this in the future is to snapshot the courseware structure.
     *
     * @param snippets   the ambrosia snippets to reduce
     * @param courseware the courseware structure
     * @param exportSummary the export summary
     * @return a string representation of the reduced ambrosia json
     */
    public Mono<AmbrosiaSnippet> reduce(final Map<UUID, ExportAmbrosiaSnippet> snippets,
                       final CoursewareElementNode courseware, final ExportSummary exportSummary) {
        final ExportAmbrosiaSnippet snippet = snippets.get(courseware.getElementId());
        log.jsonInfo("Reducing ambrosia snippets", new HashMap<String, Object>() {
            {put("totalSnippets",snippets.size());}
            {put("exportId",exportSummary.getId());}
        });
        
        if (snippet == null) {
            throw new AmbrosiaSnippetReducerException("snippet not found for top level exported element");
        }

        // invoke the reduce function
        final AmbrosiaSnippet reduced = reduce(courseware, deserializeSnippet(snippet.getAmbrosiaSnippet(), courseware.getType()), snippets);

        if (reduced == null) {
            throw new AmbrosiaSnippetReducerException("something went wrong when reducing");
        }

        // find the exported element ancestry and add it to the metadata
        return coursewareService.findCoursewareElementAncestry(CoursewareElement.from(exportSummary.getElementId(), exportSummary.getElementType()))
                .map(coursewareElementAncestry -> {
                    // set the metadata and the completion id then return the reduced snippet
                    return reduced.setExportMetadata(new ExportMetadata(UUIDs.timeBased())
                            .setExportId(exportSummary.getId())
                            .setAncestry(coursewareElementAncestry.getAncestry())
                            .setStartedAt(DateFormat.asRFC1123(exportSummary.getId()))
                            .setElementsExportedCount(snippets.size())
                            .setExportType(exportSummary.getExportType())
                            .setMetadata(exportSummary.getMetadata()));
                });
    }

    /**
     * Performs a depth-first recursion and then reduces the snippets together
     *
     * @param node     the current node being traversed
     * @param parent   the parent snippet for this node
     * @param snippets the snippets
     * @return the reduced ambrosia snippet
     */
    private AmbrosiaSnippet reduce(final CoursewareElementNode node, final AmbrosiaSnippet parent,
                                   final Map<UUID, ExportAmbrosiaSnippet> snippets) {
        // first get the snippet for this node
        final ExportAmbrosiaSnippet snippet = snippets.get(node.getElementId());

        if (snippet == null) {
            // this could be a new element that was created while the export request was processing
            // for now skip it, in the future this should not happen once the courseware structure
            // will be snapshot and persisted
            return parent;
        }

        // deserialize the snippet for the current node
        final AmbrosiaSnippet current = deserializeSnippet(snippet.getAmbrosiaSnippet(), node.getType());

        if (node.getChildren().size() > 0) {
            // traverse the children
            return node.getChildren().stream()
                    // invoke recursion
                    .map(child -> reduce(child, current, snippets))
                    // reduce the snippets
                    .reduce(current, current::reduce, current::reduce);
        }

        // there are no children so use the parent to reduce the current
        return parent.reduce(current);
    }

    /**
     * Get the ambrosia snippet class type for a courseware element type for deserialization purposes
     *
     * @param type the courseware element type to find the ambrosia snippet class for
     * @return the Ambrosia Snippet class type
     */
    private Class<? extends AmbrosiaSnippet> getSnippetClassType(final CoursewareElementType type) {
        switch (type) {
            case ACTIVITY:
                return ActivityAmbrosiaSnippet.class;
            case INTERACTIVE:
                return InteractiveAmbrosiaSnippet.class;
            case PATHWAY:
                return PathwayAmbrosiaSnippet.class;
            case COMPONENT:
                return ComponentAmbrosiaSnippet.class;
            default:
                throw new AmbrosiaSnippetReducerException(String.format("Type [%s] not supported", type));
        }
    }

    /**
     * Convenience method to deserialize a snippet and throw a runtime exception when failing to do so.
     *
     * @param snippet the snippet to deserialize
     * @param type    the courseware element type for the snippet
     * @return a deserialized ambrosia snippet
     * @throws AmbrosiaSnippetReducerException when failing to deserialize
     */
    private AmbrosiaSnippet deserializeSnippet(final String snippet, final CoursewareElementType type) {
        try {
            return objectMapper.readValue(snippet, getSnippetClassType(type));
        } catch (IOException e) {
            log.error("failed to deserialize snippet", e);
            throw new AmbrosiaSnippetReducerException(String.format("failed to deserialize snippet => %s", snippet), e);
        }
    }

    /**
     * Convenience method to serialize a snippet to a string and throw a runtime exception when failing to do so.
     *
     * @param snippet the ambrosia snippet to serialize
     * @return a File of the written JSON, caller is responsible for the deletion of it.
     * @throws AmbrosiaSnippetReducerException when failing to serialize
     */
    public File serialize(final AmbrosiaSnippet snippet) {
        File ambrosiaFile = null;
        try {
            ambrosiaFile = File.createTempFile(snippet.get$id(), "snippet");
            objectMapper.writeValue(ambrosiaFile, snippet);
            return ambrosiaFile;
        } catch (JsonProcessingException e) {
            log.error("failed to serialize snippet", e);
            cleanupTemporaryFile(ambrosiaFile);
            throw new AmbrosiaSnippetReducerException(String.format("failed to serialize snippet => %s", snippet.get$id()), e);
        } catch (IOException e) {
            log.error(String.format("failed to create temporary snippet file exportId %s ", snippet.get$id()));
            cleanupTemporaryFile(ambrosiaFile);
            throw new AmbrosiaSnippetReducerException(String.format("failed to create temporary snippet file exportId %s ", snippet.get$id()), e);
        }
    }

    /**
     * Delete the supplied file
     *
     * @param tempFile
     */
    private void cleanupTemporaryFile(final File tempFile) {
        if (tempFile != null) {
            boolean wasDeleted = tempFile.delete();
            if (!wasDeleted) {
                log.warn("Did not delete temporary file: {}", tempFile.getAbsolutePath());
            }
        }
    }
}
