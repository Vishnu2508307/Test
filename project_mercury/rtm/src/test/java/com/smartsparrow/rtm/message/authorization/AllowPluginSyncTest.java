package com.smartsparrow.rtm.message.authorization;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.smartsparrow.iam.service.AuthenticationContext;
import com.smartsparrow.plugin.wiring.PluginConfig;
import com.smartsparrow.rtm.message.recv.plugin.SyncPluginMessage;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

@SuppressFBWarnings(value = "RV_RETURN_VALUE_IGNORED_NO_SIDE_EFFECT", justification = "wont mangle a simple test " +
        "because SB is being pedantic, whole point is to verify that get config method is called")
class AllowPluginSyncTest {

    @Mock
    private PluginConfig pluginConfig;

    @Mock
    private SyncPluginMessage msg;

    @Mock
    private AuthenticationContext authenticationContext;

    @InjectMocks
    private AllowPluginSync allowPluginSync;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    void test_allowSync() {
        when(pluginConfig.getAllowSync()).thenReturn(true);
        Assertions.assertTrue(allowPluginSync.test(authenticationContext, msg));

        verify(pluginConfig).getAllowSync();
    }


}
