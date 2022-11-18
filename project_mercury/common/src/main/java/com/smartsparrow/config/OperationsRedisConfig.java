package com.smartsparrow.config;

public class OperationsRedisConfig {

    private String address;
    private String password;
    private Integer connectionPoolSize;
    private Integer connectionMinimumIdleSize;

    public OperationsRedisConfig() {  }

    public String getAddress() {
        return address;
    }

    public OperationsRedisConfig setAddress(String address) {
        this.address = address;
        return this;
    }

    public String getPassword() {
        return password;
    }

    public OperationsRedisConfig setPassword(String password) {
        this.password = password;
        return this;
    }

    public Integer getConnectionPoolSize() {
        return connectionPoolSize;
    }

    public OperationsRedisConfig setConnectionPoolSize(final Integer connectionPoolSize) {
        this.connectionPoolSize = connectionPoolSize;
        return this;
    }

    public Integer getConnectionMinimumIdleSize() {
        return connectionMinimumIdleSize;
    }

    public OperationsRedisConfig setConnectionMinimumIdleSize(final Integer connectionMinimumIdleSize) {
        this.connectionMinimumIdleSize = connectionMinimumIdleSize;
        return this;
    }

    @Override
    public String toString() {
        return "RedisConfig{" +
                "address='" + address + '\'' +
                ", password='***'" +
                ", connectionPoolSize='" + connectionPoolSize +
                ", connectionMinimumIdleSize='" + connectionMinimumIdleSize +
                '}';
    }
}
