package com.smartsparrow.rtm.message.handler.user_content;

import static com.smartsparrow.rtm.MessageHandlerTestUtils.verifySentMessage;
import static com.smartsparrow.rtm.message.handler.user_content.UserSharedResourceCreateMessageHandler.USER_CONTENT_SHARED_RESOURCE_CREATE_OK;
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
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.smartsparrow.exception.IllegalArgumentFault;
import com.smartsparrow.rtm.RTMWebSocketTestUtils;
import com.smartsparrow.rtm.lang.RTMValidationException;
import com.smartsparrow.rtm.message.recv.user_content.SharedResourceMessage;
import com.smartsparrow.user_content.data.ResourceType;
import com.smartsparrow.user_content.service.SharedResourceService;
import com.smartsparrow.util.UUIDs;

import reactor.core.publisher.Mono;

class UserSharedResourceCreateMessageHandlerTest {

    @Mock
    private SharedResourceService sharedResourceService;

    @InjectMocks
    private UserSharedResourceCreateMessageHandler userSharedResourceCreateMessageHandler;


    private static final Session session = RTMWebSocketTestUtils.mockSession();
    final UUID sharedAccountId = UUIDs.timeBased();
    final UUID accountId = UUIDs.timeBased();
    final UUID resourceId = UUIDs.timeBased();
    final ResourceType resourceType = ResourceType.COURSE;

    @BeforeEach
    private void beforeEach() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void validate() throws RTMValidationException {
        SharedResourceMessage message = mock(SharedResourceMessage.class);
        when(message.getAccountId()).thenReturn(accountId);
        when(message.getSharedAccountId()).thenReturn(sharedAccountId);
        when(message.getResourceType()).thenReturn(resourceType);
        when(message.getResourceId()).thenReturn(resourceId);

        userSharedResourceCreateMessageHandler.validate(message);
    }

    @Test
    void validate_accountIdNull() throws IllegalArgumentFault {
        SharedResourceMessage message = mock(SharedResourceMessage.class);
        when(message.getAccountId()).thenReturn(null);
        when(message.getSharedAccountId()).thenReturn(sharedAccountId);
        when(message.getResourceType()).thenReturn(resourceType);
        when(message.getResourceId()).thenReturn(resourceId);

        assertThrows(IllegalArgumentFault.class, () -> userSharedResourceCreateMessageHandler.validate(message));
    }

    @Test
    void validate_sharedAccountIdNull() throws IllegalArgumentFault {
        SharedResourceMessage message = mock(SharedResourceMessage.class);
        when(message.getAccountId()).thenReturn(accountId);
        when(message.getSharedAccountId()).thenReturn(null);
        when(message.getResourceType()).thenReturn(resourceType);
        when(message.getResourceId()).thenReturn(resourceId);

        assertThrows(IllegalArgumentFault.class, () -> userSharedResourceCreateMessageHandler.validate(message));
    }

    @Test
    void validate_resourceTypeNull() throws IllegalArgumentFault {
        SharedResourceMessage message = mock(SharedResourceMessage.class);
        when(message.getAccountId()).thenReturn(accountId);
        when(message.getSharedAccountId()).thenReturn(sharedAccountId);
        when(message.getResourceType()).thenReturn(null);
        when(message.getResourceId()).thenReturn(resourceId);

        assertThrows(IllegalArgumentFault.class, () -> userSharedResourceCreateMessageHandler.validate(message));
    }
    @Test
    void validate_ResourceIdNull() throws IllegalArgumentFault {
        SharedResourceMessage message = mock(SharedResourceMessage.class);
        when(message.getAccountId()).thenReturn(accountId);
        when(message.getSharedAccountId()).thenReturn(sharedAccountId);
        when(message.getResourceType()).thenReturn(resourceType);
        when(message.getResourceId()).thenReturn(null);

        assertThrows(IllegalArgumentFault.class, () -> userSharedResourceCreateMessageHandler.validate(message));
    }

    @Test
    void handle() throws IOException {
        when(sharedResourceService.create(any(UUID.class),
                                                  any(UUID.class),
                                                  any(UUID.class),
                                                  any(ResourceType.class))).thenReturn(Mono.empty());
        SharedResourceMessage message = mock(SharedResourceMessage.class);
        when(message.getAccountId()).thenReturn(accountId);
        when(message.getResourceType()).thenReturn(resourceType);
        when(message.getResourceId()).thenReturn(resourceId);
        when(message.getSharedAccountId()).thenReturn(sharedAccountId);


        userSharedResourceCreateMessageHandler.handle(session, message);

        verify(sharedResourceService, times(1)) //
                .create(any(UUID.class),
                                any(UUID.class),
                                any(UUID.class),
                                any(ResourceType.class));

        verifySentMessage(session, response -> {
            assertEquals(USER_CONTENT_SHARED_RESOURCE_CREATE_OK, response.getType());
        });
    }
}