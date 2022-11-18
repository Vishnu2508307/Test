package com.smartsparrow.graphql.schema;

import static com.smartsparrow.iam.IamDataStub.INSTRUCTOR_A;
import static com.smartsparrow.iam.IamDataStub.INSTRUCTOR_A_ID;
import static com.smartsparrow.iam.IamTestUtils.mockAuthenticationContext;
import static graphql.schema.DataFetchingEnvironmentImpl.newDataFetchingEnvironment;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.atMostOnce;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.UUID;

import org.apache.camel.Exchange;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.datastax.driver.core.utils.UUIDs;
import com.smartsparrow.competency.data.Document;
import com.smartsparrow.competency.data.DocumentItem;
import com.smartsparrow.competency.eventmessage.CompetencyDocumentAction;
import com.smartsparrow.competency.service.DocumentItemService;
import com.smartsparrow.competency.service.DocumentService;
import com.smartsparrow.exception.IllegalArgumentFault;
import com.smartsparrow.exception.NotFoundFault;
import com.smartsparrow.graphql.BronteGQLContext;
import com.smartsparrow.graphql.auth.AllowDocumentContributor;
import com.smartsparrow.graphql.auth.AllowWorkspaceContributorOrHigher;
import com.smartsparrow.graphql.type.mutation.CompetencyDocumentCreateInput;
import com.smartsparrow.graphql.type.mutation.CompetencyDocumentCreatePayload;
import com.smartsparrow.graphql.type.mutation.CompetencyDocumentDeleteInput;
import com.smartsparrow.graphql.type.mutation.CompetencyDocumentMutationPayload;
import com.smartsparrow.graphql.type.mutation.CompetencyDocumentUpdateInput;
import com.smartsparrow.iam.exception.PermissionFault;
import com.smartsparrow.iam.service.AuthenticationContext;
import com.smartsparrow.iam.service.MutableAuthenticationContext;
import com.smartsparrow.iam.wiring.AuthenticationContextProvider;

import io.leangen.graphql.execution.ResolutionEnvironment;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.publisher.TestPublisher;

class DocumentMutationSchemaTest {

    @InjectMocks
    private DocumentMutationSchema documentSchema;

    @Mock
    private AuthenticationContextProvider authenticationContextProvider;
    @Mock
    private DocumentService documentService;
    @Mock
    private AllowWorkspaceContributorOrHigher allowWorkspaceContributorOrHigher;
    @Mock
    private AllowDocumentContributor allowDocumentContributor;
    @Mock
    private CompetencyDocumentCreateInput competencyDocumentCreateInput;
    @Mock
    private MutableAuthenticationContext mutableAuthenticationContext;

    @Mock
    private DocumentItemService documentItemService;

    private ResolutionEnvironment resolutionEnvironment;

    @Mock
    private CompetencyDocumentDeleteInput competencyDocumentDeleteInput;

    @Mock
    private CompetencyDocumentUpdateInput competencyDocumentUpdateInput;

    private static final UUID workspaceId = UUID.randomUUID();
    private static final UUID documentId = UUIDs.timeBased();
    private final TestPublisher<Exchange> exchangeTestPublisher = TestPublisher.create();
    private Document document;
    private static final String updatedTitle = "Updated Title";

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        AuthenticationContext authenticationContext = mockAuthenticationContext(INSTRUCTOR_A);
        when(authenticationContextProvider.get()).thenReturn(authenticationContext);

        when(competencyDocumentCreateInput.getTitle()).thenReturn("Chemistry Competency");
        when(competencyDocumentCreateInput.getWorkspaceId()).thenReturn(workspaceId);

        when(allowWorkspaceContributorOrHigher.test(authenticationContext, workspaceId)).thenReturn(true);
        when(allowDocumentContributor.test(authenticationContextProvider.get(), documentId)).thenReturn(true);

        resolutionEnvironment= new ResolutionEnvironment(null, newDataFetchingEnvironment()
                .context(new BronteGQLContext()
                                 .setMutableAuthenticationContext(mutableAuthenticationContext)
                                 .setAuthenticationContext(authenticationContext)).build(),
                                                         null,
                                                         null,
                                                         null,
                                                         null);
        document = new Document()
                .setId(documentId)
                .setWorkspaceId(workspaceId);


        when(documentService.fetchDocument(documentId)).thenReturn(Mono.just(document));
        when(competencyDocumentDeleteInput.getWorkspaceId()).thenReturn(workspaceId);
        when(competencyDocumentDeleteInput.getDocumentId()).thenReturn(documentId);

        when(documentItemService.isItemPublished(any(UUID.class)))
                .thenReturn(Mono.just(false));
        when(documentItemService.isItemLinked(any(UUID.class)))
                .thenReturn(Mono.just(false));

