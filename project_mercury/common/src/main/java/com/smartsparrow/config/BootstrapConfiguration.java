package com.smartsparrow.config;

public class BootstrapConfiguration {

    private CassandraConfig cassandraConfig;
    private RedisConfig redisConfig;

    public BootstrapConfiguration(CassandraConfig cassandraConfig, RedisConfig redisConfig) {
        this.cassandraConfig = cassandraConfig;
        this.redisConfig = redisConfig;
    }

    public CassandraConfig getCassandraConfig() {
        return cassandraConfig;
    }

    public RedisConfig getRedisConfig() {
        return redisConfig;
    }
}
