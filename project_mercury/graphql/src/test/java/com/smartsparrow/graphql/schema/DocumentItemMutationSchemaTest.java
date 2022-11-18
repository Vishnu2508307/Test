package com.smartsparrow.graphql.schema;

import static graphql.schema.DataFetchingEnvironmentImpl.newDataFetchingEnvironment;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.smartsparrow.competency.data.DocumentItem;
import com.smartsparrow.competency.eventmessage.CompetencyDocumentBroadcastMessage;
import com.smartsparrow.competency.payload.DocumentItemPayload;
import com.smartsparrow.competency.service.DocumentItemService;
import com.smartsparrow.competency.service.DocumentService;
import com.smartsparrow.exception.IllegalArgumentFault;
import com.smartsparrow.graphql.BronteGQLContext;
import com.smartsparrow.graphql.auth.AllowDocumentContributor;
import com.smartsparrow.graphql.type.mutation.CreateDocumentItemInput;
import com.smartsparrow.graphql.type.mutation.DeleteDocumentItemInput;
import com.smartsparrow.graphql.type.mutation.DocumentItemMutationPayload;
import com.smartsparrow.graphql.type.mutation.UpdateDocumentItemInput;
import com.smartsparrow.iam.exception.PermissionFault;
import com.smartsparrow.iam.service.Account;
import com.smartsparrow.iam.service.AuthenticationContext;
import com.smartsparrow.iam.service.MutableAuthenticationContext;
import com.smartsparrow.iam.wiring.AuthenticationContextProvider;

import io.leangen.graphql.execution.ResolutionEnvironment;
import reactor.core.publisher.Mono;

class DocumentItemMutationSchemaTest {

    @InjectMocks
    private DocumentItemMutationSchema documentItemMutationSchema;

    @Mock
    private AuthenticationContextProvider authenticationContextProvider;

    @Mock
    private MutableAuthenticationContext mutableAuthenticationContext;

    @Mock
    private AllowDocumentContributor allowDocumentContributor;

    @Mock
    private DocumentItemService documentItemService;

    @Mock
    private DocumentService documentService;

    private ResolutionEnvironment resolutionEnvironment;
    private CreateDocumentItemInput createDocumentItemInput;
    private UpdateDocumentItemInput updateDocumentItemInput;
    private DeleteDocumentItemInput deleteDocumentItemInput;

    private static final UUID documentId = UUID.randomUUID();
    private static final UUID accountId = UUID.randomUUID();
    private static final UUID documentItemId = UUID.randomUUID();
    private static final String fullStatement = "full statement";
    private static final String abbreviatedStatement = "fs";
    private static final String humanCodingScheme = "ACD";
    private static final String clientId = "clientId";

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        createDocumentItemInput = mock(CreateDocumentItemInput.class);
        updateDocumentItemInput = mock(UpdateDocumentItemInput.class);
        deleteDocumentItemInput = mock(DeleteDocumentItemInput.class);

        when(createDocumentItemInput.getAbbreviatedStatement()).thenReturn(abbreviatedStatement);
        when(createDocumentItemInput.getFullStatement()).thenReturn(fullStatement);
        when(createDocumentItemInput.getHumanCodingScheme()).thenReturn(humanCodingScheme);
        when(createDocumentItemInput.getDocumentId()).thenReturn(documentId);

        when(updateDocumentItemInput.getAbbreviatedStatement()).thenReturn(abbreviatedStatement);
        when(updateDocumentItemInput.getFullStatement()).thenReturn(fullStatement);
        when(updateDocumentItemInput.getHumanCodingScheme()).thenReturn(humanCodingScheme);
        when(updateDocumentItemInput.getDocumentId()).thenReturn(documentId);
        when(updateDocumentItemInput.getId()).thenReturn(documentItemId);

        when(deleteDocumentItemInput.getDocumentId()).thenReturn(documentId);
        when(deleteDocumentItemInput.getId()).thenReturn(documentItemId);

