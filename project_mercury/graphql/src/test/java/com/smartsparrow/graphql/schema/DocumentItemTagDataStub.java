package com.smartsparrow.graphql.schema;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.List;

import javax.annotation.Nonnull;

import com.smartsparrow.competency.data.DocumentItemReference;
import com.smartsparrow.competency.data.DocumentItemTag;
import com.smartsparrow.courseware.data.CoursewareElement;
import com.smartsparrow.graphql.type.mutation.CompetencyDocumentItemLinkInput;
import com.smartsparrow.graphql.type.mutation.CompetencyDocumentItemUnlinkInput;
import com.smartsparrow.graphql.type.mutation.CompetencyDocumentLinkInput;
import com.smartsparrow.graphql.type.mutation.DocumentItemInput;

public class DocumentItemTagDataStub {

    public static DocumentItemTag buildItemTag(@Nonnull CoursewareElement element, @Nonnull DocumentItemReference ref) {
        return new DocumentItemTag()
                .setDocumentId(ref.getDocumentId())
                .setDocumentItemId(ref.getDocumentItemId())
                .setElementId(element.getElementId())
                .setElementType(element.getElementType());
    }

    public static CompetencyDocumentItemLinkInput mockLinkInput(@Nonnull CoursewareElement element,
                                                                @Nonnull List<DocumentItemInput> itemInputs) {
        return _mockInput(CompetencyDocumentItemLinkInput.class, element, itemInputs);
    }

    public static CompetencyDocumentItemUnlinkInput mockUnlinkInput(@Nonnull CoursewareElement element,
                                                                    @Nonnull List<DocumentItemInput> itemInputs) {
        return _mockInput(CompetencyDocumentItemUnlinkInput.class, element, itemInputs);
    }

    private static <T extends CompetencyDocumentLinkInput> T _mockInput(@Nonnull Class<T> clazz,
                                                                       @Nonnull CoursewareElement element,
                                                                       @Nonnull List<DocumentItemInput> itemInputs) {
        T input = mock(clazz);
        when(input.getDocumentItems()).thenReturn(itemInputs);
        when(input.getElementId()).thenReturn(element.getElementId());
        when(input.getElementType()).thenReturn(element.getElementType());
        return input;
    }
}
