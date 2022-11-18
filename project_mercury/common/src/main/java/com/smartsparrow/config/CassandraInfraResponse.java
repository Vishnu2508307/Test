package com.smartsparrow.config;

import java.util.List;
import java.util.Objects;

/**
 * This class is used to keep configuration needed to start Cassandra Cluster
 */
public class CassandraInfraResponse {

    private List<String> contactPoints;
    private String username;
    private String password;
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

    public String getCertificate() {
        return certificate;
    }

    public Integer getLocalMaxRequestsPerConnection() {
        return localMaxRequestsPerConnection;
    }

    public Integer getMaxQueueSize() {
        return maxQueueSize;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CassandraInfraResponse that = (CassandraInfraResponse) o;
        return Objects.equals(contactPoints, that.contactPoints) &&
                Objects.equals(username, that.username) &&
                Objects.equals(password, that.password) &&
                Objects.equals(certificate, that.certificate) &&
                Objects.equals(localMaxRequestsPerConnection, that.localMaxRequestsPerConnection) &&
                Objects.equals(maxQueueSize, that.maxQueueSize);
    }

    @Override
    public int hashCode() {
        return Objects.hash(contactPoints,
                            username,
                            password,
                            certificate,
                            localMaxRequestsPerConnection,
                            maxQueueSize);
    }

    @Override
    public String toString() {
        return "CassandraInfraResponse{" +
                "contactPoints=" + contactPoints +
                ", username='" + username + '\'' +
                ", password='" + password + '\'' +
                ", certificate='" + certificate + '\'' +
                ", localMaxRequestsPerConnection=" + localMaxRequestsPerConnection +
                ", maxQueueSize=" + maxQueueSize +
                '}';
    }
}
