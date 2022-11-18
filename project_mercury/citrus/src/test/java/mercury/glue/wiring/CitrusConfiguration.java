package mercury.glue.wiring;

import static mercury.functions.RandomEmailFunction.RANDOM_EMAIL;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

import com.consol.citrus.dsl.endpoint.CitrusEndpoints;
import com.consol.citrus.functions.FunctionLibrary;
import com.consol.citrus.http.client.HttpClient;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.smartsparrow.mercury.server.JettyServerBuilder;

import mercury.common.MessageOperations;
import mercury.functions.EscapeJsonFunction;
import mercury.functions.RandomEmailFunction;
import mercury.functions.RandomTimeUUIDFunction;

@Configuration
@PropertySource("citrus.properties")
public class CitrusConfiguration {

    public static final String JETTY_SERVER_BUILDER = "jettyServerBuilder";
    public static final String WEB_SOCKET_REQUEST_URL = "webSocketRequestUrl";
    public static final String LEARN_REQUEST_URL = "learnRequestUrl";
    public static final String REST_CLIENT = "restClient";
    public static final String HTTP_CLIENT = "httpClient";
    public static final String IES_HTTP_CLIENT_DEV = "iesHttpClientDev";
    public static final String IES_HTTP_TEST_SIMULATOR = "iesHttpClientTestSimulator";
    public static final String IES_HTTP_DEV_SIMULATOR = "iesHttpClientDevSimulator";
    public static final String IES_HTTP_CLIENT_TEST = "iesHttpClientTest";
    public static final String MYCLOUD_HTTP_CLIENT_DEV = "myCloudHttpClientDev";
    public static final String MYCLOUD_HTTP_TEST_SIMULATOR = "myCloudHttpClientTestSimulator";
    public static final String MYCLOUD_HTTP_DEV_SIMULATOR = "myCloudHttpClientDevSimulator";
    public static final String MYCLOUD_HTTP_CLIENT_TEST = "myCloudHttpClientTest";
    public static final String PLUGIN_ZIP_ENDPOINT = "pluginZipEndpoint";
    public static final String PLUGIN_FILES_ENDPOINT = "pluginFilesEndpoint";
    public static final String PLUGIN_FILES_PUBLIC_URL = "pluginFilesPublicUrl";
    public static final String WEB_SOCKET_CLIENT_REGISTRY = "webSocketClientRegistry";
    public static final String MESSAGE_OPERATIONS = "messageOperations";
    public static final String MERCURY_PRODUCT_ID = "mercuryProductId";

    @Value("${mercury.host}")
    private String mercuryHost;

    @Value("${mercury.socket.endpoint}")
    private String socketEndpoint;

    @Value("${mercury.learn.endpoint}")
    private String learnEndpoint;

    @Value("${mercury.rest.endpoint}")
    private String restEndpoint;

    @Value("${mercury.port}")
    private int port;

    @Value("${plugin.repository.publicUrl}")
    private String repositoryPublicUrl;

    @Value("${plugin.distribution.publicUrl}")
    private String distributionPublicUrl;

    @Value("${mercury.productId}")
    private String productId;

    @Value("${ies.stg.env.endpoint}")
    private String iesStgEnvEndpoint;

    @Value("${ies.dev.env.endpoint}")
    private String iesDevEnvEndPoint;

    @Value("${mycloud.stg.env.endpoint}")
    private String myCloudStgEnvEndpoint;

    @Value("${mycloud.dev.env.endpoint}")
    private String myCloudDevEnvEndPoint;

    @Value("${com.smartsparrow.mercury.simulator.port}")
    private String simulatorPort;

    @Value("${com.smartsparrow.mercury.simulator.host}")
    private String simulatorHost;

    // TODO will be refactored in BRNT-363
    @Bean(MERCURY_PRODUCT_ID)
    public String getProductId() {
        return productId;
    }

    @Bean(REST_CLIENT)
    public HttpClient restClient() {
        return CitrusEndpoints.http()
                .client()
                .requestUrl(String.format("http://%s:%s/%s", mercuryHost, port, restEndpoint))
                .build();
    }

    @Bean(HTTP_CLIENT)
    public HttpClient httpClient() {
        return CitrusEndpoints.http()
                .client()
                .requestUrl(String.format("http://%s:%s", mercuryHost, port))
                .build();
    }

    // TODO will be refactored in BRNT-363
    @Bean(IES_HTTP_CLIENT_DEV)
    public HttpClient iesHttpClientDev() {
        return CitrusEndpoints.http()
                .client()
                .requestUrl("https://tst-piapi-internal.dev-openclass.com")
                .build();
    }

