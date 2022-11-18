package com.smartsparrow.graphql.schema;

import static graphql.schema.DataFetchingEnvironmentImpl.newDataFetchingEnvironment;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.smartsparrow.graphql.BronteGQLContext;
import com.smartsparrow.iam.service.AccountAvatar;
import com.smartsparrow.iam.service.AccountIdentityAttributes;
import com.smartsparrow.iam.service.AuthenticationContext;
import com.smartsparrow.iam.service.AvatarService;
import com.smartsparrow.iam.service.MutableAuthenticationContext;
import com.smartsparrow.iam.service.Region;

import io.leangen.graphql.execution.ResolutionEnvironment;
import reactor.core.publisher.Flux;

public class AvatarSchemaTest {

    @InjectMocks
    private AvatarSchema avatarSchema;

    @Mock
    private AvatarService avatarService;

    @Mock
    private AuthenticationContext authenticationContext;

    @Mock
    private MutableAuthenticationContext mutableAuthenticationContext;

    @Mock
    private AccountIdentityAttributes identityAttributes;

    private AccountAvatar accountAvatar1 = new AccountAvatar()
            .setAccountId(UUID.randomUUID())
            .setData("Some data")
            .setIamRegion(Region.GLOBAL);

    private AccountAvatar accountAvatar2 = new AccountAvatar()
            .setAccountId(UUID.randomUUID())
            .setData("Some Random data")
            .setIamRegion(Region.GLOBAL);


    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void avatarsFromAccountIdentity_nullSize() {

        when(avatarService.findAvatar(any(), any())).thenReturn(Flux.just(accountAvatar1, accountAvatar2));

        List<AccountAvatar> accountAvatars = avatarSchema
                .avatarsFromAccountIdentity(identityAttributes, null).join();
        assertNotNull(accountAvatars);
        assertEquals(8, accountAvatars.size());
    }

    @Test
    void avatarsFromAccountIdentity_SmallSize() {

        when(avatarService.findAvatar(any(), any()))
                .thenReturn(Flux.just(accountAvatar1.setName(AccountAvatar.Size.SMALL),
                                      accountAvatar2.setName(AccountAvatar.Size.SMALL)));

        List<AccountAvatar> accountAvatars = avatarSchema
                .avatarsFromAccountIdentity(identityAttributes, AccountAvatar.Size.SMALL).join();
        assertNotNull(accountAvatars);
        assertEquals(2, accountAvatars.size());
        accountAvatars.forEach(accountAvatar -> assertEquals(AccountAvatar.Size.SMALL, accountAvatar.getName()));
    }

    @Test
    void avatarsFromAccountIdentity_LargeSize() {

        when(avatarService.findAvatar(any(), any()))
                .thenReturn(Flux.just(accountAvatar1.setName(AccountAvatar.Size.LARGE),
                                      accountAvatar2.setName(AccountAvatar.Size.LARGE)));

        List<AccountAvatar> accountAvatars = avatarSchema
                .avatarsFromAccountIdentity(identityAttributes, AccountAvatar.Size.LARGE).join();
        assertNotNull(accountAvatars);
        assertEquals(2, accountAvatars.size());
        accountAvatars.forEach(accountAvatar -> assertEquals(AccountAvatar.Size.LARGE, accountAvatar.getName()));
    }

    @Test
    void avatarsFromAccountIdentity_MediumSize() {

        when(avatarService.findAvatar(any(), any()))
                .thenReturn(Flux.just(accountAvatar1.setName(AccountAvatar.Size.MEDIUM),
                                      accountAvatar2.setName(AccountAvatar.Size.MEDIUM)));

        List<AccountAvatar> accountAvatars = avatarSchema
                .avatarsFromAccountIdentity(identityAttributes, AccountAvatar.Size.MEDIUM).join();
        assertNotNull(accountAvatars);
        assertEquals(2, accountAvatars.size());
        accountAvatars.forEach(accountAvatar -> assertEquals(AccountAvatar.Size.MEDIUM, accountAvatar.getName()));
    }

    @Test
    void avatarsFromAccountIdentity_OriginalSize() {

        when(avatarService.findAvatar(any(), any()))
                .thenReturn(Flux.just(accountAvatar1.setName(AccountAvatar.Size.ORIGINAL),
                                      accountAvatar2.setName(AccountAvatar.Size.ORIGINAL)));

        List<AccountAvatar> accountAvatars = avatarSchema
                .avatarsFromAccountIdentity(identityAttributes, AccountAvatar.Size.ORIGINAL).join();
        assertNotNull(accountAvatars);
        assertEquals(2, accountAvatars.size());
        accountAvatars.forEach(accountAvatar -> assertEquals(AccountAvatar.Size.ORIGINAL, accountAvatar.getName()));
    }

}
