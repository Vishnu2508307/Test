package com.smartsparrow.graphql.schema;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.smartsparrow.courseware.data.Activity;
import com.smartsparrow.courseware.data.PluginReference;
import com.smartsparrow.exception.IllegalArgumentFault;
import com.smartsparrow.graphql.type.LTIContext;
import com.smartsparrow.graphql.type.LTILaunchRequestParam;
import com.smartsparrow.graphql.type.LTISignedLaunch;
import com.smartsparrow.graphql.type.LTIToolProviderContext;
import com.smartsparrow.plugin.data.LTIProviderCredential;
import com.smartsparrow.plugin.data.PluginSummary;
import com.smartsparrow.plugin.service.PluginService;

import reactor.core.publisher.Mono;

class LTIToolProviderSchemaTest {

    @InjectMocks
    private LTIToolProviderSchema ltiToolProviderSchema;

    @Mock
    private LTIToolProviderContext ltiToolProviderContext;

    @Mock
    private PluginService pluginService;

    private static final UUID pluginId = UUID.randomUUID();
    private static final PluginReference pluginRef = new Activity().setPluginId(pluginId).setPluginVersionExpr("1.0.0");
    LTIProviderCredential ltiProviderCredential = new LTIProviderCredential();
    private Set<String> allowedFields = new HashSet<>();

    @BeforeEach
    void setUp() {
        allowedFields.add("userName");
        allowedFields.add("signedSecret");
        allowedFields.add("callbackURL");
        allowedFields.add("Id");
        MockitoAnnotations.openMocks(this);
        PluginSummary pluginSummary = new PluginSummary();
        pluginSummary.setId(pluginId).setName("Test");
        LTIContext ltiContext = new LTIContext(pluginSummary);
        when(ltiToolProviderContext.getLtiContext()).thenReturn(ltiContext);

        ltiProviderCredential.setId(UUID.randomUUID()).setPluginId(pluginId).setKey("Test")
                .setSecret("Secret").setAllowedFields(allowedFields);
        when(pluginService.findLTIProviderCredential(any(), anyString())).thenReturn(Mono.just(ltiProviderCredential));
    }

    @Test
    void getLTIProviderPlugin_withNoLTIProviderCredentialEntry() {
        when(pluginService.findLTIProviderCredential(any(), anyString())).thenReturn(Mono.empty());
        ltiToolProviderSchema
                .signLaunch(ltiToolProviderContext,
                            "testing",
                            "https://www.pearson.com/lti",
                            new ArrayList<LTILaunchRequestParam>())
                        .handle((ltiSignedLaunch, throwable) -> {
                            assertEquals(IllegalArgumentFault.class, throwable.getClass());
                            return ltiSignedLaunch;
                        })
                .join();
    }


    @Test
    void getLTIProviderPlugin_withInvalidURI() {
        when(pluginService.findLTIProviderCredential(any(), anyString())).thenReturn(Mono.empty());
        ltiToolProviderSchema
                .signLaunch(ltiToolProviderContext,
                            "testing",
                            "http://www.pearson.com/and&&&&here.html",
                            new ArrayList<LTILaunchRequestParam>())
                        .handle((ltiSignedLaunch, throwable) -> {
                            assertEquals(IllegalArgumentFault.class, throwable.getClass());
                            return ltiSignedLaunch;
                        })
                .join();
    }


    @Test
    void getLTIProviderPlugin_withInvalidKey() {
        when(pluginService.findLTIProviderCredential(any(), anyString())).thenReturn(Mono.empty());
        assertThrows(IllegalArgumentFault.class,
                     () -> ltiToolProviderSchema
                             .signLaunch(ltiToolProviderContext,
                                         null,
                                         "https://www.pearson.comtes/lti",
                                         new ArrayList<LTILaunchRequestParam>())
                             .join());
    }

    @Test
    void getLTIProviderPlugin_withEmptyURL() {
        when(pluginService.findLTIProviderCredential(any(), anyString())).thenReturn(Mono.empty());
        assertThrows(IllegalArgumentFault.class,
                     () -> ltiToolProviderSchema
                             .signLaunch(ltiToolProviderContext,
                                         "testing",
                                         "",
                                         new ArrayList<LTILaunchRequestParam>())
                             .join());
    }

    @Test
    void getLTIProviderPlugin_withNullInURL() {
        when(pluginService.findLTIProviderCredential(any(), anyString())).thenReturn(Mono.empty());
        assertThrows(IllegalArgumentFault.class,
                     () -> ltiToolProviderSchema
                             .signLaunch(ltiToolProviderContext,
                                         "testing",
                                         null,
                                         new ArrayList<LTILaunchRequestParam>())
                             .join());
    }

    @Test
    void getLTIProviderPlugin_withoutLTIProviderCredentialEntry() {
        when(pluginService.findLTIProviderCredential(any(), anyString())).thenReturn(Mono.just(ltiProviderCredential));
        LTISignedLaunch ltiSignedLaunch = ltiToolProviderSchema
                .signLaunch(ltiToolProviderContext,
                            "testing",
                            "https://www.pearson.com/lti",
                            new ArrayList<LTILaunchRequestParam>())
                .join();

        assertEquals(ltiSignedLaunch.getUrl(), "https://www.pearson.com/lti");
        assertNotNull(ltiSignedLaunch.getMethod(), "POST");
        assertNotNull(ltiSignedLaunch.getFormParameters());
        assertEquals(ltiSignedLaunch.getFormParameters().size(), 6);
        assertEquals(ltiSignedLaunch.getFormParameters().get("oauth_version"), "1.0");
        assertEquals(ltiSignedLaunch.getFormParameters().get("oauth_signature_method"), "HMAC-SHA1");
        assertNotNull(ltiSignedLaunch.getFormParameters().get("oauth_nonce"));
        assertNotNull(ltiSignedLaunch.getFormParameters().get("oauth_signature"));
    }

}
