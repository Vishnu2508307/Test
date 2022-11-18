package com.smartsparrow.rtm.message.handler.plugin;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
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

import com.datastax.driver.core.utils.UUIDs;
import com.google.inject.Provider;
import com.smartsparrow.exception.IllegalArgumentFault;
import com.smartsparrow.iam.IamTestUtils;
import com.smartsparrow.iam.service.Account;
import com.smartsparrow.iam.service.AuthenticationContext;
import com.smartsparrow.iam.wiring.AuthenticationContextProvider;
import com.smartsparrow.plugin.lang.S3BucketLoadFileException;
import com.smartsparrow.plugin.payload.PluginPayload;
import com.smartsparrow.plugin.service.PluginService;
import com.smartsparrow.rtm.RTMWebSocketTestUtils;
import com.smartsparrow.rtm.message.recv.plugin.SyncPluginMessage;

import reactor.core.publisher.Mono;
import reactor.test.publisher.TestPublisher;

class SyncPluginMessageHandlerTest {

    @InjectMocks
    private SyncPluginMessageHandler handler;

    @Mock
    private AuthenticationContextProvider authenticationContextProvider;
    @Mock
    private PluginService pluginService;
    @Mock
    SyncPluginMessage msg;

    Session session = RTMWebSocketTestUtils.mockSession();

    UUID accountId = UUIDs.timeBased();
    UUID pluginId = UUIDs.timeBased();
    String hash = "I am a hash";

    @BeforeEach
    void setUp() {

        MockitoAnnotations.initMocks(this);
        IamTestUtils.mockAuthenticationContextProvider(authenticationContextProvider, accountId);
        when(msg.getPluginId()).thenReturn(pluginId);
        when(msg.getHash()).thenReturn(hash);
    }

    @Test
    public void validate_noPluginId() {
        when(msg.getPluginId()).thenReturn(null);
        IllegalArgumentFault t = assertThrows(IllegalArgumentFault.class, () ->
                handler.validate(msg));
        assertEquals("pluginId is required", t.getMessage());
    }

    @Test
    public void validate_noHash() {
        when(msg.getHash()).thenReturn(null);
        IllegalArgumentFault t = assertThrows(IllegalArgumentFault.class, () ->
                handler.validate(msg));
        assertEquals("hash is required", t.getMessage());
    }

    @Test
    public void validate_noThrow() {
        assertDoesNotThrow(() -> handler.validate(msg));
    }

    @Test
    public void handle() throws IOException, S3BucketLoadFileException {
        when(pluginService.syncFromRepo(eq(pluginId), eq(hash), any(Account.class)))
                .thenReturn(Mono.just(new PluginPayload()));

        handler.handle(session, msg);
        verify(pluginService).syncFromRepo(eq(pluginId), eq(hash), any(Account.class));
        verify(session.getRemote()).sendStringByFuture(contains("workspace.plugin.sync.ok"));
    }

    @Test
    public void handle_nonReactiveIOException() throws IOException, S3BucketLoadFileException {
        when(pluginService.syncFromRepo(eq(pluginId), eq(hash), any(Account.class)))
                .thenThrow(new IOException("it blew"));

        handler.handle(session, msg);

        verify(pluginService).syncFromRepo(eq(pluginId), eq(hash), any(Account.class));
        verify(session.getRemote()).sendStringByFuture(contains("workspace.plugin.sync.error"));
    }

    @Test
    public void handle_nonReactiveS3BucketLoadFileException() throws IOException, S3BucketLoadFileException {
        when(pluginService.syncFromRepo(eq(pluginId), eq(hash), any(Account.class)))
                .thenThrow(new S3BucketLoadFileException("it blew"));

        handler.handle(session, msg);

        verify(pluginService).syncFromRepo(eq(pluginId), eq(hash), any(Account.class));
        verify(session.getRemote()).sendStringByFuture(contains("workspace.plugin.sync.error"));

    }

    @Test
    public void handle_nonReactiveException() throws IOException, S3BucketLoadFileException {
        TestPublisher<PluginPayload> publisher = TestPublisher.create();
        publisher.error(new S3BucketLoadFileException("nah"));
        when(pluginService.syncFromRepo(eq(pluginId), eq(hash), any(Account.class)))
                .thenReturn(publisher.mono());

        handler.handle(session, msg);

        verify(pluginService).syncFromRepo(eq(pluginId), eq(hash), any(Account.class));
        verify(session.getRemote()).sendStringByFuture(contains("workspace.plugin.sync.error"));
    }
}
