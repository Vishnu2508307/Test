package com.smartsparrow.rtm.message.handler.plugin;

import static com.smartsparrow.plugin.data.PluginLogBucketProvider.GENERIC_LOG_STATEMENT_BY_PLUGIN;
import static com.smartsparrow.rtm.RTMWebSocketTestUtils.mockSession;
import static com.smartsparrow.rtm.message.handler.plugin.PluginLogConfigUpdateMessageHandler.MAX_BUCKET_PER_TABLE;
import static com.smartsparrow.rtm.message.handler.plugin.PluginLogConfigUpdateMessageHandler.MAX_RECORD_PER_BUCKET;
import static com.smartsparrow.rtm.message.handler.plugin.PluginLogConfigUpdateMessageHandler.MIN_BUCKET_PER_TABLE;
import static com.smartsparrow.rtm.message.handler.plugin.PluginLogConfigUpdateMessageHandler.MIN_RECORD_PER_BUCKET;
import static com.smartsparrow.rtm.message.handler.plugin.PluginLogConfigUpdateMessageHandler.PLUGIN_LOG_CONFIG_UPDATE_ERROR;
import static com.smartsparrow.rtm.message.handler.plugin.PluginLogConfigUpdateMessageHandler.PLUGIN_LOG_CONFIG_UPDATE_OK;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.jetty.websocket.api.Session;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.smartsparrow.exception.IllegalArgumentFault;
import com.smartsparrow.plugin.data.BucketRetentionPolicy;
import com.smartsparrow.plugin.lang.PluginLogException;
import com.smartsparrow.plugin.service.PluginLogService;
import com.smartsparrow.plugin.wiring.BucketConfig;
import com.smartsparrow.plugin.wiring.PluginLogConfig;
import com.smartsparrow.rtm.MessageHandlerTestUtils;
import com.smartsparrow.rtm.message.recv.plugin.PluginLogConfigUpdateMessage;

import reactor.core.publisher.Mono;
import reactor.test.publisher.TestPublisher;

class PluginLogConfigUpdateMessageHandlerTest {

    private static final String tableName = GENERIC_LOG_STATEMENT_BY_PLUGIN;
    private static final Boolean enabled = true;
    private static final Long maxRecordCount = 10L;
    private static final BucketRetentionPolicy retentionPolicy = BucketRetentionPolicy.WEEK;
    private static final Long logBucketInstances = 5L;
    private final List<BucketConfig> bucketConfigList = new ArrayList<>();
    private final PluginLogConfig pluginLogConfig = new PluginLogConfig();

    @InjectMocks
    private PluginLogConfigUpdateMessageHandler handler;
    @Mock
    private PluginLogService pluginLogService;
    @Mock
    private PluginLogConfigUpdateMessage pluginLogConfigUpdateMessage;
    private Session session;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.initMocks(this);
        session = mockSession();

        when(pluginLogConfigUpdateMessage.getTableName()).thenReturn(tableName);
        when(pluginLogConfigUpdateMessage.getEnabled()).thenReturn(enabled);
        when(pluginLogConfigUpdateMessage.getMaxRecordCount()).thenReturn(maxRecordCount);
        when(pluginLogConfigUpdateMessage.getRetentionPolicy()).thenReturn(retentionPolicy);
        when(pluginLogConfigUpdateMessage.getLogBucketInstances()).thenReturn(logBucketInstances);

