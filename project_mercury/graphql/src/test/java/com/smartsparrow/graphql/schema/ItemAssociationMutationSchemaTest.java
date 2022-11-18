package com.smartsparrow.graphql.schema;

import static com.smartsparrow.competency.DocumentDataStubs.ASSOCIATION_A_B;
import static com.smartsparrow.competency.DocumentDataStubs.ASSOCIATION_A_B_ID;
import static com.smartsparrow.competency.DocumentDataStubs.DOCUMENT_A_ID;
import static com.smartsparrow.competency.DocumentDataStubs.ITEM_A;
import static com.smartsparrow.competency.DocumentDataStubs.ITEM_A_ID;
import static com.smartsparrow.competency.DocumentDataStubs.ITEM_B;
import static com.smartsparrow.competency.DocumentDataStubs.ITEM_B_ID;
import static com.smartsparrow.iam.IamDataStub.INSTRUCTOR_A;
import static com.smartsparrow.iam.IamDataStub.INSTRUCTOR_A_ID;
import static com.smartsparrow.iam.IamTestUtils.mockAuthenticationContext;
import static com.smartsparrow.util.ReactorTestUtils.monoErrorPublisher;
import static graphql.schema.DataFetchingEnvironmentImpl.newDataFetchingEnvironment;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.smartsparrow.competency.data.AssociationType;
import com.smartsparrow.competency.data.ItemAssociation;
import com.smartsparrow.competency.eventmessage.CompetencyDocumentBroadcastMessage;
import com.smartsparrow.competency.service.DocumentItemService;
import com.smartsparrow.competency.service.DocumentService;
import com.smartsparrow.competency.service.ItemAssociationService;
import com.smartsparrow.exception.IllegalArgumentFault;
import com.smartsparrow.exception.NotFoundFault;
import com.smartsparrow.graphql.BronteGQLContext;
import com.smartsparrow.graphql.auth.AllowDocumentContributor;
import com.smartsparrow.graphql.type.mutation.ItemAssociationCreateInput;
import com.smartsparrow.graphql.type.mutation.ItemAssociationCreatePayload;
import com.smartsparrow.graphql.type.mutation.ItemAssociationDeleteInput;
import com.smartsparrow.graphql.type.mutation.ItemAssociationDeletePayload;
import com.smartsparrow.iam.exception.PermissionFault;
import com.smartsparrow.iam.service.AuthenticationContext;
import com.smartsparrow.iam.service.MutableAuthenticationContext;
import com.smartsparrow.iam.wiring.AuthenticationContextProvider;

import io.leangen.graphql.execution.ResolutionEnvironment;
import reactor.core.publisher.Mono;

class ItemAssociationMutationSchemaTest {

    @InjectMocks
    private ItemAssociationMutationSchema itemAssociationMutationSchema;

    @Mock
    private AuthenticationContextProvider authenticationContextProvider;
    @Mock
    private ItemAssociationService itemAssociationService;
    @Mock
    private AllowDocumentContributor allowDocumentContributor;
    @Mock
    private DocumentItemService documentItemService;

    @Mock
    private ItemAssociationCreateInput itemAssociationCreateInput;
    @Mock
    private ItemAssociationDeleteInput itemAssociationDeleteInput;
    @Mock
    private DocumentService documentService;
    @Mock
    private MutableAuthenticationContext mutableAuthenticationContext;
    private ResolutionEnvironment resolutionEnvironment;

    private static final String clientId = "clientId";

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        AuthenticationContext authenticationContext = mockAuthenticationContext(INSTRUCTOR_A);
        when(authenticationContext.getClientId()).thenReturn(clientId);
        when(authenticationContextProvider.get()).thenReturn(authenticationContext);

        when(itemAssociationCreateInput.getDocumentId()).thenReturn(DOCUMENT_A_ID);
        when(itemAssociationCreateInput.getOriginItemId()).thenReturn(ITEM_A_ID);
        when(itemAssociationCreateInput.getDestinationItemId()).thenReturn(ITEM_B_ID);
        when(itemAssociationCreateInput.getAssociationType()).thenReturn(AssociationType.EXACT_MATCH_OF);

