package com.smartsparrow.iam.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Matchers.anyVararg;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Map;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.datastax.driver.core.utils.UUIDs;
import com.google.common.collect.ImmutableMap;
import com.google.common.io.BaseEncoding;
import com.smartsparrow.iam.data.AccountAvatarGateway;

import reactor.core.publisher.Flux;

class AvatarServiceTest {

    @Mock
    private AccountService accountService;

    @Mock
    private AccountAvatarGateway avatarGateway;

    private AvatarService avatarService;

    //
    private UUID validAccountId = UUIDs.timeBased();
    private UUID invalidAccountId = UUIDs.timeBased();
    private AccountAvatar thumbnail;
    private Region region = Region.GLOBAL;

    //
    private AccountAvatar.Size size = AccountAvatar.Size.SMALL;
    private String mimeType = "image/gif";
    private String base64Data = "R0lGODlhAQABAIAAAAAAAP///yH5BAEAAAAALAAAAAABAAEAAAIBRAA7";
    private byte[] data = BaseEncoding.base64().decode(base64Data);
    private Map<String, String> meta = ImmutableMap.of("width", "1", "height", "1");

    @BeforeEach
    void init() {
        MockitoAnnotations.initMocks(this);

        avatarService = new AvatarService(avatarGateway, accountService);

        thumbnail = new AccountAvatar() //
                .setAccountId(validAccountId) //
                .setIamRegion(region) //
                .setName(size) //
                .setMimeType(mimeType) //
                .setData(base64Data) //
                .setMeta(meta);

        Account account = new Account()
                //
                .setId(validAccountId).setIamRegion(region);

        when(accountService.findById(anyVararg())).thenReturn(Flux.empty());
        when(accountService.findById(validAccountId)).thenReturn(Flux.just(account));
        when(accountService.verifyValidAccount(validAccountId)).thenReturn(account);
        when(accountService.verifyValidAccount(invalidAccountId)).thenThrow(new IllegalArgumentException());
        when(accountService.findById(validAccountId, invalidAccountId)).thenReturn(Flux.just(account));
        when(avatarGateway.findAvatarByAccountId(region, validAccountId, size)).thenReturn(Flux.just(thumbnail));
    }

    @Test
    void findAvatar() {
        Flux<AccountAvatar> avatar = avatarService.findAvatar(size, validAccountId);
        AccountAvatar result = avatar.blockFirst();
        assertSame(result, thumbnail);
    }

    @Test
    void findAvatar_ignore_invalid_account() {
        Flux<AccountAvatar> avatar = avatarService.findAvatar(size, validAccountId, invalidAccountId);
        AccountAvatar result = avatar.blockFirst();
        assertNotNull(result);
    }

    @Test
    void findAvatar_single_invalid_account() {
        Flux<AccountAvatar> avatar = avatarService.findAvatar(size, invalidAccountId);
        assertNull(avatar.blockFirst());
    }

    @Test
    void findAvatar_accounts_empty() {
        Flux<AccountAvatar> avatar = avatarService.findAvatar(size, new UUID[0]);
        assertNull(avatar.blockFirst());
    }

    @Test
    void findAvatar_account_null() {
        assertThrows(IllegalArgumentException.class, () -> avatarService.findAvatar(size, (UUID[]) null));
    }

    @Test
    void findAvatar_size_required() {
        assertThrows(IllegalArgumentException.class, () -> avatarService.findAvatar(null, UUIDs.random()));
    }

    @SuppressWarnings("Duplicates")
    @Test
    void setAvatar() {
        avatarService.setAvatar(validAccountId, size, mimeType, base64Data, meta);

        verify(accountService).verifyValidAccount(validAccountId);
        ArgumentCaptor<AccountAvatar> avatarCaptor = ArgumentCaptor.forClass(AccountAvatar.class);
        verify(avatarGateway).persistBlocking(avatarCaptor.capture());
        verify(accountService, atLeastOnce()).addLogEntry(any(Account.class), any(AccountLogEntry.Action.class),
                eq(null), any(String.class));

        AccountAvatar actual = avatarCaptor.getValue();
        assertEquals(actual.getAccountId(), validAccountId);
        assertEquals(actual.getName(), size);
        assertEquals(actual.getMimeType(), mimeType);
        assertEquals(actual.getData(), base64Data);
        assertEquals(actual.getMeta(), meta);
    }

    @Test
    void setAvatar_account_invalid() {
        assertThrows(IllegalArgumentException.class, () -> {
            avatarService.setAvatar(invalidAccountId, size, mimeType, base64Data, meta);
        });
        verify(accountService).verifyValidAccount(invalidAccountId);
        verify(avatarGateway, never()).persistBlocking(any(AccountAvatar.class));
        verify(accountService, never()).addLogEntry(Matchers.eq(validAccountId), any(AccountLogEntry.Action.class),
                any(UUID.class), any(String.class));
    }

    @Test
    void setAvatar_account_required() {
        assertThrows(IllegalArgumentException.class, () -> {
            avatarService.setAvatar(null, size, mimeType, base64Data, meta);
        });
    }

