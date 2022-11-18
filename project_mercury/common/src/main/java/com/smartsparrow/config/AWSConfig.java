package com.smartsparrow.config;

import java.util.Objects;

public class AWSConfig {

    private String env;
    private String region;

    public AWSConfig() {  }

    public String getEnv() {
        return env;
    }

    public AWSConfig setEnv(final String env) {
        this.env = env;
        return this;
    }

    public String getRegion() {
        return region;
    }

    public AWSConfig setRegion(final String region) {
        this.region = region;
        return this;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AWSConfig awsConfig = (AWSConfig) o;
        return Objects.equals(env, awsConfig.env) &&
                Objects.equals(region, awsConfig.region);
    }

    @Override
    public int hashCode() {
        return Objects.hash(env, region);
    }

    @Override
    public String toString() {
        return "AWSConfig{" +
                "env='" + env + '\'' +
                ", region='" + region + '\'' +
                '}';
    }
}
