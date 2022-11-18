package mercury.glue.step;

import static mercury.glue.wiring.CitrusConfiguration.MESSAGE_OPERATIONS;
import static org.hamcrest.Matchers.is;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;

import org.hamcrest.Matchers;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

import com.consol.citrus.annotations.CitrusResource;
import com.consol.citrus.dsl.runner.TestRunner;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.smartsparrow.rtm.message.send.LiteralBasicResponseMessage;

import cucumber.api.java.en.Then;
import cucumber.api.java.en.When;
import mercury.common.MessageOperations;

public class GraphQLMessageSteps {

    @CitrusResource
    private TestRunner runner;

    @Autowired
    @Qualifier(MESSAGE_OPERATIONS)
    private MessageOperations messageOperations;

    @Autowired
    private AuthenticationSteps authenticationSteps;

    private static final String PING_QUERY = "query {ping { result }}";

    @Then("^\"([^\"]*)\" sends a GraphQL ping message")
    public void sendGraphQLPingMessageAndReceivesPong(String accountName) {
        authenticationSteps.authenticateUser(accountName);
        messageOperations.sendGraphQL(runner, PING_QUERY);
    }

    @Then("^mercury should respond with a GraphQL pong response$")
    public void mercuryShouldRespondWithAccountDetails() {
        messageOperations.validateResponseType(runner, "graphql.response", action -> //
                action.jsonPath("$.response.data.ping.result", is("pong")) //
                        .jsonPath("$.replyTo", Matchers.not(Matchers.empty())));
    }

    @When("^\"([^\"]*)\" sends a GraphQL message$")
    public void sendsAGraphQLMessage(String accountName, String query) {
        authenticationSteps.authenticateUser(accountName);
        messageOperations.sendGraphQL(runner, query);
    }
    @When("^\"([^\"]*)\" sends a GraphQL message and authenticate via my cloud$")
    public void sendsAGraphQLMessageAuthenticatedByMyCloud(String accountName, String query) {
        authenticationSteps.authenticatesViaMyCloud(accountName);
        messageOperations.sendGraphQL(runner, query);
    }

    @Then("^mercury should respond with a GraphQL response$")
    public void mercuryShouldRespondWithAGraphQLResponse(String response) throws IOException {
        ObjectMapper jsonMapper = new ObjectMapper();
        // do not include null values.
        jsonMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        // do not include empty collections
        jsonMapper.setSerializationInclusion(JsonInclude.Include.NON_DEFAULT);
        Map expected = jsonMapper.readValue(response, LinkedHashMap.class);
        messageOperations.receiveJSON(runner, action ->
                action.payload(new LiteralBasicResponseMessage("graphql.response", "@notEmpty()@")
                        .addField("data", expected), jsonMapper));
    }

    @Then("^GraphQL should respond with error code (\\d+) and message \"([^\"]*)\"$")
    public void graphqlShouldRespondWithErrorCodeAndMessage(int code, String errorMessage) {
        messageOperations.receiveJSON(runner, action ->
                action.jsonPath("$.response.errors[0].message", "@contains(" + errorMessage + ")@")
                        .jsonPath("$.response.errors[0].extensions.code", code));
    }

    @Then("^the graphql request is denied due to missing permission level$")
    public void theGraphqlRequestIsDeniedDueToMissingPermissionLevel() {
        messageOperations.receiveJSON(runner, action -> action
                .jsonPath("$.response.errors[0].extensions.code", 403)
                .jsonPath("$.response.errors[0].extensions.type", "FORBIDDEN"));
    }

    @Then("mercury should respond with a GraphQL payload")
    public void mercuryShouldRespondWithAGraphQLPayload(String payload) {
        messageOperations.receiveJSON(runner, action -> action.payload(payload));
    }
}