        AuthenticationContext authenticationContext = mock(AuthenticationContext.class);
        when(authenticationContext.getClientId()).thenReturn(clientId);
        when(authenticationContextProvider.get()).thenReturn(authenticationContext);
        when(authenticationContext.getAccount()).thenReturn(new Account().setId(accountId));

        when(documentItemService.getDocumentItemPayload(documentItemId))
                .thenReturn(Mono.just(DocumentItemPayload.from(new DocumentItem().setDocumentId(documentId))));

        when(documentService.emitEvent(any(CompetencyDocumentBroadcastMessage.class)))
                .thenReturn(Mono.just(s -> System.out.println("nothing happening here")));

        when(allowDocumentContributor.test(authenticationContext, documentId)).thenReturn(true);

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
    void createDocumentItem_nullDocumentId() {
        IllegalArgumentFault e = assertThrows(IllegalArgumentFault.class,
                () -> documentItemMutationSchema.createDocumentItem(resolutionEnvironment, null).join());

        assertEquals("input is required", e.getMessage());
    }

    @Test
    void createDocumentItem_null_documentItem() {
        when(createDocumentItemInput.getDocumentId()).thenReturn(null);
        IllegalArgumentFault e = assertThrows(IllegalArgumentFault.class,
                () -> documentItemMutationSchema
                        .createDocumentItem(resolutionEnvironment, createDocumentItemInput)
                        .join());

        assertEquals("input.documentId is required", e.getMessage());
    }

    @Test
    void createDocumentItem_success() {

        when(documentItemService.create(eq(accountId), eq(documentId), any(DocumentItem.class)))
                .thenReturn(Mono.just(DocumentItemPayload.from(new DocumentItem().setDocumentId(documentId))));

        DocumentItemMutationPayload result = documentItemMutationSchema
                .createDocumentItem(resolutionEnvironment,createDocumentItemInput).join();

        assertNotNull(result);
    }

    @Test
    void update_success() {
        when(documentItemService.update(eq(accountId), eq(documentId), eq(documentItemId), any(DocumentItem.class)))
                .thenReturn(Mono.just(new DocumentItem().setDocumentId(documentId)));

        DocumentItemMutationPayload result = documentItemMutationSchema
                .updateDocumentItem(resolutionEnvironment,updateDocumentItemInput)
                .join();

        assertNotNull(result);
    }

    @Test
    void update_noPermission() {
        when(allowDocumentContributor.test(authenticationContextProvider.get(), documentId)).thenReturn(false);

        PermissionFault f = assertThrows(PermissionFault.class,
                () -> documentItemMutationSchema
                        .updateDocumentItem(resolutionEnvironment, updateDocumentItemInput)
                        .join());

        assertEquals("not allowed to create the document item", f.getMessage());
    }

    @Test
    void update_missingArgument() {
        IllegalArgumentFault iaf = assertThrows(IllegalArgumentFault.class,
                () -> documentItemMutationSchema.updateDocumentItem(resolutionEnvironment,null).join());

        assertEquals("input is required", iaf.getMessage());
    }

    @Test
    void delete_success() {
        when(documentItemService.delete(accountId, documentId, documentItemId))
                .thenReturn(Mono.just(new DocumentItem().setDocumentId(documentId)));

        DocumentItemMutationPayload result = documentItemMutationSchema
                .deleteDocumentItem(resolutionEnvironment, deleteDocumentItemInput)
                .join();

        assertNotNull(result);
    }

    @Test
    void delete_noPermission() {
        when(allowDocumentContributor.test(authenticationContextProvider.get(), documentId)).thenReturn(false);

        PermissionFault f = assertThrows(PermissionFault.class,
                () -> documentItemMutationSchema
                        .deleteDocumentItem(resolutionEnvironment,deleteDocumentItemInput)
                        .join());

        assertEquals("not allowed to create the document item", f.getMessage());
    }

    @Test
    void delete_missingArgument() {
        IllegalArgumentFault iaf = assertThrows(IllegalArgumentFault.class,
                () -> documentItemMutationSchema.deleteDocumentItem(resolutionEnvironment,null).join());

        assertEquals("input is required", iaf.getMessage());
    }

}
