package com.smartsparrow.rtm.message.handler.math;

import static com.smartsparrow.courseware.data.CoursewareElementType.ACTIVITY;
import static com.smartsparrow.rtm.RTMWebSocketTestUtils.mockSession;
import static com.smartsparrow.rtm.message.handler.math.MathAssetCreateMessageHandler.AUTHOR_MATH_ASSET_CREATE_ERROR;
import static com.smartsparrow.rtm.message.handler.math.MathAssetCreateMessageHandler.AUTHOR_MATH_ASSET_CREATE_OK;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.util.UUID;

import org.eclipse.jetty.websocket.api.Session;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.smartsparrow.asset.data.AssetUrn;
import com.smartsparrow.exception.IllegalArgumentFault;
import com.smartsparrow.iam.service.Account;
import com.smartsparrow.iam.service.AuthenticationContext;
import com.smartsparrow.iam.wiring.AuthenticationContextProvider;
import com.smartsparrow.math.service.MathAssetService;
import com.smartsparrow.rtm.MessageHandlerTestUtils;
import com.smartsparrow.rtm.message.recv.math.MathAssetCreateMessage;

import reactor.core.publisher.Mono;
import reactor.test.publisher.TestPublisher;

public class MathAssetCreateMessageHandlerTest {

    @InjectMocks
    private MathAssetCreateMessageHandler handler;

    @Mock
    private MathAssetService mathAssetService;

    @Mock
    private MathAssetCreateMessage message;

    @Mock
    private AuthenticationContextProvider authenticationContextProvider;

    @Mock
    private AuthenticationContext authenticationContext;

    @Mock
    private Account account;

    @Mock
    private AssetUrn assetUrn;

    private Session session;

    private static final String mathML = "<math><mn>1</mn><mo>-</mo><mn>2</mn></math>";
    private static final String altText = "alt text";
    private static final UUID accountId = UUID.randomUUID();
    private static final UUID elementId = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        session = mockSession();

        when(message.getMathML()).thenReturn(mathML);
        when(message.getAltText()).thenReturn(altText);
        when(message.getElementId()).thenReturn(elementId);
        when(message.getElementType()).thenReturn(ACTIVITY);
        when(authenticationContextProvider.get()).thenReturn(authenticationContext);
        when(authenticationContext.getAccount()).thenReturn(account);
        when(account.getId()).thenReturn(accountId);
    }

    @Test
    void validate_noMathML() {
        when(message.getMathML()).thenReturn(null);

        IllegalArgumentFault ex = assertThrows(IllegalArgumentFault.class, () -> handler.validate(message));
        assertEquals("mathML is required", ex.getMessage());
    }

    @Test
    void validate_noElementId() {
        when(message.getElementId()).thenReturn(null);

        IllegalArgumentFault ex = assertThrows(IllegalArgumentFault.class, () -> handler.validate(message));
        assertEquals("elementId is required", ex.getMessage());
    }

    @Test
    void validate_noElementType() {
        when(message.getElementType()).thenReturn(null);

        IllegalArgumentFault ex = assertThrows(IllegalArgumentFault.class, () -> handler.validate(message));
        assertEquals("elementType is required", ex.getMessage());
    }

    @Test
    void handle() throws IOException {
        when(mathAssetService.createMathAsset(eq(mathML),
                                              eq(altText),
                                              eq(elementId),
                                              eq(accountId)))
                .thenReturn(Mono.just(assetUrn));

        handler.handle(session, message);

        MessageHandlerTestUtils.verifySentMessage(session, response -> {
            assertAll(() -> {
                assertEquals(AUTHOR_MATH_ASSET_CREATE_OK, response.getType());
            });
        });
    }

    @Test
    void handle_exception() throws IOException {
        TestPublisher<AssetUrn> error = TestPublisher.create();
        error.error(new RuntimeException("can't create"));
        when(mathAssetService.createMathAsset(eq(mathML),
                                              eq(altText),
                                              eq(elementId),
                                              eq(accountId)))
                .thenReturn(error.mono());

        handler.handle(session, message);

        MessageHandlerTestUtils.verifySentMessage(session,
                                                  "{\"type\":\"" + AUTHOR_MATH_ASSET_CREATE_ERROR + "\",\"code\":422," +
                                                          "\"message\":\"error creating Math asset\"}");
    }
}
