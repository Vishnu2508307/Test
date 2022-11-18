package com.smartsparrow.config;

public class RedisConfig {

    private String address;
    private String password;

    public RedisConfig() {  }

    public String getAddress() {
        return address;
    }

    public RedisConfig setAddress(String address) {
        this.address = address;
        return this;
    }

    public String getPassword() {
        return password;
    }

    public RedisConfig setPassword(String password) {
        this.password = password;
        return this;
    }

    @Override
    public String toString() {
        return "RedisConfig{" +
                "address='" + address + '\'' +
                ", password='***'" +
                '}';
    }
}