        when(itemAssociationDeleteInput.getDocumentId()).thenReturn(DOCUMENT_A_ID);
        when(itemAssociationDeleteInput.getAssociationId()).thenReturn(ASSOCIATION_A_B_ID);

        when(allowDocumentContributor.test(authenticationContext, DOCUMENT_A_ID)).thenReturn(true);

        when(documentItemService.findById(ITEM_A_ID)).thenReturn(Mono.just(ITEM_A));
        when(documentItemService.findById(ITEM_B_ID)).thenReturn(Mono.just(ITEM_B));
        when(documentService.emitEvent(any(CompetencyDocumentBroadcastMessage.class)))
                .thenReturn(Mono.just(s -> System.out.println("nothing happening here")));

        resolutionEnvironment = new ResolutionEnvironment(
                null,
                newDataFetchingEnvironment()
                        .context(new BronteGQLContext()
                                         .setMutableAuthenticationContext(mutableAuthenticationContext)
                                         .setAuthenticationContext(authenticationContext)).build(),
                null,
                null,
                null,
                null);
    }

    @Test
    void createItemAssociation_noInput() {
        IllegalArgumentFault fault = assertThrows(IllegalArgumentFault.class,
                () -> itemAssociationMutationSchema
                        .createItemAssociation(resolutionEnvironment, null)
                        .join());
        assertEquals("missing input", fault.getMessage());
    }

    @Test
    void createItemAssociation_noDocumentId() {
        when(itemAssociationCreateInput.getDocumentId()).thenReturn(null);

        IllegalArgumentFault fault = assertThrows(IllegalArgumentFault.class,
                () -> itemAssociationMutationSchema
                        .createItemAssociation(resolutionEnvironment, itemAssociationCreateInput)
                        .join());
        assertEquals("missing documentId", fault.getMessage());
    }

    @Test
    void createItemAssociation_noOriginItemId() {
        when(itemAssociationCreateInput.getOriginItemId()).thenReturn(null);

        IllegalArgumentFault fault = assertThrows(IllegalArgumentFault.class,
                () -> itemAssociationMutationSchema
                        .createItemAssociation(resolutionEnvironment, itemAssociationCreateInput)
                        .join());
        assertEquals("missing originItemId", fault.getMessage());
    }

    @Test
    void createItemAssociation_noDestinationItemId() {
        when(itemAssociationCreateInput.getDestinationItemId()).thenReturn(null);

        IllegalArgumentFault fault = assertThrows(IllegalArgumentFault.class,
                () -> itemAssociationMutationSchema
                        .createItemAssociation(resolutionEnvironment, itemAssociationCreateInput)
                        .join());
        assertEquals("missing destinationItemId", fault.getMessage());
    }

    @Test
    void createItemAssociation_noAssociationType() {
        when(itemAssociationCreateInput.getAssociationType()).thenReturn(null);

        IllegalArgumentFault fault = assertThrows(IllegalArgumentFault.class,
                () -> itemAssociationMutationSchema
                        .createItemAssociation(resolutionEnvironment, itemAssociationCreateInput)
                        .join());
        assertEquals("missing associationType", fault.getMessage());
    }

    @Test
    void createItemAssociation_noPermissions() {
        when(allowDocumentContributor.test(authenticationContextProvider.get(), DOCUMENT_A_ID)).thenReturn(false);

        PermissionFault fault = assertThrows(PermissionFault.class,
                () -> itemAssociationMutationSchema
                        .createItemAssociation(resolutionEnvironment, itemAssociationCreateInput)
                        .join());
        assertEquals("User does not have permissions to change document", fault.getMessage());
    }

    @Test
    void createItemAssociation_originItemDoesNotExist() {
        when(documentItemService.findById(ITEM_A_ID)).thenReturn(Mono.empty());

        IllegalArgumentFault fault = assertThrows(IllegalArgumentFault.class,
                () -> itemAssociationMutationSchema
                        .createItemAssociation(resolutionEnvironment, itemAssociationCreateInput)
                        .join());
        assertEquals("originItemId does not exist", fault.getMessage());
    }

    @Test
    void createItemAssociation_destinationItemDoesNotExist() {
        when(documentItemService.findById(ITEM_B_ID)).thenReturn(Mono.empty());

        IllegalArgumentFault fault = assertThrows(IllegalArgumentFault.class,
                () -> itemAssociationMutationSchema
                        .createItemAssociation(resolutionEnvironment, itemAssociationCreateInput)
                        .join());
        assertEquals("destinationItemId does not exist", fault.getMessage());
    }

    @Test
    void createItemAssociation() {
        ItemAssociation association = new ItemAssociation()
                .setId(UUID.randomUUID())
                .setDocumentId(DOCUMENT_A_ID);
        when(itemAssociationService.create(DOCUMENT_A_ID, ITEM_A_ID, ITEM_B_ID,
                AssociationType.EXACT_MATCH_OF, INSTRUCTOR_A_ID)).thenReturn(Mono.just(association));

        ItemAssociationCreatePayload result = itemAssociationMutationSchema
                .createItemAssociation(resolutionEnvironment, itemAssociationCreateInput)
                .join();

        assertNotNull(result);
        assertEquals(association, result.getAssociation());
    }

    @Test
    void deleteItemAssociation_noInput() {
        IllegalArgumentFault fault = assertThrows(IllegalArgumentFault.class,
                () -> itemAssociationMutationSchema
                        .deleteItemAssociation(resolutionEnvironment, null)
                        .join());
        assertEquals("missing input", fault.getMessage());
    }

    @Test
    void deleteItemAssociation_noDocumentId() {
        when(itemAssociationDeleteInput.getDocumentId()).thenReturn(null);

        IllegalArgumentFault fault = assertThrows(IllegalArgumentFault.class,
                () -> itemAssociationMutationSchema
                        .deleteItemAssociation(resolutionEnvironment, itemAssociationDeleteInput)
                        .join());
        assertEquals("missing documentId", fault.getMessage());
    }

    @Test
    void deleteItemAssociation_noAssociationId() {
        when(itemAssociationDeleteInput.getAssociationId()).thenReturn(null);

        IllegalArgumentFault fault = assertThrows(IllegalArgumentFault.class,
                () -> itemAssociationMutationSchema
                        .deleteItemAssociation(resolutionEnvironment, itemAssociationDeleteInput)
                        .join());
        assertEquals("missing associationId", fault.getMessage());
    }

    @Test
    void deleteItemAssociation_associationNotFound() {
        when(itemAssociationService.findById(ASSOCIATION_A_B_ID))
                .thenReturn(monoErrorPublisher(new NotFoundFault("association not found")));

        itemAssociationMutationSchema
                .deleteItemAssociation(resolutionEnvironment, itemAssociationDeleteInput)
                .handle((itemAssociationDeletePayload, throwable) -> {
                    assertEquals(NotFoundFault.class, throwable.getClass());
                    assertEquals("association not found", throwable.getMessage());
                    return itemAssociationDeletePayload;
                })
                .join();
    }

    @Test
    void deleteItemAssociation_noPermissions() {
        when(allowDocumentContributor.test(authenticationContextProvider.get(), DOCUMENT_A_ID)).thenReturn(false);

        PermissionFault fault = assertThrows(PermissionFault.class,
                () -> itemAssociationMutationSchema
                        .deleteItemAssociation(resolutionEnvironment, itemAssociationDeleteInput)
                        .join());
        assertEquals("User does not have permissions to change document", fault.getMessage());
    }

    @Test
    void deleteItemAssociation() {
        when(itemAssociationService.findById(ASSOCIATION_A_B_ID)).thenReturn(Mono.just(ASSOCIATION_A_B));
        when(itemAssociationService.delete(ASSOCIATION_A_B, INSTRUCTOR_A_ID)).thenReturn(Mono.just(ASSOCIATION_A_B));

        ItemAssociationDeletePayload result = itemAssociationMutationSchema
                .deleteItemAssociation(resolutionEnvironment, itemAssociationDeleteInput)
                .join();

        assertNotNull(result);
        assertEquals(ASSOCIATION_A_B_ID, result.getAssociationId());
        assertEquals(DOCUMENT_A_ID, result.getDocumentId());
    }
}
