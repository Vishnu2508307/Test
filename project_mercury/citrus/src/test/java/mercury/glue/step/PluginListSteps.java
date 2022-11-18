package mercury.glue.step;

import static mercury.common.Variables.nameFrom;
import static mercury.glue.step.PluginShareSteps.PLUGIN_ID_VAR;
import static mercury.glue.step.PluginSteps.DEFAULT_PLUGIN_NAME;
import static mercury.glue.wiring.CitrusConfiguration.MESSAGE_OPERATIONS;
import static mercury.helpers.plugin.PluginListHelper.authorPluginListOkResponse;
import static mercury.helpers.plugin.PluginListHelper.authorPluginListOkResponseEmpty;
import static mercury.helpers.plugin.PluginListHelper.authorPluginListOkResponseWithEmptyList;
import static mercury.helpers.plugin.PluginListHelper.authorPluginListRequest;
import static mercury.helpers.plugin.PluginListHelper.authorPluginListRequestWithPluginType;
import static mercury.helpers.plugin.PluginListHelper.authorPluginListRequestWithPluginTypeAndFilters;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.assertj.core.util.Maps;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

import com.consol.citrus.annotations.CitrusResource;
import com.consol.citrus.context.TestContext;
import com.consol.citrus.dsl.runner.TestRunner;
import com.smartsparrow.plugin.data.PluginFilter;
import com.smartsparrow.plugin.data.PluginFilterType;

import cucumber.api.java.en.Then;
import cucumber.api.java.en.When;
import mercury.common.MessageOperations;
import mercury.common.PayloadBuilder;
import mercury.common.ResponseMessageValidationCallback;
import mercury.common.Variables;

public class PluginListSteps {

    @Autowired
    @Qualifier(MESSAGE_OPERATIONS)
    private MessageOperations messageOperations;

    @CitrusResource
    private TestRunner runner;

    @Autowired
    private AuthenticationSteps authenticationSteps;

    @Then("^\"([^\"]*)\" can not see any plugin in workspace$")
    public void canNotSeeAnyPluginInWorkspace(String accountName) {
        canNotSeeAnyPlugin(accountName, "workspace");
    }

    @Then("^\"([^\"]*)\" can not see any plugin in author")
    public void canNotSeeAnyPluginInAuthor(String accountName) {
        canNotSeeAnyPlugin(accountName, "author");
    }

    private void canNotSeeAnyPlugin(String accountName, String context) {
        authenticationSteps.authenticateUser(accountName);

        PayloadBuilder payload = new PayloadBuilder();
        payload.addField("type", context + ".plugin.list");
        messageOperations.sendJSON(runner, payload.build());
        messageOperations.receiveJSON(runner,
                action -> action.payload("{ \"type\":\"" + context + ".plugin.list.ok\"," +
                        "  \"response\": {" +
                        "    \"plugins\":[]" +
                        "  }," +
                        "  \"replyTo\": \"@notEmpty()@\"" +
                        "}"));
    }

    @Then("^\"([^\"]*)\" can see this plugin in a list of visible plugins$")
    public void canSeeThisPluginInAListOfVisiblePlugins(String accountName) {
        authenticationSteps.authenticateUser(accountName);

        PayloadBuilder payload = new PayloadBuilder();
        payload.addField("type", "workspace.plugin.list");
        messageOperations.sendJSON(runner, payload.build());
        messageOperations.validateResponseType(runner, "workspace.plugin.list.ok",
                action -> action.payload("{ \"type\":\"workspace.plugin.list.ok\"," +
                        "  \"response\": {" +
                        "    \"plugins\":[{" +
                        "      \"pluginId\": \""+Variables.interpolate(PLUGIN_ID_VAR) +"\"," +
                        "      \"name\": \"" + DEFAULT_PLUGIN_NAME + "\"," +
                        "      \"subscriptionId\": \"@notEmpty()@\"," +
                        "      \"createdAt\": \"@notEmpty()@\"," +
                        "      \"publishMode\": \"@notEmpty()@\"," +
                        "      \"creator\": {" +
                        "        \"accountId\": \"@notEmpty()@\"," +
                        "        \"subscriptionId\": \"@notEmpty()@\"," +
                        "        \"iamRegion\": \"@notEmpty()@\"," +
                        "        \"primaryEmail\": \"@notEmpty()@\"," +
                        "        \"roles\": \"@notEmpty()@\"," +
                        "        \"email\": \"@notEmpty()@\"," +
                        "        \"authenticationType\": \"@notEmpty()@\"" +
                        "      }" +
                        "    }]" +
                        "  }," +
                        "\"replyTo\":\"@notEmpty()@\"" +
                        "}"));
    }

    @When("^\"([^\"]*)\" lists the workspace plugins$")
    public void listsTheWorkspacePlugins(String user) {
        authenticationSteps.authenticateUser(user);

        messageOperations.sendJSON(runner, new PayloadBuilder()
                .addField("type", "workspace.plugin.list")
                .build());
    }

