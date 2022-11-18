package com.smartsparrow.graphql.schema;

import static graphql.schema.DataFetchingEnvironmentImpl.newDataFetchingEnvironment;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.datastax.driver.core.utils.UUIDs;
import com.smartsparrow.competency.data.Document;
import com.smartsparrow.competency.service.DocumentService;
import com.smartsparrow.graphql.BronteGQLContext;
import com.smartsparrow.graphql.auth.AllowDocumentReviewerOrHigher;
import com.smartsparrow.graphql.type.Learn;
import com.smartsparrow.iam.exception.PermissionFault;
import com.smartsparrow.iam.service.Account;
import com.smartsparrow.iam.service.AuthenticationContext;
import com.smartsparrow.iam.service.MutableAuthenticationContext;
import com.smartsparrow.iam.wiring.AuthenticationContextProvider;
import com.smartsparrow.learner.data.LearnerDocument;
import com.smartsparrow.learner.payload.LearnerDocumentPayload;
import com.smartsparrow.learner.service.LearnerCompetencyDocumentService;
import com.smartsparrow.workspace.data.Workspace;

import io.leangen.graphql.execution.ResolutionEnvironment;
import io.leangen.graphql.execution.relay.Page;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public class DocumentSchemaTest {

    @InjectMocks
    private DocumentSchema documentSchema;

    @Mock
    private DocumentService documentService;

    @Mock
    private AllowDocumentReviewerOrHigher allowDocumentReviewerOrHigher;

    @Mock
    private AuthenticationContextProvider authenticationContextProvider;

    @Mock
    private MutableAuthenticationContext mutableAuthenticationContext;

    private ResolutionEnvironment resolutionEnvironment;

    @Mock
    private LearnerCompetencyDocumentService learnerCompetencyDocumentService;

    private UUID workspaceId = UUID.randomUUID();
    private UUID workspaceId1 = UUID.randomUUID();
    private UUID documentId1 = UUID.randomUUID();
    private UUID documentId2 = UUID.randomUUID();
    private UUID accountId = UUID.randomUUID();

    private Document document1, document2;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        document1 = new Document()
                .setId(documentId1)
                .setCreatedAt(UUID.randomUUID())
                .setCreatedBy(UUID.randomUUID())
                .setModifiedAt(UUID.randomUUID())
                .setModifiedBy(UUID.randomUUID())
                .setTitle("document 1")
                .setWorkspaceId(workspaceId);

        document2 = new Document()
                .setId(documentId2)
                .setCreatedAt(UUID.randomUUID())
                .setCreatedBy(UUID.randomUUID())
                .setModifiedAt(UUID.randomUUID())
                .setModifiedBy(UUID.randomUUID())
                .setTitle("document 2")
                .setWorkspaceId(workspaceId1);

        AuthenticationContext authenticationContext = mock(AuthenticationContext.class);
        when(authenticationContext.getAccount()).thenReturn(new Account().setId(accountId));
        when(authenticationContextProvider.get()).thenReturn(authenticationContext);
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
    void test_NoDocumentPermissionsWhenDocumentIdSupplied() {

        when(allowDocumentReviewerOrHigher.test(any(), any(UUID.class))).thenReturn(false);

        PermissionFault e = assertThrows(PermissionFault.class, () -> documentSchema.getDocuments(resolutionEnvironment,
                                                                                                  new Workspace()
                                                                                                          .setId(workspaceId),
                                                                                                  documentId1,
                                                                                                  null,
                                                                                                  null).join());

        assertEquals("User does not have permissions to view document", e.getMessage());
    }

    @Test
    void test_Param_noDocumentFound() {
        when(allowDocumentReviewerOrHigher.test(any(), any(UUID.class))).thenReturn(true);

        when(documentService.fetchDocument(documentId1)).thenReturn(Mono.empty());

        Page<Document> document = documentSchema.getDocuments(resolutionEnvironment, new Workspace(),
                documentId1,
                null,
                null).join();

        assertNotNull(document);
        assertNotNull(document.getEdges());
        assertEquals(0, document.getEdges().size());
    }

    @Test
    void test_FromContext_noDocumentFound() {
        when(documentService.fetchDocuments(accountId)).thenReturn(Flux.empty());

        Page<Document> document = documentSchema.getDocuments(resolutionEnvironment, new Workspace()
                        .setId(workspaceId),
                null,
                null,
                null).join();

        assertNotNull(document);
        assertNotNull(document.getEdges());
        assertEquals(0, document.getEdges().size());
    }

    @Test
    void test_FromContext_DocumentsFound() {

        when(documentService.fetchDocuments(accountId)).thenReturn(Flux.just(document1, document2));

        Page<Document> document = documentSchema.getDocuments(resolutionEnvironment, new Workspace()
                        .setId(workspaceId),
                null,
                null,
                null).join();

        assertNotNull(document);
        assertNotNull(document.getEdges());
        assertEquals(1, document.getEdges().size());
        assertNotNull(document.getEdges().get(0));
        assertNotNull(document.getEdges().get(0).getNode());
        assertEquals(document1, document.getEdges().get(0).getNode());
    }


    @Test
    void test_Param_DocumentsFound() {

        when(allowDocumentReviewerOrHigher.test(any(), any(UUID.class))).thenReturn(true);

        when(documentService.fetchDocument(any(UUID.class)))
                .thenReturn(Mono.just(document2));

        Page<Document> document = documentSchema.getDocuments(resolutionEnvironment,new Workspace(),
                documentId2,
                null,
                null).join();

        assertNotNull(document);
        assertNotNull(document.getEdges());
        assertEquals(1, document.getEdges().size());
        assertNotNull(document.getEdges().get(0));
        assertNotNull(document.getEdges().get(0).getNode());
        assertEquals(document2, document.getEdges().get(0).getNode());
    }

    @Test
    void getPublishedDocument() {
        LearnerDocument document = new LearnerDocument()
                .setId(documentId1)
                .setCreatedAt(UUIDs.timeBased());

        Learn learn = mock(Learn.class);
        when(learnerCompetencyDocumentService.findDocument(documentId1)).thenReturn(Mono.just(document));

        LearnerDocumentPayload found = documentSchema.getPublishedDocument(learn, documentId1).join();

        assertNotNull(found);
        assertEquals(documentId1, found.getDocumentId());
    }

}
