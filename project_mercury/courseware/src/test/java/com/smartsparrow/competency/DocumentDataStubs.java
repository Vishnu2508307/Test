package com.smartsparrow.competency;

import java.util.UUID;

import javax.annotation.Nullable;

import com.smartsparrow.competency.data.AssociationType;
import com.smartsparrow.competency.data.Document;
import com.smartsparrow.competency.data.DocumentItem;
import com.smartsparrow.competency.data.DocumentItemReference;
import com.smartsparrow.competency.data.DocumentItemTag;
import com.smartsparrow.competency.data.ItemAssociation;
import com.smartsparrow.courseware.data.CoursewareElementType;

public class DocumentDataStubs {

    public static final UUID DOCUMENT_A_ID = UUID.randomUUID();
    public static final UUID ITEM_A_ID = UUID.randomUUID();
    public static final UUID ITEM_B_ID = UUID.randomUUID();
    public static final UUID ASSOCIATION_A_B_ID = UUID.randomUUID();
    public static final UUID ASSOCIATION_B_A_ID = UUID.randomUUID();
    public static final UUID DOCUMENT_ID = UUID.randomUUID();

    public static final ItemAssociation ASSOCIATION_A_B = new ItemAssociation()
            .setId(ASSOCIATION_A_B_ID)
            .setDocumentId(DOCUMENT_A_ID)
            .setAssociationType(AssociationType.IS_CHILD_OF)
            .setOriginItemId(ITEM_A_ID)
            .setDestinationItemId(ITEM_B_ID);

    public static final ItemAssociation ASSOCIATION_B_A = new ItemAssociation()
            .setId(ASSOCIATION_B_A_ID)
            .setDocumentId(DOCUMENT_A_ID)
            .setAssociationType(AssociationType.IS_PART_OF)
            .setOriginItemId(ITEM_B_ID)
            .setDestinationItemId(ITEM_A_ID);

    public static final DocumentItem ITEM_A = new DocumentItem()
            .setId(ITEM_A_ID)
            .setDocumentId(DOCUMENT_A_ID);

    public static final DocumentItem ITEM_B = new DocumentItem()
            .setId(ITEM_B_ID)
            .setDocumentId(DOCUMENT_A_ID);

    public static DocumentItemReference buildItemReference(@Nullable UUID documentId, @Nullable UUID documentItemId) {
        final UUID docId = (documentId != null ? documentId : UUID.randomUUID());
        final UUID itemId = (documentItemId != null ? documentItemId : UUID.randomUUID());

        return new DocumentItemReference()
                .setDocumentId(docId)
                .setDocumentItemId(itemId);
    }

    public static DocumentItemReference buildItemReference() {
        return buildItemReference(null, null);
    }

    public static DocumentItemTag buildTag(UUID elementId, CoursewareElementType elementType, UUID documentId,
                                           UUID documentItemId) {
        return new DocumentItemTag()
                .setElementId(elementId)
                .setElementType(elementType)
                .setDocumentItemId(documentItemId)
                .setDocumentId(documentId);
    }

    public static Document buildDocument() {
        return new Document()
                .setId(DOCUMENT_ID)
                .setTitle("title");
    }
}
