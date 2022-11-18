package com.smartsparrow.iam.service;

import java.util.Map;
import java.util.UUID;

import javax.annotation.Nullable;
import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.common.io.BaseEncoding;
import com.smartsparrow.iam.data.AccountAvatarGateway;

import reactor.core.Exceptions;
import reactor.core.publisher.Flux;

/**
 * Service to manage account avatars.
 *
 * TODO: move the managing/resizing/etc of avatars here.
 */
public class AvatarService {

    private static final Logger log = LoggerFactory.getLogger(AvatarService.class);

    private final AccountAvatarGateway avatarGateway;
    private final AccountService accountService;

    @Inject
    public AvatarService(AccountAvatarGateway avatarGateway, AccountService accountService) {
        this.avatarGateway = avatarGateway;
        this.accountService = accountService;
    }

    /**
     * Find the sized avatar for the provided account ids
     *
     * @param size the avatar size
     * @param accountIds the account ids
     * @return a {@link Flux} of the avatars
     */
    public Flux<AccountAvatar> findAvatar(AccountAvatar.Size size, UUID... accountIds) {
        Preconditions.checkArgument(size != null, "missing size");
        Preconditions.checkArgument(accountIds != null, "missing account ids");

        return accountService.findById(accountIds)
                //
                .flatMap(acct -> avatarGateway.findAvatarByAccountId(acct.getIamRegion(), acct.getId(), size));
    }

    /**
     * Fetch the Avatar information, skipping the actual data.
     *
     * @param accountId the id to fetch for
     * @return a {@link Flux} of avatars without the data
     */
    public Flux<AccountAvatar> findAvatarInfo(UUID accountId) {
        Preconditions.checkArgument(accountId != null, "missing account id");
        return accountService.findById(accountId)
                //
                .flatMap(acct -> avatarGateway.findAvatarInfoByAccountId(acct.getIamRegion(), acct.getId()));
    }

    /**
     * Set an avatar on an account
     *
     * @param accountId the account id
     * @param size the size of the avatar
     * @param mimeType the mime type of the data
     * @param data the base64 encoded data
     * @param meta meta information about the data (optional)
     */
    public void setAvatar(UUID accountId,
            AccountAvatar.Size size,
            String mimeType,
            String data,
            @Nullable Map<String, String> meta) {
        //
        Preconditions.checkArgument(accountId != null, "missing account id");
        Preconditions.checkArgument(size != null, "missing size");
        Preconditions.checkArgument(!Strings.isNullOrEmpty(mimeType), "missing mime type");
        Preconditions.checkArgument(!Strings.isNullOrEmpty(data), "missing data");

        //
        Account account = accountService.verifyValidAccount(accountId);

        AccountAvatar avatar = new AccountAvatar().setAccountId(account.getId())
                .setIamRegion(account.getIamRegion())
                .setName(size)
                .setMimeType(mimeType)
                .setData(data)
                .setMeta(meta);

        try {
            avatarGateway.persistBlocking(avatar);
            //
            String msg = String.format("Add %s avatar", avatar.getName());
            accountService.addLogEntry(account, AccountLogEntry.Action.PII_CHANGE, null, msg);
        } catch (Throwable t) {
            log.error("an unexpected error occurred", t);
            throw Exceptions.bubble(t);
        }

    }

    /**
     * Set an avatar on an account
     *
     * @param accountId the account id
     * @param size the size of the avatar
     * @param mimeType the mime type of the data
     * @param data the raw binary data
     * @param meta meta information about the data (optional)
     */
    public void setAvatar(UUID accountId,
            AccountAvatar.Size size,
            String mimeType,
            byte[] data,
            @Nullable Map<String, String> meta) {
        //
        Preconditions.checkArgument(data != null && data.length > 0, "missing data");
        // encode the binary data
        String base64Data = BaseEncoding.base64().encode(data);
        setAvatar(accountId, size, mimeType, base64Data, meta);
    }

    /**
     * Remove an avatar from an account
     *
     * @param accountId the account id
     * @param size the size of the avatar
     */
    public void removeAvatar(UUID accountId, AccountAvatar.Size size) {
        Preconditions.checkArgument(accountId != null, "missing account id");
        Preconditions.checkArgument(size != null, "missing size");

        Account account = accountService.verifyValidAccount(accountId);

        // underlying gateway only requires the fields set below.
        AccountAvatar avatar = new AccountAvatar() //
                .setAccountId(account.getId()) //
                .setIamRegion(account.getIamRegion()) //
                .setName(size);

        try {
            avatarGateway.deleteBlocking(avatar);
            //
            String msg = String.format("Removed %s avatar", avatar.getName());
            accountService.addLogEntry(avatar.getAccountId(), AccountLogEntry.Action.PII_CHANGE, null, msg);
        } catch (Throwable t) {
            log.error("an unexpected error occurred", t);
            throw Exceptions.propagate(t);
        }
    }
}
