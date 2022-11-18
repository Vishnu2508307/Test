package com.smartsparrow.rtm.message.handler.user_content;

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
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.smartsparrow.exception.IllegalArgumentFault;
import com.smartsparrow.rtm.RTMWebSocketTestUtils;
import com.smartsparrow.rtm.lang.RTMValidationException;
import com.smartsparrow.rtm.message.recv.user_content.FavoriteMessage;
import com.smartsparrow.user_content.service.FavoriteService;
import com.smartsparrow.util.UUIDs;

import reactor.core.publisher.Mono;

class UserFavoriteRemoveMessageHandlerTest {

    @Mock
    private FavoriteService favoriteService;

    @InjectMocks
    private UserFavoriteRemoveMessageHandler userFavoriteRemoveMessageHandler;


    private static final Session session = RTMWebSocketTestUtils.mockSession();
    final UUID accountId = UUIDs.timeBased();
    final UUID id = UUIDs.timeBased();
    final UUID rootElementId = UUIDs.timeBased();

    @BeforeEach
    private void beforeEach() {
        MockitoAnnotations.openMocks(this);


    }

    @Test
    void validate() throws RTMValidationException {
        FavoriteMessage message = mock(FavoriteMessage.class);
        when(message.getAccountId()).thenReturn(accountId);
        when(message.getFavoriteId()).thenReturn(id);
        when(message.getRootElementId()).thenReturn(rootElementId);
        userFavoriteRemoveMessageHandler.validate(message);
    }

    @Test
    void validate_accountIdNull() throws IllegalArgumentFault {
        FavoriteMessage message = mock(FavoriteMessage.class);
        when(message.getAccountId()).thenReturn(null);
        assertThrows(IllegalArgumentFault.class, () -> userFavoriteRemoveMessageHandler.validate(message));
    }

    @Test
    void validate_idNull() throws IllegalArgumentFault {
        FavoriteMessage message = mock(FavoriteMessage.class);
        when(message.getFavoriteId()).thenReturn(null);
        assertThrows(IllegalArgumentFault.class, () -> userFavoriteRemoveMessageHandler.validate(message));
    }

    @Test
    void validate_rootElementIdNull() throws IllegalArgumentFault {
        FavoriteMessage message = mock(FavoriteMessage.class);
        when(message.getRootElementId()).thenReturn(null);
        assertThrows(IllegalArgumentFault.class, () -> userFavoriteRemoveMessageHandler.validate(message));
    }

    @Test
    void handle() throws IOException {
        when(favoriteService.remove(any())).thenReturn(Mono.empty());
        FavoriteMessage message = mock(FavoriteMessage.class);
        when(message.getAccountId()).thenReturn(accountId);
        when(message.getFavoriteId()).thenReturn(id);

        userFavoriteRemoveMessageHandler.handle(session, message);

        verify(favoriteService, times(1)) //
                .remove(any());

    }
}