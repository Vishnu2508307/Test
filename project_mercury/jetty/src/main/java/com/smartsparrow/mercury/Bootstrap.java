package com.smartsparrow.mercury;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Lists;
import com.google.inject.Module;
import com.smartsparrow.data.InstanceType;
import com.smartsparrow.export.wiring.ExportModule;
import com.smartsparrow.graphql.wiring.GraphQLServletModule;
import com.smartsparrow.iam.wiring.IamModule;
import com.smartsparrow.mercury.server.JettyServer;
import com.smartsparrow.mercury.server.JettyServerBuilder;
import com.smartsparrow.mercury.server.JettyServerBuilderException;
import com.smartsparrow.mercury.wiring.DefaultModule;
import com.smartsparrow.pubsub.wiring.PubSubModule;
import com.smartsparrow.rest.wiring.RestServletModule;
import com.smartsparrow.util.Enums;
import com.smartsparrow.wiring.AmazonModule;
import com.smartsparrow.wiring.CommonModule;
import com.smartsparrow.wiring.LearnspaceModule;
import com.smartsparrow.wiring.OperationsModule;
import com.smartsparrow.wiring.WorkspaceModule;

import reactor.core.scheduler.Schedulers;

/**
 * The port can be passed through system properties: '-Dmercury.port=8082', otherwise default port (8080) will be used
 */
public class Bootstrap {

    private static final Logger log = LoggerFactory.getLogger(Bootstrap.class);

    public static void main(String... args) throws JettyServerBuilderException {

        // Known dev/test environments
//        List<String> devEnvironments = Lists.newArrayList(
//                EnvRegion.LOCAL.name(),
//                EnvRegion.DEV.name(),
//                EnvRegion.SANDBOX.name());

        // creates a jetty server and adds a ServletContextHandler for the given url
        JettyServerBuilder builder = new JettyServerBuilder(System.getProperty("instance.type",
                                                                               Enums.asString(InstanceType.DEFAULT)));
        // prepare required modules
        List<Module> modules = Lists.newArrayList(
                new AmazonModule(),
                new GraphQLServletModule(),
                new IamModule(),
                new RestServletModule(),
                new PubSubModule(),
                new CommonModule(),
                // TODO: Remove RedisModule from ExportModule and put here.
                //  ExportModule should only need to be in Operations cluster
                new ExportModule()
        );

        // add relevant module based on the instance type
        log.info("Initializing as instance type: " + builder.getInstanceType());
        switch (builder.getInstanceType()) {
            case WORKSPACE:
                // only wires workspace related apis
                modules.add(new WorkspaceModule());
                break;
            case LEARNER:
                // only wires learnspace related apis
                modules.add(new LearnspaceModule());
                break;
            case OPERATIONS:
                // only wires operations related apis
                modules.add(new OperationsModule());
                break;
            default:
                // ALL LEGACY SERVERS execute this use case
                // wire all available apis
                modules.add(new DefaultModule());
        }

        builder.withModules(modules.toArray(new Module[0]));

        if (System.getProperty("mercury.port") != null) {
            builder.setPort(Integer.parseInt(System.getProperty("mercury.port")));
        }

        // FIXME: allowing wide open CORS in all environments for now until Prod stabilizes
        builder.withPermissiveCors(true);
        // FIXME: Replace the above line with the commented block below once prod cors is fixed to not need * origin
        // Allow wide open CORS if is is any of LOCAL, DEV, SANDBOX environments
//        builder.withPermissiveCors(devEnvironments
//                .stream()
//                .anyMatch(System.getProperty("env.region", EnvRegion.LOCAL.name())::contains));

        // to log any unhandled exception thrown by JVM, just log them
        // https://projectreactor.io/docs/core/release/api/reactor/core/Exceptions.html#throwIfJvmFatal-java.lang.Throwable-
        Schedulers.onHandleError((thread, error) -> {
            log.error(
                    "Scheduler Unhandled exception; threadName: {},message: {},threadStackTrace: {}, errorStackTrace: {}",
                    thread.getName(),
                    error.getMessage(),
                    thread.getStackTrace(),
                    error.getStackTrace()
            );
        });

        final JettyServer jettyServer = builder.build();
        // starts the JettyServer
        jettyServer.startNow();

        // On forced shutdown perform a graceful shutdown
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            log.info("Force shutdown invoked");

            if (!jettyServer.isStopped()) {
                try {
                    jettyServer.stop();
                } catch (Exception e) {
                    log.error("An error occurred while stopping the server", e);
                }
            }
        }));
    }
}
