package com.smartsparrow.config;

import java.util.List;

/**
 * This class is used to keep configuration needed to start Cassandra Cluster
 */
public class CassandraConfig {

    private List<String> contactPoints;
    private String username;
    private String password;
    //Either keystore/keystorePassword or certificate should be defined
    private String keystore;
    private String keystorePassword;
    private String certificate;
    private Integer localMaxRequestsPerConnection;
    private Integer maxQueueSize;

    public List<String> getContactPoints() {
        return contactPoints;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    public String getKeystore() {
        return keystore;
    }

    public String getKeystorePassword() {
        return keystorePassword;
    }

    public String getCertificate() {
        return certificate;
    }

    public Integer getLocalMaxRequestsPerConnection() {
        return localMaxRequestsPerConnection;
    }

    public Integer getMaxQueueSize() {
        return maxQueueSize;
    }

    public CassandraConfig setContactPoints(List<String> contactPoints) {
        this.contactPoints = contactPoints;
        return this;
    }

    public CassandraConfig setUsername(String userName) {
        this.username = userName;
        return this;
    }

    public CassandraConfig setPassword(String password) {
        this.password = password;
        return this;
    }

    public CassandraConfig setKeystore(String keyStoreUrl) {
        this.keystore = keyStoreUrl;
        return this;
    }

    public CassandraConfig setKeystorePassword(String keyStorePassword) {
        this.keystorePassword = keyStorePassword;
        return this;
    }

    public CassandraConfig setCertificate(String certificate) {
        this.certificate = certificate;
        return this;
    }

    public CassandraConfig setLocalMaxRequestsPerConnection(Integer localMaxRequestsPerConnection) {
        this.localMaxRequestsPerConnection = localMaxRequestsPerConnection;
        return this;
    }

    public CassandraConfig setMaxQueueSize(Integer maxQueueSize) {
        this.maxQueueSize = maxQueueSize;
        return this;
    }

    @Override
    public String toString() {
        return "CassandraConfig{" +
                "contactPoints=" + contactPoints +
                ", username='" + username + '\'' +
                ", password=***" +
                ", keystore='" + keystore + '\'' +
                ", keystorePassword=***" +
                ", certificate=***" +
                '}';
    }
}
