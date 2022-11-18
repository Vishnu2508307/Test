package com.smartsparrow.cache.wiring;

import javax.inject.Singleton;

import org.redisson.Redisson;
import org.redisson.api.RedissonReactiveClient;
import org.redisson.config.Config;
import org.redisson.config.SingleServerConfig;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.smartsparrow.config.BootstrapConfiguration;
import com.smartsparrow.config.RedisConfig;

public class RedissonModule extends AbstractModule {

    @Override
    protected void configure() {
        // Provider takes care of it
    }

    /**
     * Provides the basic Redisson reactive client
     * @return <code>RedissonReactiveClient</code> that interfaces with the redis server
     */
    @Provides
    @Singleton
    public RedissonReactiveClient getStringRedisReactiveCommands(BootstrapConfiguration bootstrapConfiguration) {
        //
        RedisConfig redisConfig = bootstrapConfiguration.getRedisConfig();
        Config config = getConfig(redisConfig.getAddress(), redisConfig.getPassword());
        return Redisson.createReactive(config);
    }

    private Config getConfig(String address, String password) {
        Config config = new Config();
        SingleServerConfig singleServerConfig = config.useSingleServer();
        singleServerConfig //
                .setAddress(address) //
                .setPassword(password)
                .setConnectionPoolSize(64)
                .setConnectionMinimumIdleSize(10)
                .setSubscriptionConnectionPoolSize(150)
                .setSubscriptionsPerConnection(100);

        return config;
    }
}
