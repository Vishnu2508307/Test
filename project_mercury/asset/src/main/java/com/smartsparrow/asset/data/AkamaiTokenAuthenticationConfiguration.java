package com.smartsparrow.asset.data;

import java.util.Objects;

public class AkamaiTokenAuthenticationConfiguration implements AssetSignatureConfiguration {

    /**
     * parameter name for the new token
     */
    private String tokenName;

    /**
     * secret required to generate the token. It must be hexadecimal digit string with even-length
     */
    private String key;

    /**
     * to use to generate the token. (sha1, sha256, or md5)
     */
    private String algorithm;

    /**
     * IP Address to restrict this token to. Troublesome in many cases (roaming, NAT, etc) so not often used.
     */
    private String ip;

    /**
     * what is the start time? ({@code NOW} for the current time)
     */
    private Long startTime;

    /**
     * when does this token expire? It overrides {@code windowSeconds}
     */
    private Long endTime;

    /**
     * How long is this token valid for?
     */
    private Long windowSeconds;

    public String getTokenName() {
        return tokenName;
    }

    public AkamaiTokenAuthenticationConfiguration setTokenName(String tokenName) {
        this.tokenName = tokenName;
        return this;
    }

    public String getKey() {
        return key;
    }

    public AkamaiTokenAuthenticationConfiguration setKey(String key) {
        this.key = key;
        return this;
    }

    public String getAlgorithm() {
        return algorithm;
    }

    public AkamaiTokenAuthenticationConfiguration setAlgorithm(String algorithm) {
        this.algorithm = algorithm;
        return this;
    }

    public String getIp() {
        return ip;
    }

    public AkamaiTokenAuthenticationConfiguration setIp(String ip) {
        this.ip = ip;
        return this;
    }

    public Long getStartTime() {
        return startTime;
    }

    public AkamaiTokenAuthenticationConfiguration setStartTime(Long startTime) {
        this.startTime = startTime;
        return this;
    }

    public Long getEndTime() {
        return endTime;
    }

    public AkamaiTokenAuthenticationConfiguration setEndTime(Long endTime) {
        this.endTime = endTime;
        return this;
    }

    public Long getWindowSeconds() {
        return windowSeconds;
    }

    public AkamaiTokenAuthenticationConfiguration setWindowSeconds(Long windowSeconds) {
        this.windowSeconds = windowSeconds;
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AkamaiTokenAuthenticationConfiguration that = (AkamaiTokenAuthenticationConfiguration) o;
        return Objects.equals(tokenName, that.tokenName) &&
                Objects.equals(key, that.key) &&
                Objects.equals(algorithm, that.algorithm) &&
                Objects.equals(ip, that.ip) &&
                Objects.equals(startTime, that.startTime) &&
                Objects.equals(endTime, that.endTime) &&
                Objects.equals(windowSeconds, that.windowSeconds);
    }

    @Override
    public int hashCode() {
        return Objects.hash(tokenName, key, algorithm, ip, startTime, endTime, windowSeconds);
    }

    @Override
    public String toString() {
        return "AkamaiTokenAuthenticationConfiguration{" +
                "tokenName='" + tokenName + '\'' +
                ", key='" + key + '\'' +
                ", algorithm='" + algorithm + '\'' +
                ", ip='" + ip + '\'' +
                ", startTime=" + startTime +
                ", endTime=" + endTime +
                ", windowSeconds=" + windowSeconds +
                '}';
    }

    @Override
    public AssetSignatureStrategyType getAssetSignatureStrategyType() {
        return AssetSignatureStrategyType.AKAMAI_TOKEN_AUTHENTICATION;
    }
}
