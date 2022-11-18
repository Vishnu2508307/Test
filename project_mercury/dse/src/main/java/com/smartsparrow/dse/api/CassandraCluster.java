package com.smartsparrow.dse.api;

import java.io.ByteArrayInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.util.List;

import javax.annotation.Nullable;
import javax.net.ssl.TrustManagerFactory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.datastax.driver.core.Cluster;
import com.datastax.driver.core.ConsistencyLevel;
import com.datastax.driver.core.HostDistance;
import com.datastax.driver.core.PoolingOptions;
import com.datastax.driver.core.ProtocolOptions;
import com.datastax.driver.core.ProtocolVersion;
import com.datastax.driver.core.QueryOptions;
import com.datastax.driver.core.RemoteEndpointAwareNettySSLOptions;
import com.datastax.driver.core.SSLOptions;
import com.datastax.driver.core.Session;
import com.datastax.driver.core.exceptions.AuthenticationException;
import com.datastax.driver.core.exceptions.NoHostAvailableException;
import com.datastax.driver.core.policies.DCAwareRoundRobinPolicy;
import com.datastax.driver.core.policies.Policies;
import com.datastax.driver.core.policies.TokenAwarePolicy;
import com.google.common.base.Preconditions;
import com.smartsparrow.config.CassandraConfig;

import io.netty.handler.ssl.SslContextBuilder;
import io.netty.handler.ssl.SslProvider;

/**
 * Contains common logic to build Cassandra cluster and open session.
 * Can be used in different places where we need to open new Cassandra connection.
 */
public class CassandraCluster {

    private final static Logger log = LoggerFactory.getLogger(CassandraCluster.class);

    private Cluster cluster;

    public CassandraCluster(CassandraConfig config) {
        this(config.getContactPoints(),
                config.getUsername(),
                config.getPassword(),
                config.getKeystore(),
                config.getKeystorePassword(),
                config.getCertificate(),
                config.getLocalMaxRequestsPerConnection(),
                config.getMaxQueueSize());
    }

    public CassandraCluster(List<String> contactPoints,
                            String username,
                            String password,
                            String keystorePath,
                            String keystorePassword,
                            String certificate,
                            @Nullable Integer localMaxRequestsPerConnection,
                            @Nullable Integer maxQueueSize) {

        Preconditions.checkNotNull(contactPoints);
        Preconditions.checkNotNull(username);
        Preconditions.checkNotNull(password);
        Preconditions.checkArgument((keystorePath == null && keystorePassword == null && certificate != null)
                        || (certificate == null && keystorePath != null && keystorePassword != null),
                "Either certificate or keystorePath/keystorePassword should be passed");

        if (log.isDebugEnabled()){
            log.debug("Creating C* cluster with settings: contactPoints='{}', username='{}', password='***', keystorePath='{}', " +
                            "keystorePassword='{}', certificate='{}'", contactPoints, username, keystorePath,
                    keystorePassword == null ? null : "***", certificate == null ? null : "***");
        }

        SSLOptions sslOptions = buildSSLOptions(keystorePath, keystorePassword, certificate);

        // Set optional pooling options. As of driver 3.6, they default to:
        // 1024 local max requests per connection
        // 256 max queue size
        PoolingOptions poolingOptions = new PoolingOptions();
        if (localMaxRequestsPerConnection != null) {
            poolingOptions = poolingOptions.setMaxRequestsPerConnection(HostDistance.LOCAL, localMaxRequestsPerConnection);
        }
        if (maxQueueSize != null) {
            poolingOptions = poolingOptions.setMaxQueueSize(maxQueueSize);
        }

        cluster = Cluster.builder()
                .addContactPoints(contactPoints.toArray(new String[contactPoints.size()]))
                .withCompression(ProtocolOptions.Compression.LZ4)
                .withPoolingOptions(poolingOptions)
                .withCredentials(username, password)
                // the default constructor os DCAwareRoundRobinPolicy assumes the contact points
                // are the "local" datacenter.
                .withLoadBalancingPolicy(new TokenAwarePolicy(DCAwareRoundRobinPolicy.builder().build()))
                .withReconnectionPolicy(Policies.defaultReconnectionPolicy())
                .withProtocolVersion(ProtocolVersion.V4)
                .withSSL(sslOptions)
                .withQueryOptions(new QueryOptions().setConsistencyLevel(ConsistencyLevel.LOCAL_QUORUM))
                .build();
    }

    public CassandraCluster(List<String> contactPoints, String username, String password, String certificate) {
        this(contactPoints, username, password, null, null, certificate,
                null, null);
    }

    public CassandraCluster(List<String> contactPoints, String username, String password, String keystorePath,
                            String keystorePassword) {
        this(contactPoints, username, password, keystorePath, keystorePassword,
                null, null, null);
    }

    public Session openSession() throws DSEException {
        if (log.isDebugEnabled()) {
            log.debug("Connecting to C* cluster and creating session.");
        }
        Session session;
        try {
            session = cluster.connect();
        } catch (NoHostAvailableException | AuthenticationException | IllegalStateException e) {
            throw new DSEException("error connecting to cluster", e);
        }
        return session;
    }

    public void close() {
        if (log.isDebugEnabled()) {
            log.debug("Closing C* cluster.");
        }
        cluster.close();
    }

    /*
     * Build the SSL transport layer between the client and the DSE server.
     *
     * See:
     *  - http://docs.datastax.com/en/developer/java-driver/3.3/manual/ssl/#netty
     *  - http://netty.io/wiki/forked-tomcat-native.html
     */
    private SSLOptions buildSSLOptions(final String keystorePath, final String keystorePassword, final String certificate) {
        try {
            KeyStore ks = KeyStore.getInstance("JKS");
            if (keystorePath != null && keystorePassword != null) {
                try (InputStream trustStore = new FileInputStream(keystorePath)) {
                    ks.load(trustStore, keystorePassword.toCharArray());
                }
            } else if (certificate != null) {
                ByteArrayInputStream byteStream = new ByteArrayInputStream(certificate.getBytes(Charset.forName("UTF-8")));
                Certificate cert = CertificateFactory.getInstance("X.509").generateCertificate(byteStream);
                ks.load(null, null);
                ks.setCertificateEntry("cassandra-cert", cert);

            }
            TrustManagerFactory tmf = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
            tmf.init(ks);
            SslContextBuilder builder = SslContextBuilder.forClient()
                    .sslProvider(SslProvider.OPENSSL)
                    .trustManager(tmf);

            return new RemoteEndpointAwareNettySSLOptions(builder.build());
        } catch (KeyStoreException | IOException | CertificateException | NoSuchAlgorithmException e) {
            throw new RuntimeException("Unable to build SSL options for Cassandra.", e);
        }
    }

    public Cluster getCluster() {
        return cluster;
    }
}
