package com.smartsparrow.rtm.message.handler;

import static com.smartsparrow.rtm.MessageHandlerTestUtils.verifySentMessage;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Base64;
import java.util.UUID;

import javax.imageio.ImageIO;

import org.apache.commons.io.IOUtils;
import org.eclipse.jetty.websocket.api.Session;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.google.common.io.BaseEncoding;
import com.google.inject.Provider;
import com.smartsparrow.iam.service.Account;
import com.smartsparrow.iam.service.AccountAvatar;
import com.smartsparrow.iam.service.AuthenticationContext;
import com.smartsparrow.iam.service.AvatarService;
import com.smartsparrow.rtm.RTMWebSocketTestUtils;
import com.smartsparrow.rtm.lang.RTMValidationException;
import com.smartsparrow.rtm.lang.WriteResponseException;
import com.smartsparrow.rtm.message.recv.SetAvatarMessage;
import com.smartsparrow.util.Images;

class SetAvatarMessageHandlerTest {

    private static final UUID ACCOUNT_ID = UUID.randomUUID();

    @InjectMocks
    private SetAvatarMessageHandler setAvatarMessageHandler;
    @Mock
    private Provider<AuthenticationContext> authenticationContextProvider;
    @Mock
    private AvatarService avatarService;
    private Session session;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.initMocks(this);
        AuthenticationContext authenticationContext = mock(AuthenticationContext.class);
        Account account = mock(Account.class);
        when(authenticationContextProvider.get()).thenReturn(authenticationContext);
        when(authenticationContext.getAccount()).thenReturn(account);
        when(account.getId()).thenReturn(ACCOUNT_ID);

        session = RTMWebSocketTestUtils.mockSession();
    }

    @Test
    void validate_noAvatar() {
        RTMValidationException result =
                assertThrows(RTMValidationException.class, () -> setAvatarMessageHandler.validate(new SetAvatarMessage()));

        assertEquals("Avatar is missing", result.getErrorMessage());
    }

    @Test
    void validate_emptyAvatar() {
        RTMValidationException result =
                assertThrows(RTMValidationException.class, () -> setAvatarMessageHandler.validate(mockMessage("")));

        assertEquals("Avatar is missing", result.getErrorMessage());
    }

    @Test
    void handle_emptyAvatar() throws WriteResponseException {
        setAvatarMessageHandler.handle(session, mockMessage("  "));

        verifySentMessage(session, "{\"type\":\"me.avatar.set.error\",\"code\":400,\"message\":\"Invalid format for avatar field\"}");
    }

    @Test
    void handle_invalidFormat() throws WriteResponseException {
        setAvatarMessageHandler.handle(session, mockMessage("dataimage/jpeg;base64,sdfsdfsf"));

        verifySentMessage(session, "{\"type\":\"me.avatar.set.error\",\"code\":400,\"message\":\"Invalid format for avatar field\"}");
    }

    @Test
    void handle_invalidData1() throws WriteResponseException {
        setAvatarMessageHandler.handle(session, mockMessage("data:image/jpeg;base64,/9j/7QBEUGhvdG9zaG9wIDMuMAA4QklNBAQAAAAAAAscAm4ABqkgTkFTEBADgAOAAA"));

        verifySentMessage(session, "{\"type\":\"me.avatar.set.error\",\"code\":422,\"message\":\"Can't upload avatar\"}");
    }

    @Test
    void handle_invalidData2() throws WriteResponseException {
        setAvatarMessageHandler.handle(session, mockMessage("data:image/jpeg;base64,sdfsdfsf"));

        verifySentMessage(session, "{\"type\":\"me.avatar.set.error\",\"code\":400,\"message\":\"Invalid format for avatar field\"}");
    }

    @Test
    void handle_invalidMimeType() throws WriteResponseException {
        setAvatarMessageHandler.handle(session, mockMessage("data:text/plain;base64,sdfsdfsf"));

        verifySentMessage(session, "{\"type\":\"me.avatar.set.error\",\"code\":415,\"message\":\"Type 'text/plain' is not supported\"}");
    }

    @Test
    void handle() throws IOException {
        String originalImage = loadImageAsString("mercury-original.jpg");

        setAvatarMessageHandler.handle(session, mockMessage("data:image/jpg;base64," + originalImage));

        verifySentMessage(session, "{\"type\":\"me.avatar.set.ok\"}");

        BufferedImage expectedOriginal = loadImage("mercury-original.jpg");
        verifySetAvatar(AccountAvatar.Size.ORIGINAL, "image/jpg", expectedOriginal,
                "Byte arrays for original image should be equal");

        BufferedImage expectedLarge = loadImage("mercury-large.jpg");
        verifySetAvatar(AccountAvatar.Size.LARGE, "image/jpg", expectedLarge,
                "Byte arrays for large image should be equal");

        BufferedImage expectedMedium = loadImage("mercury-medium.png");
        verifySetAvatar(AccountAvatar.Size.MEDIUM, "image/png", expectedMedium,
                "Byte arrays for medium image should be equal");

        BufferedImage expectedSmall = loadImage("mercury-small.png");
        verifySetAvatar(AccountAvatar.Size.SMALL, "image/png", expectedSmall,
                "Byte arrays for small image should be equal");
    }

    private void verifySetAvatar(AccountAvatar.Size size, String mimeType, BufferedImage expectedImage, String message)
            throws IOException {
        ArgumentCaptor<String> dataCapture = ArgumentCaptor.forClass(String.class);

        verify(avatarService).setAvatar(eq(ACCOUNT_ID), eq(size), eq(mimeType), dataCapture.capture(), any());

        String actualImage = dataCapture.getValue();

        byte[] decodedImg = Base64.getDecoder().decode(actualImage);
        assertTrue(Images.isSameImage(expectedImage, ImageIO.read(new ByteArrayInputStream(decodedImg))));
    }

    private BufferedImage loadImage(String name) throws IOException {
        return ImageIO.read(getClass().getClassLoader().getResourceAsStream(name));
    }

    private String loadImageAsString(String name) throws IOException {
        byte[] bytes = loadImageAsBytes(name);
        return BaseEncoding.base64().encode(bytes);
    }

    private byte[] loadImageAsBytes(String name) throws IOException {
        return IOUtils.toByteArray(getClass().getClassLoader().getResourceAsStream(name));
    }

    private static SetAvatarMessage mockMessage(String avatar) {
        SetAvatarMessage message = mock(SetAvatarMessage.class);
        when(message.getAvatar()).thenReturn(avatar);
        return message;
    }
}