    @Test
    void setAvatar_size_required() {
        assertThrows(IllegalArgumentException.class, () -> {
            avatarService.setAvatar(validAccountId, null, mimeType, base64Data, meta);
        });
    }

    @Test
    void setAvatar_mime_required() {
        assertThrows(IllegalArgumentException.class, () -> {
            avatarService.setAvatar(validAccountId, size, null, base64Data, meta);
        });
    }

    @Test
    void setAvatar_data_required() {
        assertThrows(IllegalArgumentException.class, () -> {
            avatarService.setAvatar(validAccountId, size, mimeType, (String) null, meta);
        });
    }

    @Test
    void setAvatar_meta_optional() {
        avatarService.setAvatar(validAccountId, size, mimeType, base64Data, null);

        verify(accountService).verifyValidAccount(validAccountId);
        ArgumentCaptor<AccountAvatar> avatarCaptor = ArgumentCaptor.forClass(AccountAvatar.class);
        verify(avatarGateway).persistBlocking(avatarCaptor.capture());
        verify(accountService, atLeastOnce()).addLogEntry(any(Account.class), any(AccountLogEntry.Action.class),
                eq(null), any(String.class));

        AccountAvatar actual = avatarCaptor.getValue();
        assertEquals(actual.getAccountId(), validAccountId);
        assertEquals(actual.getName(), size);
        assertEquals(actual.getMimeType(), mimeType);
        assertEquals(actual.getData(), base64Data);
        assertEquals(actual.getMeta(), null);
    }

    @SuppressWarnings("Duplicates")
    @Test
    void setAvatar_binary() {
        avatarService.setAvatar(validAccountId, size, mimeType, data, meta);

        verify(accountService).verifyValidAccount(validAccountId);
        ArgumentCaptor<AccountAvatar> avatarCaptor = ArgumentCaptor.forClass(AccountAvatar.class);
        verify(avatarGateway).persistBlocking(avatarCaptor.capture());
        verify(accountService, atLeastOnce()).addLogEntry(any(Account.class), any(AccountLogEntry.Action.class),
                eq(null), any(String.class));

        AccountAvatar actual = avatarCaptor.getValue();
        assertEquals(actual.getAccountId(), validAccountId);
        assertEquals(actual.getName(), size);
        assertEquals(actual.getMimeType(), mimeType);
        assertEquals(actual.getData(), base64Data);
        assertEquals(actual.getMeta(), meta);
    }

    @Test
    void setAvatar_binary_account_invalid() {
        assertThrows(IllegalArgumentException.class, () -> {
            avatarService.setAvatar(invalidAccountId, size, mimeType, data, meta);
        });
        verify(accountService).verifyValidAccount(invalidAccountId);
        verify(avatarGateway, never()).persistBlocking(any(AccountAvatar.class));
        verify(accountService, never()).addLogEntry(Matchers.eq(validAccountId), any(AccountLogEntry.Action.class),
                any(UUID.class), any(String.class));
    }

    @Test
    void setAvatar_binary_account_required() {
        assertThrows(IllegalArgumentException.class, () ->{
            avatarService.setAvatar(null, size, mimeType, data, meta);
        });
    }

    @Test
    void setAvatar_binary_size_required() {
        assertThrows(IllegalArgumentException.class, () ->{
            avatarService.setAvatar(validAccountId, null, mimeType, data, meta);
        });
    }

    @Test
    void setAvatar_binary_size_mime_required() {
        assertThrows(IllegalArgumentException.class, () ->{
            avatarService.setAvatar(validAccountId, size, null, data, meta);
        });
    }

    @Test
    void setAvatar_binary_data_required() {
        assertThrows(IllegalArgumentException.class, () ->{
            avatarService.setAvatar(validAccountId, size, mimeType, (byte[]) null, meta);
        });
    }

    @Test
    void removeAvatar() {
        avatarService.removeAvatar(validAccountId, size);

        verify(accountService).verifyValidAccount(validAccountId);
        verify(avatarGateway).deleteBlocking(any(AccountAvatar.class));
        verify(accountService, atLeastOnce()).addLogEntry(Matchers.eq(validAccountId), any(AccountLogEntry.Action.class),
                eq(null), any(String.class));
    }

    @Test
    void removeAvatar_account_invalid() {
        assertThrows(IllegalArgumentException.class, () ->{
            avatarService.removeAvatar(invalidAccountId, size);
        });

        verify(accountService).verifyValidAccount(invalidAccountId);
        verify(avatarGateway, never()).deleteBlocking(any(AccountAvatar.class));
        verify(accountService, never()).addLogEntry(Matchers.eq(validAccountId), any(AccountLogEntry.Action.class),
                any(UUID.class), any(String.class));
    }

    @Test
    void removeAvatar_account_required() {
        assertThrows(IllegalArgumentException.class, ()->{
            avatarService.removeAvatar(null, size);
        });
    }

    @Test
    void removeAvatar_size_required() {
        assertThrows(IllegalArgumentException.class, ()->{
            avatarService.removeAvatar(validAccountId, null);
        });
    }
}
