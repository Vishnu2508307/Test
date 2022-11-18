package com.smartsparrow.graphql.schema;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.smartsparrow.iam.service.AccountAvatar;
import com.smartsparrow.iam.service.AccountIdentityAttributes;
import com.smartsparrow.iam.service.AvatarService;

import io.leangen.graphql.annotations.GraphQLArgument;
import io.leangen.graphql.annotations.GraphQLContext;
import io.leangen.graphql.annotations.GraphQLQuery;
import reactor.core.publisher.Flux;

@Singleton
public class AvatarSchema {

    private final AvatarService avatarService;

    @Inject
    public AvatarSchema(AvatarService avatarService) {
        this.avatarService = avatarService;
    }

    /**
     * Expose the avatar under an Account Identity attribute.
     *
     * @param identityAttributes the account identity
     * @param size the size of the avatar, optional, by default all.
     * @return the avatars.
     */
    @GraphQLQuery(name = "avatars", description = "Account avatars")
    public CompletableFuture<List<AccountAvatar>> avatarsFromAccountIdentity(@GraphQLContext AccountIdentityAttributes identityAttributes,
                                                                             @GraphQLArgument(name = "size", description = "fetch a specific size (optional) [default: all]") AccountAvatar.Size size) {

        // size is an optional parameter.
        List<AccountAvatar.Size> sizes;
        if (size != null) {
            sizes = Collections.singletonList(size);
        } else {
            // all!
            sizes = Arrays.asList(AccountAvatar.Size.values());
        }

        // fetch all the sizes, convert to a list.
        return Flux.fromIterable(sizes) //
                .flatMap(avatarSize -> avatarService.findAvatar(avatarSize, identityAttributes.getAccountId())) //
                .collectList()
                .toFuture();
    }
}
