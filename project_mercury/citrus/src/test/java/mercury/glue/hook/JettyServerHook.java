package mercury.glue.hook;

import org.springframework.beans.factory.annotation.Autowired;

import com.consol.citrus.Citrus;
import com.consol.citrus.annotations.CitrusFramework;
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
import com.smartsparrow.wiring.AmazonModule;
import com.smartsparrow.wiring.CommonModule;

import cucumber.api.java.Before;

public class JettyServerHook {

    private static final InstanceType instanceType = InstanceType.DEFAULT;
    private static JettyServer jettyServer = null;

    @CitrusFramework
    private Citrus citrus;

    @Autowired
    private JettyServerBuilder jettyServerBuilder;

    @Before(order = HooksOrder.JETTY_SERVER_BEFORE)
    public void beforeAll() throws JettyServerBuilderException {
        boolean skip = ("true".equals(System.getProperty("skipJettyStart")));
        if(jettyServer == null && !skip) {
            JettyServer server = jettyServerBuilder
                    .withModules(
                            new AmazonModule(),
                            new GraphQLServletModule(),
                            new IamModule(),
                            new RestServletModule(),
                            new PubSubModule(),
                            new ExportModule(),
                            new DefaultModule(),
                            new CommonModule()
                    )
                    .build();

            // start the jetty server in a new thread
            server.startNow();
            jettyServer = server;
        }
    }
}
