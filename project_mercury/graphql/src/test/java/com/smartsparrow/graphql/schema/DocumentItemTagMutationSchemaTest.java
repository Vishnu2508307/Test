package com.smartsparrow.graphql.schema;

import static com.smartsparrow.competency.DocumentDataStubs.DOCUMENT_ID;
import static com.smartsparrow.competency.DocumentDataStubs.ITEM_A_ID;
import static com.smartsparrow.competency.DocumentDataStubs.ITEM_B_ID;
import static com.smartsparrow.competency.DocumentDataStubs.buildItemReference;
import static com.smartsparrow.graphql.schema.DocumentItemTagDataStub.buildItemTag;
import static com.smartsparrow.graphql.schema.DocumentItemTagDataStub.mockLinkInput;
import static com.smartsparrow.graphql.schema.DocumentItemTagDataStub.mockUnlinkInput;
import static graphql.schema.DataFetchingEnvironmentImpl.newDataFetchingEnvironment;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.google.common.collect.Lists;
import com.smartsparrow.competency.payload.DocumentItemLinkPayload;
import com.smartsparrow.competency.payload.DocumentItemReferencePayload;
import com.smartsparrow.competency.service.DocumentItemLinkService;
import com.smartsparrow.courseware.data.CoursewareElement;
import com.smartsparrow.courseware.data.CoursewareElementType;
import com.smartsparrow.courseware.service.CoursewareElementDataStub;
import com.smartsparrow.exception.IllegalArgumentFault;
import com.smartsparrow.graphql.BronteGQLContext;
import com.smartsparrow.graphql.auth.AllowCoursewareElementContributorOrHigher;
import com.smartsparrow.graphql.auth.AllowDocumentReviewerOrHigher;
import com.smartsparrow.graphql.type.mutation.CompetencyDocumentItemLinkInput;
import com.smartsparrow.graphql.type.mutation.CompetencyDocumentItemUnlinkInput;
import com.smartsparrow.graphql.type.mutation.CompetencyDocumentLinkPayload;
import com.smartsparrow.graphql.type.mutation.DocumentItemInput;
import com.smartsparrow.iam.exception.PermissionFault;
import com.smartsparrow.iam.service.AuthenticationContext;
import com.smartsparrow.iam.service.MutableAuthenticationContext;

import io.leangen.graphql.execution.ResolutionEnvironment;
import reactor.core.publisher.Flux;

class DocumentItemTagMutationSchemaTest {

    @InjectMocks
    private DocumentItemTagMutationSchema documentItemTagMutationSchema;

    @Mock
    private AllowDocumentReviewerOrHigher allowDocumentReviewerOrHigher;

    @Mock
    private AllowCoursewareElementContributorOrHigher allowCoursewareElementContributorOrHigher;

    @Mock
    private AuthenticationContext authenticationContext;

    @Mock
    private MutableAuthenticationContext mutableAuthenticationContext;

    @Mock
    private DocumentItemLinkService documentItemLinkService;

    private ResolutionEnvironment resolutionEnvironment;
    private static final CoursewareElement interactive = CoursewareElementDataStub.build(CoursewareElementType.INTERACTIVE);
    private CompetencyDocumentItemLinkInput linkInput;
    private CompetencyDocumentItemUnlinkInput unlinkInput;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        List<DocumentItemInput> documentItemInputs = Lists.newArrayList(
                mockItemInput(DOCUMENT_ID, ITEM_A_ID),
                mockItemInput(DOCUMENT_ID, ITEM_B_ID)
        );

        when(documentItemLinkService.link(eq(interactive), any())).thenReturn(Flux.just(
                buildItemTag(interactive, buildItemReference(DOCUMENT_ID, ITEM_A_ID)),
                buildItemTag(interactive, buildItemReference(DOCUMENT_ID, ITEM_B_ID))
        ));

        when(documentItemLinkService.unlink(eq(interactive), any())).thenReturn(Flux.just(
                buildItemTag(interactive, buildItemReference(DOCUMENT_ID, ITEM_A_ID)),
                buildItemTag(interactive, buildItemReference(DOCUMENT_ID, ITEM_B_ID))
        ));

