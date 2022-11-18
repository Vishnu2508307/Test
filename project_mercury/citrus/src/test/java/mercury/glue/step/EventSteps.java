package mercury.glue.step;

import static mercury.common.Variables.interpolate;
import static mercury.common.Variables.nameFrom;
import static mercury.glue.step.PluginShareSteps.PLUGIN_ID_VAR;
import static mercury.glue.step.ProvisionSteps.getSubscriptionIdVar;
import static mercury.glue.step.courseware.ActivitySteps.ACTIVITY_ID_VAR;
import static mercury.glue.step.workspace.WorkspaceCreateSteps.WORKSPACE_ID_VAR;
import static mercury.helpers.workspace.WorkspaceHelper.getWorkspaceIdVar;

import java.util.HashMap;
import java.util.Map;

import org.hamcrest.Matchers;
import org.springframework.beans.factory.annotation.Autowired;

import com.consol.citrus.annotations.CitrusResource;
import com.consol.citrus.dsl.runner.TestRunner;
import com.google.common.base.Strings;

import cucumber.api.java.en.And;
import cucumber.api.java.en.Given;
import cucumber.api.java.en.Then;
import mercury.common.Event;
import mercury.common.MessageOperations;
import mercury.common.PayloadBuilder;
import mercury.glue.wiring.WebSocketClientRegistry;

public class EventSteps {

    @CitrusResource
    private TestRunner runner;

    @Autowired
    private MessageOperations messageOperations;

    public static final String ACCOUNT_PROVISION_BROADCAST_ID = "account_provision_broadcast_id";
    public static final String PLUGIN_PERMISSION_BROADCAST_ID = "plugin_permission_broadcast_id";
    public static final String COHORT_BROADCAST_ID = "cohort_broadcast_id";
    public static final String ACTIVITY_BROADCAST_ID = "activity_broadcast_id";

    private static final Map<String, Event> events =
            new HashMap<String, Event>() {{
                put("account provision", new Event("iam.account.provision.subscribe")
                        .setRtmSubscriptionIdVariable(ACCOUNT_PROVISION_BROADCAST_ID));

                put("plugin permission", new Event("workspace.plugin.permission.subscribe")
                        .addField("pluginId", interpolate(PLUGIN_ID_VAR))
                        .setRtmSubscriptionIdVariable(PLUGIN_PERMISSION_BROADCAST_ID));

                put("cohort", new Event("workspace.cohort.subscribe")
                        .addField("cohortId", "${cohort_id}")
                        .setRtmSubscriptionIdVariable(COHORT_BROADCAST_ID));

                put("activity", new Event("author.activity.subscribe")
                        .addField("activityId", interpolate(ACTIVITY_ID_VAR))
                        .setRtmSubscriptionIdVariable(ACTIVITY_BROADCAST_ID));

                put("cohortUnsubscribe", new Event("workspace.cohort.unsubscribe")
                        .addField("cohortId", "${cohort_id}")
                        .setRtmSubscriptionIdVariable(COHORT_BROADCAST_ID));
            }};


    @Given("^\"([^\"]*)\" subscribes to \"([^\"]*)\" events$")
    public void subscribesToEvents(String userName, String eventName) {
        subscribesToEvents(userName, eventName, WebSocketClientRegistry.DEFAULT_WEB_SOCKET_CLIENT);
    }

    @Given("^\"([^\"]*)\" subscribes to \"([^\"]*)\" events via a \"([^\"]*)\" client$")
    public void subscribesToEvents(String userName, String eventName, String clientName) {
        Event event = events.get(eventName);
        PayloadBuilder payloadBuilder = new PayloadBuilder();
        payloadBuilder.addField("type", event.getType());

        payloadBuilder.addAll(events.get(eventName).getFields());

        messageOperations.sendJSON(runner, payloadBuilder.build(), clientName);
        messageOperations.receiveJSON(runner, action-> action
                .jsonPath("$.type", event.getType() + ".ok")
                .extractFromPayload("$.response.rtmSubscriptionId", clientName + "_" + event.getRtmSubscriptionIdVariable()), clientName);
    }

