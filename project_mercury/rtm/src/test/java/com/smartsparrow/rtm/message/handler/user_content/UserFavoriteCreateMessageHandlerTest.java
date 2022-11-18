package com.smartsparrow.rtm.message.handler.user_content;

import static com.smartsparrow.rtm.MessageHandlerTestUtils.verifySentMessage;
import static com.smartsparrow.rtm.message.handler.user_content.UserFavoriteCreateMessageHandler.USER_CONTENT_FAVORITE_CREATE_OK;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.util.UUID;

import org.eclipse.jetty.websocket.api.Session;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.smartsparrow.exception.IllegalArgumentFault;
import com.smartsparrow.rtm.RTMWebSocketTestUtils;
import com.smartsparrow.rtm.lang.RTMValidationException;
import com.smartsparrow.rtm.message.recv.user_content.FavoriteMessage;
import com.smartsparrow.user_content.data.Favorite;
import com.smartsparrow.user_content.data.ResourceType;
import com.smartsparrow.user_content.service.FavoriteService;
import com.smartsparrow.util.UUIDs;

import reactor.core.publisher.Mono;

class UserFavoriteCreateMessageHandlerTest {

    @Mock
    private FavoriteService favoriteService;

    @InjectMocks
    private UserFavoriteCreateMessageHandler userFavoriteCreateMessageHandler;


    private static final Session session = RTMWebSocketTestUtils.mockSession();
    final UUID activityId = UUIDs.timeBased();
    final UUID workspaceId = UUIDs.timeBased();
    final UUID accountId = UUIDs.timeBased();
    final UUID projectId = UUIDs.timeBased();
    final UUID rootElementId = UUIDs.timeBased();
    final UUID documentId = UUIDs.timeBased();
    final ResourceType resourceType = ResourceType.COURSE;

    @BeforeEach
    private void beforeEach() {
        MockitoAnnotations.openMocks(this);


    }

    @Test
    void validate() throws RTMValidationException {
        FavoriteMessage message = mock(FavoriteMessage.class);
        when(message.getAccountId()).thenReturn(accountId);
        when(message.getRootElementId()).thenReturn(rootElementId);
        when(message.getResourceType()).thenReturn(resourceType);
        when(message.getWorkspaceId()).thenReturn(workspaceId);
        when(message.getProjectId()).thenReturn(projectId);

        userFavoriteCreateMessageHandler.validate(message);
    }

    @Test
    void validate_accountIdNull() throws IllegalArgumentFault {
        FavoriteMessage message = mock(FavoriteMessage.class);
        when(message.getAccountId()).thenReturn(null);
        when(message.getRootElementId()).thenReturn(rootElementId);
        when(message.getResourceType()).thenReturn(resourceType);
        when(message.getWorkspaceId()).thenReturn(workspaceId);

        assertThrows(IllegalArgumentFault.class, () -> userFavoriteCreateMessageHandler.validate(message));
    }

    @Test
    void validate_rootElementIdNull() throws IllegalArgumentFault {
        FavoriteMessage message = mock(FavoriteMessage.class);
        when(message.getAccountId()).thenReturn(accountId);
        when(message.getRootElementId()).thenReturn(null);
        when(message.getResourceType()).thenReturn(resourceType);
        when(message.getWorkspaceId()).thenReturn(workspaceId);

        assertThrows(IllegalArgumentFault.class, () -> userFavoriteCreateMessageHandler.validate(message));
    }

    @Test
    void validate_resourceTypeNull() throws IllegalArgumentFault {
        FavoriteMessage message = mock(FavoriteMessage.class);
        when(message.getAccountId()).thenReturn(accountId);
        when(message.getRootElementId()).thenReturn(rootElementId);
        when(message.getResourceType()).thenReturn(null);
        when(message.getWorkspaceId()).thenReturn(workspaceId);

        assertThrows(IllegalArgumentFault.class, () -> userFavoriteCreateMessageHandler.validate(message));
    }
    @Test
    void validate_WorkSpaceNull() throws IllegalArgumentFault {
        FavoriteMessage message = mock(FavoriteMessage.class);
        when(message.getAccountId()).thenReturn(accountId);
        when(message.getRootElementId()).thenReturn(rootElementId);
        when(message.getResourceType()).thenReturn(resourceType);
        when(message.getWorkspaceId()).thenReturn(null);

        assertThrows(IllegalArgumentFault.class, () -> userFavoriteCreateMessageHandler.validate(message));
    }

    @ParameterizedTest
    @ValueSource(strings = {"COURSE", "LESSON"})
    void handle(String resourceType) throws IOException {
        Favorite favorite = new Favorite();
        favorite.setId(UUID.randomUUID())
                .setAccountId(accountId)
                .setActivityId(activityId)
                .setDocumentId(documentId)
                .setResourceType(ResourceType.valueOf(resourceType))
                .setRootElementId(rootElementId)
                .setWorkspaceId(workspaceId)
                .setProjectId(projectId);
        when(favoriteService.createFavorite(activityId,
                                            workspaceId,
                                            accountId,
                                            projectId,
                                            rootElementId,
                                            documentId,
                                            ResourceType.valueOf(resourceType))).thenReturn(Mono.just(favorite));
        FavoriteMessage message = mock(FavoriteMessage.class);
        when(message.getAccountId()).thenReturn(accountId);
        when(message.getRootElementId()).thenReturn(rootElementId);
        when(message.getResourceType()).thenReturn(ResourceType.valueOf(resourceType));
        when(message.getWorkspaceId()).thenReturn(workspaceId);
        when(message.getProjectId()).thenReturn(projectId);
        when(message.getActivityId()).thenReturn(activityId);
        when(message.getDocumentId()).thenReturn(documentId);


        userFavoriteCreateMessageHandler.handle(session, message);

        verify(favoriteService, times(1)) //
                .createFavorite(activityId,
                                workspaceId,
                                accountId,
                                projectId,
                                rootElementId,
                                documentId,
                                ResourceType.valueOf(resourceType));

        verifySentMessage(session, response -> {
            assertEquals(USER_CONTENT_FAVORITE_CREATE_OK, response.getType());
        });
    }
}