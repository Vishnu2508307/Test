package com.smartsparrow.competency.service;

import static com.smartsparrow.competency.DocumentDataStubs.DOCUMENT_ID;
import static com.smartsparrow.competency.DocumentDataStubs.ITEM_A_ID;
import static com.smartsparrow.competency.DocumentDataStubs.ITEM_B_ID;
import static com.smartsparrow.competency.DocumentDataStubs.buildItemReference;
import static com.smartsparrow.competency.DocumentDataStubs.buildTag;
import static com.smartsparrow.courseware.CoursewareDataStubs.ELEMENT_ID;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.google.common.collect.Lists;
import com.smartsparrow.competency.data.DocumentItemLinkGateway;
import com.smartsparrow.competency.data.DocumentItemReference;
import com.smartsparrow.competency.data.DocumentItemTag;
import com.smartsparrow.courseware.data.CoursewareElement;
import com.smartsparrow.courseware.data.CoursewareElementType;
import com.smartsparrow.courseware.service.CoursewareElementDataStub;

import reactor.core.publisher.Flux;

class DocumentItemLinkServiceTest {

    @InjectMocks
    private DocumentItemLinkService documentItemLinkService;

    @Mock
    private DocumentItemLinkGateway documentItemLinkGateway;

    private static final CoursewareElement element = CoursewareElementDataStub.build(CoursewareElementType.INTERACTIVE);
    private List<DocumentItemReference> itemReferences;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.initMocks(this);

        itemReferences = Lists.newArrayList(
                buildItemReference(DOCUMENT_ID, ITEM_A_ID),
                buildItemReference(DOCUMENT_ID, ITEM_B_ID)
        );

        when(documentItemLinkGateway.persist(any(DocumentItemTag.class))).thenReturn(Flux.just(new Void[]{}));
        when(documentItemLinkGateway.delete(any(DocumentItemTag.class))).thenReturn(Flux.just(new Void[]{}));
    }

    @Test
    void link() {
        List<DocumentItemTag> documentItemLinks = documentItemLinkService.link(element, itemReferences)
                .collectList()
                .block();

        verifyAssertions(documentItemLinks);

        verify(documentItemLinkGateway, times(2)).persist(any(DocumentItemTag.class));
    }

    @Test
    void unlink() {
        List<DocumentItemTag> documentItemLinks = documentItemLinkService.unlink(element, itemReferences)
                .collectList()
                .block();

        verifyAssertions(documentItemLinks);

        verify(documentItemLinkGateway, times(2)).delete(any(DocumentItemTag.class));
    }

    private void verifyAssertions(List<DocumentItemTag> documentItemLinks) {
        assertNotNull(documentItemLinks);
        assertEquals(2, documentItemLinks.size());

        DocumentItemTag one = documentItemLinks.get(0);
        DocumentItemTag two = documentItemLinks.get(1);

        assertEquals(DOCUMENT_ID, one.getDocumentId());
        assertEquals(DOCUMENT_ID, two.getDocumentId());
        assertEquals(ITEM_A_ID, one.getDocumentItemId());
        assertEquals(ITEM_B_ID, two.getDocumentItemId());

        assertEquals(element.getElementId(), one.getElementId());
        assertEquals(element.getElementId(), two.getElementId());
        assertEquals(CoursewareElementType.INTERACTIVE, one.getElementType());
        assertEquals(CoursewareElementType.INTERACTIVE, two.getElementType());
    }

    @Test
    void duplicateLinks() {
        when(documentItemLinkGateway.fetchByElement(ELEMENT_ID)).thenReturn(Flux.just(
                buildTag(ELEMENT_ID, CoursewareElementType.INTERACTIVE, DOCUMENT_ID, ITEM_A_ID),
                buildTag(ELEMENT_ID, CoursewareElementType.INTERACTIVE, DOCUMENT_ID, ITEM_B_ID)
        ));
        UUID newWalkableId = element.getElementId();

        List<DocumentItemTag> result = documentItemLinkService.duplicateLinks(ELEMENT_ID, newWalkableId).collectList().block();

        assertNotNull(result);
        verifyAssertions(result);
        verify(documentItemLinkGateway).persist(result.get(0));
        verify(documentItemLinkGateway).persist(result.get(1));
    }

    @Test
    void duplicateLinks_noLinks() {
        when(documentItemLinkGateway.fetchByElement(element.getElementId())).thenReturn(Flux.empty());
        UUID newWalkableId = UUID.randomUUID();

        List<DocumentItemTag> result = documentItemLinkService.duplicateLinks(element.getElementId(), newWalkableId).collectList().block();

        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(documentItemLinkGateway, never()).persist(any());
    }
}
