package com.smartsparrow.iam.wiring;

import java.util.Objects;

public class SystemCredentialsConfig {

    private String userId;
    private String password;
    private String username;
    private String environment;

    public String getUserId() {
        return userId;
    }

    public SystemCredentialsConfig setUserId(String userId) {
        this.userId = userId;
        return this;
    }

    public String getPassword() {
        return password;
    }

    public SystemCredentialsConfig setPassword(String password) {
        this.password = password;
        return this;
    }

    public String getUsername() {
        return username;
    }

    public SystemCredentialsConfig setUsername(String username) {
        this.username = username;
        return this;
    }

    public String getEnvironment() {
        return environment;
    }

    public SystemCredentialsConfig setEnvironment(String environment) {
        this.environment = environment;
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SystemCredentialsConfig that = (SystemCredentialsConfig) o;
        return Objects.equals(userId, that.userId) &&
                Objects.equals(password, that.password) &&
                Objects.equals(username, that.username) &&
                Objects.equals(environment, that.environment);
    }

    @Override
    public int hashCode() {
        return Objects.hash(userId, password, username, environment);
    }

    @Override
    public String toString() {
        return "SystemCredentialsConfig{" +
                "userId='" + userId + '\'' +
                ", password='" + "*****" + '\'' + // Redacted
                ", username='" + username + '\'' +
                ", environment='" + environment + '\'' +
                '}';
    }
}