    @Then("^the following plugins are returned$")
    public void theFollowingPluginsAreReturned(List<String> pluginNames) {
        messageOperations.receiveJSON(runner, action -> action.validationCallback(new ResponseMessageValidationCallback<List>(List.class) {
            @Override
            public void validate(List plugins, Map<String, Object> headers, TestContext context) {
                assertEquals(pluginNames.size(), plugins.size());

                Map<String, String> expected = pluginNames.stream()
                        .map(pluginName-> Maps.newHashMap(pluginName, Variables.nameFrom("plugin_id", pluginName)))
                        .reduce((prev, next) ->{
                            next.putAll(prev);
                            return next;
                        }).orElse(new HashMap<>());

                for (Object payload : plugins) {
                    Map plugin = (Map) payload;
                    String pluginId = (String) plugin.get("pluginId");
                    String name = (String) plugin.get("name");
                    assertEquals(context.getVariable(expected.get(name)), pluginId);
                }
            }

            @Override
            public String getRootElementName() {
                return "plugins";
            }

            @Override
            public String getType() {
                return "workspace.plugin.list.ok";
            }
        }));
    }

    @Then("^\"([^\"]*)\" can not see any plugin in the list of published plugins$")
    public void canNotSeeAnyPluginInTheListOfPublishedPlugins(String user) {
        authenticationSteps.authenticateUser(user);
        messageOperations.sendJSON(runner, authorPluginListRequest());
        messageOperations.receiveJSON(runner, action -> action.payload(authorPluginListOkResponseEmpty()));
    }

    @Then("^\"([^\"]*)\" can see in the list of published plugins$")
    public void canSeeInTheListOfPublishedPlugins(String user, Map<String, String> expectedPlugins) {
        authenticationSteps.authenticateUser(user);
        messageOperations.sendJSON(runner, authorPluginListRequest());
        messageOperations.receiveJSON(runner, action -> action.validationCallback(authorPluginListOkResponse(expectedPlugins)));
    }

    @Then("^\"([^\"]*)\" can see in the list of published plugins filtered by type \"([^\"]*)\"$")
    public void canSeeInTheListOfPublishedPluginsFilteredByType(String user, String pluginType, Map<String, String> expectedPlugins) {
        authenticationSteps.authenticateUser(user);
        messageOperations.sendJSON(runner, authorPluginListRequestWithPluginType(pluginType.toLowerCase()));
        messageOperations.receiveJSON(runner, action -> action.validationCallback(authorPluginListOkResponse(expectedPlugins)));
    }

    @When("{string} tries to list published plugins filtered by type {string} and filters")
    public void triesToListPublishedPluginsFilteredByTypeAndFilters(String user, String pluginType, Map<String, String> pluginfilters) {
        authenticationSteps.authenticateUser(user);
        Set<String> filterValues = new HashSet<>();
        filterValues.add(getVariableValue(nameFrom("plugin_id", pluginfilters.get("filterValues"))));
        PluginFilter pluginFilter = new PluginFilter()
                .setPluginId(UUID.fromString(getVariableValue(nameFrom("plugin_id", pluginfilters.get("pluginName")))))
                                     .setFilterType(PluginFilterType.valueOf(pluginfilters.get("filterType")))
                                     .setVersion(pluginfilters.get("version"))
                                     .setFilterValues(filterValues);
        List<PluginFilter> pluginFilterList = new ArrayList<>();
        pluginFilterList.add(pluginFilter);

        messageOperations.sendJSON(runner, authorPluginListRequestWithPluginTypeAndFilters(pluginType.toLowerCase(), pluginFilterList));
    }

    private String getVariableValue(String variableName) {
        return runner.variable(variableName, "${" + variableName + "}");
    }

    @Then("{string} list following published plugins filtered by type {string} and plugin filters")
    public void listFollowingPublishedPluginsFilteredByTypeAndPluginFilters(String user, String pluginType, Map<String, String> expectedPlugins) {
        messageOperations.receiveJSON(runner, action -> action.validationCallback(authorPluginListOkResponse(expectedPlugins)));
    }

    @Then("{string} list empty published plugins filtered by type {string} and plugin filters")
    public void listEmptyPublishedPluginsFilteredByTypeAndPluginFilters(String user, String pluginType) {
        messageOperations.receiveJSON(runner, action -> action.validationCallback(authorPluginListOkResponseWithEmptyList()));

    }

    @When("{string} tries to list published plugins filtered by type {string} and filter values")
    public void triesToListPublishedPluginsFilteredByTypeAndFilterValues(String user, String pluginType, Map<String, String> pluginfilters) {
        authenticationSteps.authenticateUser(user);
        Set<String> filterValues = new HashSet<>();
        filterValues.add(pluginfilters.get("filterValues"));
        PluginFilter pluginFilter = new PluginFilter()
                .setPluginId(UUID.fromString(getVariableValue(nameFrom("plugin_id", pluginfilters.get("pluginName")))))
                .setFilterType(PluginFilterType.valueOf(pluginfilters.get("filterType")))
                .setVersion(pluginfilters.get("version"))
                .setFilterValues(filterValues);
        List<PluginFilter> pluginFilterList = new ArrayList<>();
        pluginFilterList.add(pluginFilter);

        messageOperations.sendJSON(runner, authorPluginListRequestWithPluginTypeAndFilters(pluginType.toLowerCase(), pluginFilterList));
    }
}
