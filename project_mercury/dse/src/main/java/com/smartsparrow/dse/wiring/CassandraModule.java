package com.smartsparrow.dse.wiring;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.datastax.driver.core.Cluster;
import com.datastax.driver.core.Host;
import com.datastax.driver.core.HostDistance;
import com.datastax.driver.core.PoolingOptions;
import com.datastax.driver.core.Session;
import com.datastax.driver.core.policies.LoadBalancingPolicy;
import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.smartsparrow.config.BootstrapConfiguration;
import com.smartsparrow.dse.api.CassandraCluster;
import com.smartsparrow.dse.api.DSEException;
import com.smartsparrow.dse.api.PreparedStatementCache;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

/**
 * Provide underlying access to the Cassandra cluster(s) by providing com.datastax.driver.core.Session
 *
 * Required configuration parameters:
 * <ul>
 *  <li>cassandra.contactPoints - a comma separated list of cassandra nodes; <br>
 *                               <strong>NOTE: it should be nodes in the same physical datacenter/AWS Region!</strong></li>
 *  <li>cassandra.authentication.username - the username to connect with.</li>
 *  <li>cassandra.authentication.password - the password to connect with.</li>
 * </ul>
 *
 */
public class CassandraModule extends AbstractModule {

    private final static Logger log = LoggerFactory.getLogger(CassandraModule.class);

    @Override
    protected void configure() {
        // nothing to do here, module acts solely as a provider of Sessions.
    }

    /**
     * Provide Cassandra Session objects.
     *
     * This is marked as a singleton because of the 4 rules:
     * http://www.datastax.com/dev/blog/4-simple-rules-when-using-the-datastax-drivers-for-cassandra
     *
     * Additional side effect of the singleton is that it makes this code really straightforward.
     */
    @SuppressFBWarnings(value = "DM_EXIT",
            justification = "Stop the application if the Cassandra cluster is not available")
    @Singleton
    @Provides
    Session provideSession(BootstrapConfiguration config) {
        CassandraCluster cluster = new CassandraCluster(config.getCassandraConfig());
        Session session = null;
        try {
            session = cluster.openSession();
            periodicPoolMetrics(cluster.getCluster(), session);
        } catch (DSEException e) {
            log.error("error connecting to cluster, exiting.", e);
            System.exit(-1);
        }
        return session;
    }

    void periodicPoolMetrics(Cluster cluster, Session session) {
        // print metrics
        final LoadBalancingPolicy loadBalancingPolicy =
                cluster.getConfiguration().getPolicies().getLoadBalancingPolicy();

        final PoolingOptions pooling =
                cluster.getConfiguration().getPoolingOptions();

        ScheduledExecutorService scheduled =
                Executors.newScheduledThreadPool(1);
        scheduled.scheduleAtFixedRate(new Runnable() {
            @Override
            public void run() {
                Session.State state = session.getState();
                for (Host host : state.getConnectedHosts()) {
                    HostDistance distance = loadBalancingPolicy.distance(host);
                    int connections = state.getOpenConnections(host);
                    int inFlightQueries = state.getInFlightQueries(host);
                    log.debug(String.format("%s connections=%d, current load=%d, maxload=%d maxQueue=%d",
                            host, connections, inFlightQueries,
                                    connections * pooling.getMaxRequestsPerConnection(distance),
                                    pooling.getMaxQueueSize()));
                }
            }
        }, 30, 5, TimeUnit.SECONDS);
    }

    @Provides
    @Singleton
    PreparedStatementCache provideStmtCache(Session session) {
        return new PreparedStatementCache(session);
    }

}