        when(allowDocumentReviewerOrHigher.test(authenticationContext, DOCUMENT_ID)).thenReturn(true);
        when(allowCoursewareElementContributorOrHigher.test(authenticationContext, interactive.getElementId(), interactive.getElementType()))
                .thenReturn(true);

        linkInput = mockLinkInput(interactive, documentItemInputs);

        unlinkInput = mockUnlinkInput(interactive, documentItemInputs);
        resolutionEnvironment= new ResolutionEnvironment(null, newDataFetchingEnvironment()
                .context(new BronteGQLContext()
                                 .setMutableAuthenticationContext(mutableAuthenticationContext)
                                 .setAuthenticationContext(authenticationContext)).build(),
                                                         null,
                                                         null,
                                                         null,
                                                         null);
    }

    @Test
    void linkCoursewareElement_invalidArgument() {
        linkInput = null;

        IllegalArgumentFault e = assertThrows(IllegalArgumentFault.class,
                () -> documentItemTagMutationSchema
                        .linkCoursewareElement(resolutionEnvironment, linkInput)
                        .join());

        assertEquals("input is required", e.getMessage());

    }

    @Test
    void linkCoursewareElement_permissionDenied() {
        when(allowDocumentReviewerOrHigher.test(authenticationContext,DOCUMENT_ID)).thenReturn(false);

        PermissionFault e = assertThrows(PermissionFault.class, () -> documentItemTagMutationSchema
                .linkCoursewareElement(resolutionEnvironment, linkInput)
                .join());
        assertTrue(e.getMessage().contains("Permission level missing or too low over document"));
    }

    @Test
    void linkCoursewareElement_success() {
        CompetencyDocumentLinkPayload payload = documentItemTagMutationSchema
                .linkCoursewareElement(resolutionEnvironment,linkInput)
                .join();

        verifyAssertions(payload);
    }

    @Test
    void unlinkCoursewareElement_invalidArgument() {
        when(unlinkInput.getDocumentItems()).thenReturn(null);

        IllegalArgumentFault e = assertThrows(IllegalArgumentFault.class,
                () -> documentItemTagMutationSchema.unlinkCoursewareElement(resolutionEnvironment, unlinkInput).join());

        assertEquals("documentItems is required", e.getMessage());
    }

    @Test
    void unlinkCoursewareElement_permissionDenied() {
        when(allowCoursewareElementContributorOrHigher.test(authenticationContext, interactive.getElementId(), interactive.getElementType()))
                .thenReturn(false);

        PermissionFault e = assertThrows(PermissionFault.class,
                () -> documentItemTagMutationSchema.unlinkCoursewareElement(resolutionEnvironment, unlinkInput).join());
        assertTrue(e.getMessage().contains("Permission level missing or too low over"));
    }

    @Test
    void unlinkCoursewareElement_success() {
        CompetencyDocumentLinkPayload paylaod = documentItemTagMutationSchema
                .unlinkCoursewareElement(resolutionEnvironment, unlinkInput)
                .join();

        verifyAssertions(paylaod);
    }

    private DocumentItemInput mockItemInput(UUID documentId, UUID documentItemId) {
        DocumentItemInput input = mock(DocumentItemInput.class);
        when(input.getDocumentId()).thenReturn(documentId);
        when(input.getDocumentItemId()).thenReturn(documentItemId);
        return input;
    }

    private void verifyAssertions(CompetencyDocumentLinkPayload paylaod) {
        assertNotNull(paylaod);
        DocumentItemLinkPayload content = paylaod.getDocumentLink();
        assertNotNull(content);
        assertEquals(interactive.getElementId(), content.getElementId());
        assertEquals(interactive.getElementType(), content.getElementType());
        assertEquals(2, content.getDocumentItems().size());

        DocumentItemReferencePayload one = content.getDocumentItems().get(0);
        DocumentItemReferencePayload two = content.getDocumentItems().get(1);

        assertEquals(DOCUMENT_ID, one.getDocumentId());
        assertEquals(DOCUMENT_ID, two.getDocumentId());
        assertEquals(ITEM_A_ID, one.getDocumentItemId());
        assertEquals(ITEM_B_ID, two.getDocumentItemId());
    }

}
