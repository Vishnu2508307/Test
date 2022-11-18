package com.smartsparrow.graphql.type.mutation;

import java.util.List;
import java.util.UUID;

import com.smartsparrow.courseware.data.CoursewareElementType;

public abstract class CompetencyDocumentLinkInput {

    public abstract UUID getElementId();

    public abstract List<DocumentItemInput> getDocumentItems();

    public abstract CoursewareElementType getElementType();
}
