package com.smartsparrow.rtm.message.handler;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.UUID;

import javax.inject.Inject;

import org.eclipse.jetty.http.HttpStatus;
import org.eclipse.jetty.websocket.api.Session;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.ImmutableMap;
import com.google.inject.Provider;
import com.smartsparrow.iam.service.Account;
import com.smartsparrow.iam.service.AccountAvatar;
import com.smartsparrow.iam.service.AuthenticationContext;
import com.smartsparrow.iam.service.AvatarService;
import com.smartsparrow.rtm.lang.RTMValidationException;
import com.smartsparrow.rtm.lang.WriteResponseException;
import com.smartsparrow.rtm.message.MessageHandler;
import com.smartsparrow.rtm.message.recv.SetAvatarMessage;
import com.smartsparrow.rtm.message.send.BasicResponseMessage;
import com.smartsparrow.rtm.message.send.ErrorMessage;
import com.smartsparrow.rtm.util.Responses;
import com.smartsparrow.util.Images;

public class SetAvatarMessageHandler implements MessageHandler<SetAvatarMessage> {

    private static final Logger log = LoggerFactory.getLogger(SetAvatarMessage.class);

    private static final int LARGE_THUMBNAIL_WIDTH_PX = 550;
    private static final int MEDIUM_THUMBNAIL_SIZE_PX = 160;
    private static final int SMALL_THUMBNAIL_SIZE_PX = 68;
    private static final String AVATAR_CONTENT_TYPE = "image/png";

    public static final String ME_AVATAR_SET = "me.avatar.set";
    public static final String ME_AVATAR_SET_OK = "me.avatar.set.ok";
    public static final String ME_AVATAR_SET_ERROR = "me.avatar.set.error";

    private final Provider<AuthenticationContext> authenticationContextProvider;
    private final AvatarService avatarService;

    @Inject
    public SetAvatarMessageHandler(Provider<AuthenticationContext> authenticationContextProvider,
                                   AvatarService avatarService) {
        this.authenticationContextProvider = authenticationContextProvider;
        this.avatarService = avatarService;
    }

    @Override
    public void validate(SetAvatarMessage message) throws RTMValidationException {
        if (message.getAvatar() == null || message.getAvatar().isEmpty()) {
            throw new RTMValidationException("Avatar is missing", message.getId(), ME_AVATAR_SET_ERROR);
        }
    }

    @Override
    public void handle(Session session, SetAvatarMessage message) throws WriteResponseException {
        Account account = authenticationContextProvider.get().getAccount();

        Images.ImageData avatarPayload;
        try {
            avatarPayload = Images.parse(message.getAvatar());
        } catch (IllegalArgumentException e) {
            if (log.isDebugEnabled()) {
                log.debug("Error uploading avatar image for account {}: " + e.getMessage(), account.getId());
            }
            emitError(session, message.getId(), "Invalid format for avatar field", HttpStatus.BAD_REQUEST_400);
            return;
        }

        String data = avatarPayload.getData();
        String mimeType = avatarPayload.getMimeType();

        if (!Images.isValidMimeType(mimeType)) {
            String reason = String.format("Type '%s' is not supported", mimeType);
            if (log.isDebugEnabled()) {
                log.debug(getErrorMessage(reason));
            }
            emitError(session, message.getId(), reason, HttpStatus.UNSUPPORTED_MEDIA_TYPE_415);
            return;
        }

        try {
            BufferedImage originalImage = Images.readFromString(data);

            if (originalImage == null) {
                if (log.isDebugEnabled()) {
                    log.debug(getErrorMessage("Original image can't be null"));
                }
                emitError(session, message.getId(), "Invalid format for avatar field", HttpStatus.BAD_REQUEST_400);
                return;
            }

            int originalWidth = originalImage.getWidth();
            int originalHeight = originalImage.getHeight();

            //save original image
            saveAvatar(AccountAvatar.Size.ORIGINAL, mimeType, data, originalWidth, originalHeight);

            //save large thumbnail (not sure if we need it in CDP, maybe remove later)
            if (originalWidth < LARGE_THUMBNAIL_WIDTH_PX) {
                saveAvatar(AccountAvatar.Size.LARGE, mimeType, data, originalWidth, originalHeight);
            } else {
                BufferedImage largeBufferedImage = Images.rescaleImage(originalImage, LARGE_THUMBNAIL_WIDTH_PX, -1);
                String largeThumbData = Images.writeToString(largeBufferedImage, mimeType);
                saveAvatar(AccountAvatar.Size.LARGE, mimeType, largeThumbData, LARGE_THUMBNAIL_WIDTH_PX, largeBufferedImage.getHeight());
            }

            // save medium thumbnail
            BufferedImage mediumThumbnail = Images.rescaleImageToPng(originalImage, MEDIUM_THUMBNAIL_SIZE_PX, MEDIUM_THUMBNAIL_SIZE_PX);
            String mediumThumbData = Images.writeToString(mediumThumbnail, AVATAR_CONTENT_TYPE);
            saveAvatar(AccountAvatar.Size.MEDIUM, AVATAR_CONTENT_TYPE, mediumThumbData, MEDIUM_THUMBNAIL_SIZE_PX, MEDIUM_THUMBNAIL_SIZE_PX);

            //save small thumbnail
            BufferedImage smallThumbnail = Images.rescaleImageToPng(originalImage, SMALL_THUMBNAIL_SIZE_PX, SMALL_THUMBNAIL_SIZE_PX);
            String smallThumbData = Images.writeToString(smallThumbnail, AVATAR_CONTENT_TYPE);
            saveAvatar(AccountAvatar.Size.SMALL, AVATAR_CONTENT_TYPE, smallThumbData, SMALL_THUMBNAIL_SIZE_PX, SMALL_THUMBNAIL_SIZE_PX);

        } catch (RuntimeException e) {
            if (log.isDebugEnabled()) {
                log.debug(getErrorMessage(e.getMessage()), e);
            }
            emitError(session, message.getId(), "Invalid format for avatar field", HttpStatus.BAD_REQUEST_400);
            return;
        } catch (IOException e) {
            log.error(getErrorMessage(e.getMessage()), e);
            emitError(session, message.getId(), "Can't upload avatar", HttpStatus.UNPROCESSABLE_ENTITY_422);
            return;
        }

        BasicResponseMessage response = new BasicResponseMessage(ME_AVATAR_SET_OK, message.getId());
        Responses.write(session, response);

    }

    private void saveAvatar(AccountAvatar.Size size, String mimeType, String data, int width, int height) {
        Account account = authenticationContextProvider.get().getAccount();
        avatarService.setAvatar(account.getId(), size, mimeType, data,
                ImmutableMap.of("width", Integer.toString(width), "height", Integer.toString(height)));
    }

    private void emitError(Session session, String inMessageId, String reason, int code) throws WriteResponseException {
        ErrorMessage error = new ErrorMessage(ME_AVATAR_SET_ERROR);
        error.setReplyTo(inMessageId);
        error.setCode(code);
        error.setMessage(reason);
        Responses.write(session, error);
    }

    private String getErrorMessage(String details) {
        UUID accountId = authenticationContextProvider.get().getAccount().getId();
        return String.format("Error uploading avatar image for account %s: %s", accountId, details);
    }
}
