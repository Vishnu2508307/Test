package com.smartsparrow.export.wiring;

import org.redisson.Redisson;
import org.redisson.api.RedissonReactiveClient;
import org.redisson.config.Config;
import org.redisson.config.SingleServerConfig;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.smartsparrow.config.OperationsRedisConfig;

/**
 * Provider for custom redisson client to when @Operations is used
 */
public class OperationsRedissonClientProvider implements Provider<RedissonReactiveClient> {

    @Inject
    OperationsRedisConfig operationsRedisConfig;

    @Override
    public RedissonReactiveClient get() {
        Config config = new Config();

        // single node for start, might need a sharded cluster if exports explode in usage
        SingleServerConfig singleServerConfig = config.useSingleServer();

        // default configs should be enough for this one
        singleServerConfig //
                .setAddress(operationsRedisConfig.getAddress()) //
                .setPassword(operationsRedisConfig.getPassword())
                .setConnectionPoolSize(operationsRedisConfig.getConnectionPoolSize())
                .setConnectionMinimumIdleSize(operationsRedisConfig.getConnectionMinimumIdleSize());
        return Redisson.createReactive(config);
    }
}
