package com.smartsparrow.config;

import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonProperty;

public class RedisInfraResponse {

    @JsonProperty("address")
    private String address;
    @JsonProperty("password")
    private String password;

    public RedisInfraResponse() {  }

    public String getAddress() {
        return address;
    }

    public RedisInfraResponse setAddress(String address) {
        this.address = address;
        return this;
    }

    public String getPassword() {
        return password;
    }

    public RedisInfraResponse setPassword(String password) {
        this.password = password;
        return this;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        RedisInfraResponse that = (RedisInfraResponse) o;
        return Objects.equals(address, that.address) && Objects.equals(password, that.password);
    }

    @Override
    public int hashCode() {
        return Objects.hash(address, password);
    }

    @Override
    public String toString() {
        return "RedisInfraResponse{" +
                "address='" + address + '\'' +
                ", password='" + password + '\'' +
                '}';
    }
}