        when(documentService.deleteAccountCollaborators(documentId)).thenReturn(Flux.empty());
        when(documentService.deleteTeamCollaborators(documentId)).thenReturn(Flux.empty());

        when(documentService.update(document)).thenReturn(Mono.just(document));

        when(documentService.broadcastDocumentMessage(documentId, CompetencyDocumentAction.DOCUMENT_DELETED))
                .thenReturn(Mono.empty());

        when(competencyDocumentUpdateInput.getWorkspaceId()).thenReturn(workspaceId);
        when(competencyDocumentUpdateInput.getDocumentId()).thenReturn(documentId);
        when(competencyDocumentUpdateInput.getTitle()).thenReturn(updatedTitle);
    }

    @Test
    void createCompetencyDocument() {
        Document document = new Document()
                .setId(UUIDs.timeBased())
                .setTitle("Chemistry Competency")
                .setWorkspaceId(workspaceId)
                .setCreatedAt(UUIDs.timeBased());

        when(documentService.create("Chemistry Competency", workspaceId, INSTRUCTOR_A_ID)).thenReturn(Mono.just(document));

        CompetencyDocumentCreatePayload result = documentSchema
                .createCompetencyDocument(resolutionEnvironment,competencyDocumentCreateInput).join();

        assertNotNull(result);
        assertNotNull(result.getDocument());
        assertEquals(document.getId(), result.getDocument().getDocumentId());
        assertEquals(document.getTitle(), result.getDocument().getTitle());
        assertEquals(document.getWorkspaceId(), result.getDocument().getWorkspaceId());
        assertNotNull(document.getCreatedAt());
    }

    @Test
    void createCompetencyDocument_emptyTitle() {
        when(competencyDocumentCreateInput.getTitle()).thenReturn("");

        IllegalArgumentFault fault = assertThrows(IllegalArgumentFault.class,
                () -> documentSchema.createCompetencyDocument(resolutionEnvironment,competencyDocumentCreateInput).join());
        assertEquals("missing title", fault.getMessage());
    }

    @Test
    void createCompetencyDocument_noTitle() {
        when(competencyDocumentCreateInput.getTitle()).thenReturn(null);

        IllegalArgumentFault fault = assertThrows(IllegalArgumentFault.class,
                () -> documentSchema.createCompetencyDocument(resolutionEnvironment,competencyDocumentCreateInput).join());
        assertEquals("missing title", fault.getMessage());
    }

    @Test
    void createCompetencyDocument_noWorkspace() {
        when(competencyDocumentCreateInput.getWorkspaceId()).thenReturn(null);

        IllegalArgumentFault fault = assertThrows(IllegalArgumentFault.class,
                () -> documentSchema.createCompetencyDocument(resolutionEnvironment,competencyDocumentCreateInput).join());
        assertEquals("missing workspaceId", fault.getMessage());
    }

    @Test
    void createCompetencyDocument_noPermissions() {
        when(allowWorkspaceContributorOrHigher.test(authenticationContextProvider.get(), workspaceId)).thenReturn(false);

        PermissionFault fault = assertThrows(PermissionFault.class,
                () -> documentSchema.createCompetencyDocument(resolutionEnvironment,competencyDocumentCreateInput).join());
        assertEquals("User does not have permissions to change workspace", fault.getMessage());
    }

    @Test
    void deleteCompetencyDocument() {
        when(documentItemService.findByDocumentId(documentId))
                .thenReturn(Flux.just(new DocumentItem().setId(UUID.randomUUID())));

        CompetencyDocumentMutationPayload result = documentSchema.deleteCompetencyDocument(resolutionEnvironment, competencyDocumentDeleteInput)
                .join();

        assertNotNull(result);
        assertNotNull(result.getDocument());
        assertNotNull(result.getDocument().getLastChangedAt());

        assertEquals(documentId, result.getDocument().getDocumentId());
        assertEquals(workspaceId, result.getDocument().getWorkspaceId());
    }

    @Test
    void deleteCompetencyDocument_noItem() {
        when(documentItemService.findByDocumentId(documentId))
                .thenReturn(Flux.empty());

        CompetencyDocumentMutationPayload result = documentSchema.deleteCompetencyDocument(resolutionEnvironment, competencyDocumentDeleteInput)
                .join();

        assertNotNull(result);
        assertNotNull(result.getDocument());
        assertNotNull(result.getDocument().getLastChangedAt());

        assertEquals(documentId, result.getDocument().getDocumentId());
        assertEquals(workspaceId, result.getDocument().getWorkspaceId());
    }

    @Test
    void deleteCompetencyDocument_notFound() {
        when(documentService.fetchDocument(documentId)).thenReturn(Mono.empty());

        NotFoundFault f = assertThrows(NotFoundFault.class,
                () -> documentSchema.deleteCompetencyDocument(resolutionEnvironment, competencyDocumentDeleteInput));

        assertEquals("cannot find the document by document id: " + documentId, f.getMessage());
    }

    @Test
    void deleteCompetencyDocument_noWorkspaceId() {
        when(competencyDocumentDeleteInput.getWorkspaceId()).thenReturn(null);

        IllegalArgumentFault fault = assertThrows(IllegalArgumentFault.class,
                () -> documentSchema.deleteCompetencyDocument(resolutionEnvironment, competencyDocumentDeleteInput));
        assertEquals("workspaceId is required", fault.getMessage());
    }

    @Test
    void deleteCompetencyDocument_noDocumentId() {
        when(competencyDocumentDeleteInput.getDocumentId()).thenReturn(null);

        IllegalArgumentFault fault = assertThrows(IllegalArgumentFault.class,
                () -> documentSchema.deleteCompetencyDocument(resolutionEnvironment, competencyDocumentDeleteInput));
        assertEquals("documentId is required", fault.getMessage());
    }

    @Test
    void deleteCompetencyDocument_noPermission() {
        when(documentItemService.findByDocumentId(documentId))
                .thenReturn(Flux.empty());

        when(allowDocumentContributor.test(authenticationContextProvider.get(), documentId)).thenReturn(false);

        PermissionFault fault = assertThrows(PermissionFault.class,
                () -> documentSchema.deleteCompetencyDocument(resolutionEnvironment, competencyDocumentDeleteInput));
        assertEquals("User does not have permissions to delete the document", fault.getMessage());
    }

    @Test
    void updateCompetencyDocument() {
        when(documentService.update(any(Document.class))).thenReturn(Mono.just(document));
        when(documentService.broadcastDocumentMessage(documentId, CompetencyDocumentAction.DOCUMENT_UPDATED))
                .thenReturn(Mono.empty());

        CompetencyDocumentMutationPayload result = documentSchema.updateCompetencyDocument(resolutionEnvironment, competencyDocumentUpdateInput)
                .join();

        assertNotNull(result);
        assertNotNull(result.getDocument());
        assertNotNull(result.getDocument().getLastChangedAt());

        verify(documentService, atMostOnce()).update(any(Document.class));

        assertEquals(documentId, result.getDocument().getDocumentId());
        assertEquals(workspaceId, result.getDocument().getWorkspaceId());
        assertEquals(updatedTitle, result.getDocument().getTitle());
    }

    @Test
    void updateCompetencyDocument_noDocumentFound() {

        when(documentService.fetchDocument(documentId)).thenReturn(Mono.empty());

        NotFoundFault f = assertThrows(NotFoundFault.class,
                () -> documentSchema.deleteCompetencyDocument(resolutionEnvironment, competencyDocumentDeleteInput));

        assertEquals("cannot find the document by document id: " + documentId, f.getMessage());

        verify(documentService, never()).update(any(Document.class));
    }

    @Test
    void updateCompetencyDocument_noWorkspaceId() {
        when(competencyDocumentUpdateInput.getWorkspaceId()).thenReturn(null);

        IllegalArgumentFault fault = assertThrows(IllegalArgumentFault.class,
                () -> documentSchema.updateCompetencyDocument(resolutionEnvironment, competencyDocumentUpdateInput));
        assertEquals("workspaceId is required", fault.getMessage());
    }

    @Test
    void updateCompetencyDocument_noDocumentId() {
        when(competencyDocumentUpdateInput.getDocumentId()).thenReturn(null);

        IllegalArgumentFault fault = assertThrows(IllegalArgumentFault.class,
                () -> documentSchema.updateCompetencyDocument(resolutionEnvironment, competencyDocumentUpdateInput));
        assertEquals("documentId is required", fault.getMessage());
    }

    @Test
    void updateCompetencyDocument_noTitle() {
        when(competencyDocumentUpdateInput.getTitle()).thenReturn(null);

        IllegalArgumentFault fault = assertThrows(IllegalArgumentFault.class,
                () -> documentSchema.updateCompetencyDocument(resolutionEnvironment, competencyDocumentUpdateInput));
        assertEquals("document title is required", fault.getMessage());
    }

    @Test
    void updateCompetencyDocument_noPermission() {
        when(allowDocumentContributor.test(authenticationContextProvider.get(), documentId)).thenReturn(false);

        PermissionFault fault = assertThrows(PermissionFault.class,
                () -> documentSchema.updateCompetencyDocument(resolutionEnvironment, competencyDocumentUpdateInput));
        assertEquals("User does not have permissions to update the document", fault.getMessage());
    }
}