    @Then("{string} should receive an account provisioned {string} notification via a {string} client")
    public void shouldBeNotifiedViaAClient(final String accountName, String accountAction, String clientName) {
        messageOperations.receiveJSON(runner, action -> action.payload("{\"type\":\"iam.account.provision.broadcast\","
                + "\"replyTo\":\"" + interpolate(clientName + "_" + ACCOUNT_PROVISION_BROADCAST_ID) + "\","
                + "\"response\":{\"account\":{\"accountId\":\"@notEmpty()@\","
                + "\"subscriptionId\":\""+ interpolate(getSubscriptionIdVar(accountName)) +"\","
                + "\"iamRegion\":\"GLOBAL\","
                + "\"primaryEmail\":\"@notEmpty()@\","
                + "\"roles\":\"@notEmpty()@\","
                + "\"email\":\"@notEmpty()@\","
                + "\"authenticationType\":\"@notEmpty()@\"}," + "\"rtmEvent\" : \"" + accountAction + "\"" + "}}"
                ), clientName);
    }

    @Then("^\"([^\"]*)\" should receive a plugin \"([^\"]*)\" notification via a \"([^\"]*)\" client$")
    public void shouldReceiveAPluginNotificationViaAClient(String accountName, String permissionAction, String clientName) {
        String field;
        String actionName;

        if (permissionAction.contains("TEAM")) {
            field = "team";
            actionName = permissionAction.substring(5); // it will 'granted' or 'revoked'
        } else {
            field = "account";
            actionName = permissionAction;
        }

        messageOperations.receiveJSON(runner, action -> action
                .jsonPath("$.type", "workspace.plugin.permission.broadcast")
                .jsonPath("$.replyTo", interpolate(clientName + "_" + PLUGIN_PERMISSION_BROADCAST_ID))
                .jsonPath("$.response.action", actionName.split("_")[2])
                .jsonPath("$.response.rtmEvent", actionName)
                .jsonPath("$.response.collaborator." + field, "@notEmpty()@"), clientName);
    }

    @Then("^\"([^\"]*)\" should not be notified$")
    public void shouldNotBeNotified(String client) {
        messageOperations.receiveTimeout(runner, client);
    }

    @And("^\"([^\"]*)\" subscribes to \"([^\"]*)\" events for \"([^\"]*)\" via a \"([^\"]*)\" client$")
    public void subscribesToEventsForViaAClient(String user, String eventName, String entityName, String clientName) {
        String type = events.get(eventName).getType();

        String entityIdVar = String.format("%sId", eventName);

        messageOperations.sendJSON(runner, new PayloadBuilder()
                .addField("type", type)
                .addField(entityIdVar, interpolate(nameFrom(entityName, "id")))
                .build(), clientName);
        messageOperations.receiveJSON(runner, action-> action.jsonPath("$.type",
                Matchers.containsString("subscribe.ok")), clientName);
    }

    @Then("^\"([^\"]*)\" should not receive a broadcast$")
    public void notReceiveABroadcast(String user) {
        messageOperations.receiveTimeout(runner);
    }

    @Given("{string} unsubscribe to {string} events via a {string} client")
    public void unsubscribeToEventsViaAClient(String userName, String eventName, String clientName) {
        messageOperations.sendJSON(runner, new PayloadBuilder()
                .addField("type", "workspace.cohort.unsubscribe")
                .addField("cohortId", "${cohort_id}")
                .build(), clientName);
        messageOperations.validateResponseType(runner, "workspace.cohort.unsubscribe.ok", clientName, action -> {
        });
    }

    @Given("{string} cannot subscribes to {string} events via a {string} client due to missing permission level")
    public void cannotSubscribesToEventsViaAClientDueToMissingPermissionLevel(String user, String eventName, String clientName) {
        messageOperations.sendJSON(runner, new PayloadBuilder()
                .addField("type", "workspace.cohort.subscribe")
                .addField("cohortId", "${cohort_id}")
                .build(), clientName);
        messageOperations.receiveJSON(runner,
                                      action -> action.jsonPath("$.type",
                                                                Matchers.containsString("workspace.cohort.subscribe.error")),
                                      clientName);
    }

    @Given("{string} cannot unsubscribe to {string} events via a {string} client due to missing permission level")
    public void cannotUnsubscribeToEventsViaAClientDueToMissingPermissionLevel(String user, String eventname, String clientName) {
        messageOperations.sendJSON(runner, new PayloadBuilder()
                .addField("type", "workspace.cohort.unsubscribe")
                .addField("cohortId", "${cohort_id}")
                .build(), clientName);
        messageOperations.receiveJSON(runner,
                                      action -> action.jsonPath("$.type",
                                                                Matchers.containsString("workspace.cohort.unsubscribe.error")),
                                      clientName);
    }
}