    // TODO will be refactored in BRNT-363
    @Bean(IES_HTTP_CLIENT_TEST)
    public HttpClient iesHttpClientTest() {
        return CitrusEndpoints.http()
                .client()
                .requestUrl("https://int-piapi-internal.stg-openclass.com")
                .build();
    }

    // TODO will be refactored in BRNT-363
    @Bean(MYCLOUD_HTTP_CLIENT_DEV)
    public HttpClient myCloudHttpClientDev() {
        return CitrusEndpoints.http()
                .client()
                .requestUrl("https://identity-internal-test.pearson.com")
                .build();
    }

    // TODO will be refactored in BRNT-363
    @Bean(MYCLOUD_HTTP_CLIENT_TEST)
    public HttpClient myCloudHttpClientTest() {
        return CitrusEndpoints.http()
                .client()
                .requestUrl("https://identity-internal-test.pearson.com")
                .build();
    }

    @Bean(JETTY_SERVER_BUILDER)
    public JettyServerBuilder jettyServerBuilder() {
        return new JettyServerBuilder()
                .setPort(port);
    }

    @Bean (WEB_SOCKET_REQUEST_URL)
    public String getWebSocketRequestUrl() {
        return String.format("ws://%s:%s/%s", mercuryHost, port, socketEndpoint);
    }

    @Bean (LEARN_REQUEST_URL)
    public String getLearnRequestUrl() {
        return String.format("ws://%s:%s/%s", mercuryHost, port, learnEndpoint);
    }

    @Bean(name="mercuryFunctionLibrary")
    public FunctionLibrary getFunctionLibrary() {
        FunctionLibrary mercuryFunctionLibrary = new FunctionLibrary();

        mercuryFunctionLibrary.setPrefix("mercury:");
        mercuryFunctionLibrary.setName("mercuryFunctionLibrary");

        mercuryFunctionLibrary.getMembers().put(RANDOM_EMAIL, new RandomEmailFunction());
        mercuryFunctionLibrary.getMembers().put("randomTimeUUID", new RandomTimeUUIDFunction());
        mercuryFunctionLibrary.getMembers().put("escapeJson", new EscapeJsonFunction());

        return mercuryFunctionLibrary;
    }

    @Bean(PLUGIN_ZIP_ENDPOINT)
    public HttpClient S3RepositoryClient() {
        return CitrusEndpoints.http()
                .client()
                .requestUrl(repositoryPublicUrl)
                .build();
    }

    @Bean(PLUGIN_FILES_ENDPOINT)
    public HttpClient S3DistributionClient() {
        return CitrusEndpoints.http()
                .client()
                .requestUrl(distributionPublicUrl)
                .build();
    }

    @Bean(PLUGIN_FILES_PUBLIC_URL)
    public String S3PublicUrl() {
        return distributionPublicUrl;
    }

    @Bean
    public ObjectMapper getObjectMapper() {
        return new ObjectMapper();
    }

    @Bean(WEB_SOCKET_CLIENT_REGISTRY)
    public WebSocketClientRegistry getWebSocketClientRegistry() {
        return new WebSocketClientRegistry();
    }

    @Bean(MESSAGE_OPERATIONS)
    public MessageOperations getMessageOperations() {
        return new MessageOperations();
    }

    @Bean(IES_HTTP_TEST_SIMULATOR)
    public HttpClient iesSimulatorStgClient() {
        return CitrusEndpoints.http().client()
                .requestUrl(String.format("http://%s:%s/%s", simulatorHost, simulatorPort, iesStgEnvEndpoint))
                .build();
    }

    @Bean(IES_HTTP_DEV_SIMULATOR)
    public HttpClient iesSimulatorDevClient() {
        return CitrusEndpoints.http().client()
                .requestUrl(String.format("http://%s:%s/%s", simulatorHost, simulatorPort, iesDevEnvEndPoint))
                .build();
    }

    @Bean(MYCLOUD_HTTP_TEST_SIMULATOR)
    public HttpClient myCloudSimulatorStgClient() {
        return CitrusEndpoints.http().client()
                .requestUrl(String.format("http://%s:%s/%s", simulatorHost, simulatorPort, myCloudStgEnvEndpoint))
                .build();
    }

    @Bean(MYCLOUD_HTTP_DEV_SIMULATOR)
    public HttpClient myCloudSimulatorDevClient() {
        return CitrusEndpoints.http().client()
                .requestUrl(String.format("http://%s:%s/%s", simulatorHost, simulatorPort, myCloudDevEnvEndPoint))
                .build();
    }
}