        BucketConfig bucketConfig = new BucketConfig().setTableName(tableName).setLogBucketInstances(
                logBucketInstances).setRetentionPolicy(retentionPolicy).setMaxRecordCount(maxRecordCount);
        bucketConfigList.add(bucketConfig);
        pluginLogConfig.setEnabled(enabled);
        pluginLogConfig.setBucketConfigs(bucketConfigList);
    }

    @Test
    void validate_noTableName() {
        when(pluginLogConfigUpdateMessage.getTableName()).thenReturn(null);

        IllegalArgumentFault ex = assertThrows(IllegalArgumentFault.class,
                                               () -> handler.validate(pluginLogConfigUpdateMessage));
        assertEquals("missing tableName", ex.getMessage());
    }

    @Test
    void validate_noEnabled() {
        when(pluginLogConfigUpdateMessage.getEnabled()).thenReturn(null);

        IllegalArgumentFault ex = assertThrows(IllegalArgumentFault.class,
                                               () -> handler.validate(pluginLogConfigUpdateMessage));
        assertEquals("missing enabled", ex.getMessage());
    }

    @Test
    void validate_lessThan_maxRecordCount() {
        when(pluginLogConfigUpdateMessage.getMaxRecordCount()).thenReturn(MIN_RECORD_PER_BUCKET - 1);

        IllegalArgumentFault ex = assertThrows(IllegalArgumentFault.class,
                                               () -> handler.validate(pluginLogConfigUpdateMessage));
        assertEquals("maxRecordCount is less than or equal " + MIN_RECORD_PER_BUCKET, ex.getMessage());
    }

    @Test
    void validate_moreThan_maxRecordCount() {
        when(pluginLogConfigUpdateMessage.getMaxRecordCount()).thenReturn(MAX_RECORD_PER_BUCKET * 2);

        IllegalArgumentFault ex = assertThrows(IllegalArgumentFault.class,
                                               () -> handler.validate(pluginLogConfigUpdateMessage));
        assertEquals("maxRecordCount is more than or equal " + MAX_RECORD_PER_BUCKET, ex.getMessage());
    }

    @Test
    void validate_lessThan_logBucketInstances() {
        when(pluginLogConfigUpdateMessage.getLogBucketInstances()).thenReturn(MIN_BUCKET_PER_TABLE - 1);

        IllegalArgumentFault ex = assertThrows(IllegalArgumentFault.class,
                                               () -> handler.validate(pluginLogConfigUpdateMessage));
        assertEquals("logBucketInstances is less than or equal " + MIN_BUCKET_PER_TABLE, ex.getMessage());
    }

    @Test
    void validate_moreThan_logBucketInstances() {
        when(pluginLogConfigUpdateMessage.getLogBucketInstances()).thenReturn(MAX_BUCKET_PER_TABLE * 2);

        IllegalArgumentFault ex = assertThrows(IllegalArgumentFault.class,
                                               () -> handler.validate(pluginLogConfigUpdateMessage));
        assertEquals("logBucketInstances is more than or equal " + MAX_BUCKET_PER_TABLE, ex.getMessage());
    }

    @Test
    void handle() throws IOException, PluginLogException {
        when(pluginLogService.updatePluginLogConfig(tableName,
                                                    enabled,
                                                    maxRecordCount,
                                                    retentionPolicy,
                                                    logBucketInstances))
                .thenReturn(Mono.just(pluginLogConfig));

        handler.handle(session, pluginLogConfigUpdateMessage);

        MessageHandlerTestUtils.verifySentMessage(session, response -> {
            assertAll(() -> {
                assertEquals(PLUGIN_LOG_CONFIG_UPDATE_OK, response.getType());
            });
        });
    }

    @Test
    void handle_exception() throws IOException, PluginLogException {
        TestPublisher<PluginLogConfig> error = TestPublisher.create();
        error.error(new RuntimeException("Unable to update PluginLogConfig"));
        when(pluginLogService.updatePluginLogConfig(tableName,
                                                    enabled,
                                                    maxRecordCount,
                                                    retentionPolicy,
                                                    logBucketInstances)).thenReturn(error.mono());

        handler.handle(session, pluginLogConfigUpdateMessage);

        MessageHandlerTestUtils.verifySentMessage(session,
                                                  "{\"type\":\"" + PLUGIN_LOG_CONFIG_UPDATE_ERROR + "\",\"code\":422," +
                                                          "\"message\":\"error updating the PluginLogConfig\"}");
    }
}