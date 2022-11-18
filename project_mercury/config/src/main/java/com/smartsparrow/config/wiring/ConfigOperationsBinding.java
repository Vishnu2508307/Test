package com.smartsparrow.config.wiring;

public class ConfigOperationsBinding {

    private final ConfigurationBindingOperations binder;

    public ConfigOperationsBinding(final ConfigurationBindingOperations binder) {
        this.binder = binder;
    }

    public void bind() {
        binder.bind("redis.infra")
                .toConfigType(RedisInfraConfiguration.class);

        binder.bind("cassandra.infra")
                .toConfigType(CassandraInfraConfiguration.class);

    }
}
