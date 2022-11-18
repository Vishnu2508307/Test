package com.smartsparrow.graphql.type.mutation;

import java.util.List;
import java.util.Objects;
import java.util.UUID;

import com.smartsparrow.courseware.data.CoursewareElementType;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import io.leangen.graphql.annotations.GraphQLInputField;
import io.leangen.graphql.annotations.types.GraphQLType;

@SuppressFBWarnings(value = "UWF_UNWRITTEN_FIELD")
@GraphQLType(name = "CompetencyDocumentItemUnlink", description = "Arguments to unlink a competency document item from a courseware element")
public class CompetencyDocumentItemUnlinkInput extends CompetencyDocumentLinkInput {

    private List<DocumentItemInput> documentItems;
    private UUID elementId;
    private CoursewareElementType elementType;

    @GraphQLInputField(name = "elementType", description = "the courseware element type")
    public CoursewareElementType getElementType() {
        return elementType;
    }

    @GraphQLInputField(name = "documentItems", description = "list of document item to unlink")
    public List<DocumentItemInput> getDocumentItems() {
        return documentItems;
    }

    @GraphQLInputField(name = "elementId", description = "id of the courseware element to unlink")
    public UUID getElementId() {
        return elementId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CompetencyDocumentItemUnlinkInput that = (CompetencyDocumentItemUnlinkInput) o;
        return Objects.equals(documentItems, that.documentItems) &&
                Objects.equals(elementId, that.elementId) &&
                elementType == that.elementType;
    }

    @Override
    public int hashCode() {
        return Objects.hash(documentItems, elementId, elementType);
    }

    @Override
    public String toString() {
        return "CompetencyDocumentItemUnlinkInput{" +
                "documentItems=" + documentItems +
                ", elementId=" + elementId +
                ", elementType=" + elementType +
                '}';
    }
}
