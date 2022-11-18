package mercury.glue.step;

import static com.smartsparrow.rtm.message.handler.plugin.LearnspacePluginLogMessageHandler.LEARNSPACE_PLUGIN_LOG;
import static com.smartsparrow.rtm.message.handler.plugin.PluginLogConfigUpdateMessageHandler.MAX_BUCKET_PER_TABLE;
import static com.smartsparrow.rtm.message.handler.plugin.PluginLogConfigUpdateMessageHandler.MAX_RECORD_PER_BUCKET;
import static com.smartsparrow.rtm.message.handler.plugin.PluginLogConfigUpdateMessageHandler.PLUGIN_LOG_CONFIG_UPDATE;
import static com.smartsparrow.rtm.message.handler.plugin.WorkspacePluginLogMessageHandler.WORKSPACE_PLUGIN_LOG;
import static mercury.common.Variables.interpolate;
import static mercury.common.Variables.nameFrom;
import static mercury.glue.wiring.CitrusConfiguration.MESSAGE_OPERATIONS;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

import com.consol.citrus.annotations.CitrusResource;
import com.consol.citrus.dsl.runner.TestRunner;
import com.smartsparrow.courseware.data.CoursewareElementType;
import com.smartsparrow.plugin.data.BucketRetentionPolicy;
import com.smartsparrow.plugin.data.PluginLogLevel;
import com.smartsparrow.util.UUIDs;

import cucumber.api.java.Before;
import cucumber.api.java.en.When;
import mercury.common.MessageOperations;
import mercury.common.PayloadBuilder;

public class PluginLogSteps {

    @CitrusResource
    private TestRunner runner;

    public static final String DEFAULT_PLUGIN_MESSAGE = "This is a log message";
    public static final String DEFAULT_PLUGIN_ARGS = "This is a log message args";
    public static final String DEFAULT_PLUGIN_CONTEXT = "PREVIEW";
    public static final String DEFAULT_PLUGIN_VERSION = "1.0.0";
    public static final String TABLE_NAME = "generic_log_statement_by_plugin";

    @Autowired
    @Qualifier(MESSAGE_OPERATIONS)
    private MessageOperations messageOperations;

    @Autowired
    private AuthenticationSteps authenticationSteps;

    /**
     * this is hack to initialize @CitrusResource Runner, so this step class can be injected with @Autowired annotation
     * to other steps classes
     */
    @Before
    public void initializeCitrusResources() {
    }

    @When("{string} logs the learner plugin {string}")
    public void logsTheLearnerPlugin(String user, String pluginName) {
        String pluginIdVar = nameFrom("plugin_id", pluginName);
        String id = UUIDs.timeBased().toString();
        authenticationSteps.authenticateUser(user);
        messageOperations.sendJSON(runner, new PayloadBuilder()
                .addField("type", LEARNSPACE_PLUGIN_LOG)
                .addField("pluginId", interpolate(pluginIdVar))
                .addField("version", DEFAULT_PLUGIN_VERSION)
                .addField("level", PluginLogLevel.INFO)
                .addField("message", DEFAULT_PLUGIN_MESSAGE)
                .addField("args", DEFAULT_PLUGIN_ARGS)
                .addField("pluginContext", DEFAULT_PLUGIN_CONTEXT)
                .addField("elementId", id)
                .addField("elementType", CoursewareElementType.COMPONENT)
                .addField("deploymentId", id)
                .addField("cohortId", id)
                .build());
    }

    @When("{string} logs the learner plugin {string} with missing data")
    public void logsTheLearnerPluginWithMissingData(String user, String pluginName) {
        String pluginIdVar = nameFrom("plugin_id", pluginName);
        authenticationSteps.authenticateUser(user);
        messageOperations.sendJSON(runner, new PayloadBuilder()
                .addField("type", LEARNSPACE_PLUGIN_LOG)
                .addField("pluginId", interpolate(pluginIdVar))
                .build());
    }

    @When("{string} logs the workspace plugin {string}")
    public void logsTheWorkspacePlugin(String user, String pluginName) {
        String pluginIdVar = nameFrom("plugin_id", pluginName);
        String id = UUIDs.timeBased().toString();
        authenticationSteps.authenticateUser(user);
        messageOperations.sendJSON(runner, new PayloadBuilder()
                .addField("type", WORKSPACE_PLUGIN_LOG)
                .addField("pluginId", interpolate(pluginIdVar))
                .addField("version", DEFAULT_PLUGIN_VERSION)
                .addField("level", PluginLogLevel.INFO)
                .addField("message", DEFAULT_PLUGIN_MESSAGE)
                .addField("args", DEFAULT_PLUGIN_ARGS)
                .addField("pluginContext", DEFAULT_PLUGIN_CONTEXT)
                .addField("elementId", id)
                .addField("elementType", CoursewareElementType.COMPONENT)
                .addField("projectId", id)
                .build());
    }

    @When("{string} logs the workspace plugin {string} with missing data")
    public void logsTheWorkspacePluginWithMissingData(String user, String pluginName) {
        String pluginIdVar = nameFrom("plugin_id", pluginName);
        authenticationSteps.authenticateUser(user);
        messageOperations.sendJSON(runner, new PayloadBuilder()
                .addField("type", WORKSPACE_PLUGIN_LOG)
                .addField("pluginId", interpolate(pluginIdVar))
                .build());
    }

    @When("{string} updates plugin log config")
    public void updatePluginConfig(String user) {
        authenticationSteps.authenticateUser(user);
        messageOperations.sendJSON(runner, new PayloadBuilder()
                .addField("type", PLUGIN_LOG_CONFIG_UPDATE)
                .addField("enabled", true)
                .addField("tableName", TABLE_NAME)
                .addField("maxRecordCount", MAX_RECORD_PER_BUCKET / 2)
                .addField("retentionPolicy", BucketRetentionPolicy.WEEK)
                .addField("logBucketInstances", MAX_BUCKET_PER_TABLE / 2)
                .build());
    }

    @When("{string} updates plugin log config with missing data")
    public void updatePluginConfigWithMissingData(String user) {
        authenticationSteps.authenticateUser(user);
        messageOperations.sendJSON(runner, new PayloadBuilder()
                .addField("type", PLUGIN_LOG_CONFIG_UPDATE)
                .addField("enabled", true)
                .build());
    }

    @When("{string} updates plugin log config with wrong data")
    public void updatePluginConfigWithWrong(String user) {
        authenticationSteps.authenticateUser(user);
        messageOperations.sendJSON(runner, new PayloadBuilder()
                .addField("type", PLUGIN_LOG_CONFIG_UPDATE)
                .addField("enabled", true)
                .addField("tableName", TABLE_NAME)
                .addField("maxRecordCount", MAX_RECORD_PER_BUCKET * 2)
                .addField("retentionPolicy", BucketRetentionPolicy.WEEK)
                .addField("logBucketInstances", MAX_BUCKET_PER_TABLE * 2)
                .build());
    }
}
