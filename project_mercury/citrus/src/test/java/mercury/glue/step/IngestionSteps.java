package mercury.glue.step;

import static mercury.common.Variables.interpolate;
import static mercury.common.Variables.nameFrom;
import static mercury.glue.wiring.CitrusConfiguration.HTTP_CLIENT;
import static mercury.glue.wiring.CitrusConfiguration.MESSAGE_OPERATIONS;
import static mercury.helpers.workspace.WorkspaceHelper.getWorkspaceIdVar;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import com.smartsparrow.ingestion.data.IngestionAdapterType;
import org.apache.http.entity.ContentType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;

import com.consol.citrus.annotations.CitrusResource;
import com.consol.citrus.context.TestContext;
import com.consol.citrus.dsl.runner.TestRunner;
import com.consol.citrus.http.client.HttpClient;
import com.consol.citrus.message.MessageType;

import cucumber.api.java.en.And;
import cucumber.api.java.en.Given;
import cucumber.api.java.en.Then;
import cucumber.api.java.en.When;
import mercury.common.MessageOperations;
import mercury.common.PayloadBuilder;
import mercury.common.ResponseMessageValidationCallback;

public class IngestionSteps {

    private static final String DEFAULT_URL = "http://default.ambrosia.url";
    private static final String DEFAULT_CONFIG = "[{ 'key': 'value' },{ 'title': 'titleName' }]";
    private static final String DEFAULT_COURSE_NAME = "Test Course";

    public static final String DEFAULT_INGESTION_NAME = "ingestion";
    public static final String INGESTION_ID_VAR = nameFrom(DEFAULT_INGESTION_NAME, "id");

    @CitrusResource
    private TestRunner runner;

    @Autowired
    @Qualifier(HTTP_CLIENT)
    private HttpClient httpClient;

    @Autowired
    @Qualifier(MESSAGE_OPERATIONS)
    private MessageOperations messageOperations;

    @Autowired
    private AuthenticationSteps authenticationSteps;

    @When("^\"([^\"]*)\" tries creating an ingestion request in project \"([^\"]*)\" in workspace \"([^\"]*)\"$")
    public void triesCreatingIngestion(String user, String project, String workspace) {
        authenticationSteps.authenticateUser(user);

        messageOperations.sendJSON(runner, new PayloadBuilder()
                                           .addField("type", "project.ingest.request")
                                           .addField("projectId", interpolate(nameFrom(project, "id")))
                                           .addField("workspaceId", interpolate(getWorkspaceIdVar(workspace)))
                                           .addField("url", DEFAULT_URL)
                                           .addField("configFields", DEFAULT_CONFIG)
                                           .addField("courseName", DEFAULT_COURSE_NAME)
                                           .build()
        );
    }

    @When("^\"([^\"]*)\" tries creating an ingestion request in project \"([^\"]*)\" in workspace \"([^\"]*)\" with course name \"([^\"]*)\"$")
    public void triesCreatingIngestionWithCourseName(String user, String project, String workspace, String courseName) {
        authenticationSteps.authenticateUser(user);

        messageOperations.sendJSON(runner, new PayloadBuilder()
                .addField("type", "project.ingest.request")
                .addField("projectId", interpolate(nameFrom(project, "id")))
                .addField("workspaceId", interpolate(getWorkspaceIdVar(workspace)))
                .addField("url", DEFAULT_URL)
                .addField("configFields", DEFAULT_CONFIG)
                .addField("courseName", courseName)
                .build()
        );
    }

    @Then("^the ingestion request is successfully created")
    public void theIngestionIsSuccessfullyCreated() {
        messageOperations.receiveJSON(runner, action ->
                action.jsonPath("$.type", "project.ingest.request.ok")
                      .jsonPath("$.response.ingestionId", "@notEmpty()@")
                      .extractFromPayload("$.response.ingestionId", INGESTION_ID_VAR)
        );
    }

    @Then("^the ingestion is not created due to missing permission level$")
    public void theIngestionIsNotCreatedDueToMissingPermissionLevel() {
        messageOperations.receiveJSON(runner, action ->
                action.payload("{" +
                               "\"type\":\"project.ingest.request.error\"," +
                               "\"code\":401," +
                               "\"message\":\"@notEmpty()@\"," +
                               "\"replyTo\":\"@notEmpty()@\"}"));
    }

    @Then("^the ingestion is not created due to existing course name$")
    public void theIngestionIsNotCreatedDueToExistingCourseName() {
        messageOperations.receiveJSON(runner, action ->
                action.payload("{" +
                                       "\"type\":\"project.ingest.request.error\"," +
                                       "\"code\":409," +
                                       "\"message\":\"@notEmpty()@\"," +
                                       "\"replyTo\":\"@notEmpty()@\"}"));
    }

    @When("^\"([^\"]*)\" tries to fetch the ingestion request in project \"([^\"]*)\"$")
    public void triesFetchingIngestion(String user, String project) {
        authenticationSteps.authenticateUser(user);

        messageOperations.sendJSON(runner, new PayloadBuilder()
                .addField("type", "project.ingest.get")
                .addField("ingestionId", interpolate(INGESTION_ID_VAR))
                .build()
        );
    }

    @Then("^the ingestion request is successfully fetched$")
    public void theIngestionIsSuccessfullyFetched() {
        messageOperations.receiveJSON(runner, action ->
            action.jsonPath("$.type", "project.ingest.get.ok")
                  .jsonPath("$.response.ingestion", "@notEmpty()@")
        );
    }

    @Then("^the ingestion is not fetched due to missing permission level$")
    public void theIngestionIsNotFetchedDueToMissingPermissionLevel() {
        messageOperations.receiveJSON(runner, action ->
                action.payload("{" +
                               "\"type\":\"project.ingest.get.error\"," +
                               "\"code\":401," +
                               "\"message\":\"@notEmpty()@\"," +
                               "\"replyTo\":\"@notEmpty()@\"}"));
    }

    @Given("{string} has created an ingestion request in project {string} in workspace {string}")
    public void hasCreatedAnIngestion(String accountName, String project, String workspace) {
        authenticationSteps.authenticateUser(accountName);
        messageOperations.sendJSON(runner, new PayloadBuilder()
                .addField("type", "project.ingest.request")
                .addField("projectId", interpolate(nameFrom(project, "id")))
                .addField("workspaceId", interpolate(getWorkspaceIdVar(workspace)))
                .addField("url", DEFAULT_URL)
                .addField("configFields", DEFAULT_CONFIG)
                .addField("courseName", DEFAULT_COURSE_NAME)
                .build()
        );
        messageOperations.receiveJSON(runner, action ->
                action.extractFromPayload("$.response.ingestionId", nameFrom(project, "ingestionId")));
    }

    @When("{string} subscribes to the ingestion for project {string}")
    public void subscribesToIngestion(String accountName, String project) {
        authenticationSteps.authenticateUser(accountName);
        messageOperations.sendJSON(runner, new PayloadBuilder()
                .addField("type", "project.ingest.subscribe")
                .addField("ingestionId", interpolate(nameFrom(project, "ingestionId")))
                .build());
    }

    @Then("{string} is successfully subscribed to the ingestion")
    public void ingestionIsSuccessfullySubscribed(String accountName) {
        messageOperations.receiveJSON(runner, action -> action
                .jsonPath("$.type", "project.ingest.subscribe.ok")
                .jsonPath("$.response.rtmSubscriptionId", "@notEmpty()@")
        );
    }

    @When("{string} unsubscribes to the ingestion for project {string}")
    public void unsubscribesToIngestion(String accountName, String project) {
        authenticationSteps.authenticateUser(accountName);
        messageOperations.sendJSON(runner, new PayloadBuilder()
                .addField("type", "project.ingest.unsubscribe")
                .addField("ingestionId", interpolate(nameFrom(project, "ingestionId")))
                .build());
    }

    @Then("{string} is successfully unsubscribed to the ingestion")
    public void ingestionIsSuccessfullyUnsubscribed(String accountName) {
        messageOperations.receiveJSON(runner, action -> action
                .jsonPath("$.type", "project.ingest.unsubscribe.ok")
        );
    }

    @When("{string} tries to list all ingestion summaries in project {string}")
    public void listsAllIngestionSummariesInProject(String accountName, String project) {
        authenticationSteps.authenticateUser(accountName);

        messageOperations.sendJSON(runner, new PayloadBuilder()
                .addField("type", "project.ingest.list")
                .addField("projectId", interpolate(nameFrom(project, "id")))
                .build());
    }

    @Then("^the ingestion summaries are not listed due to missing permission level$")
    public void theIngestionSummariesAreNotListedDueToMissingPermissionLevel() {
        messageOperations.receiveJSON(runner, action ->
                action.payload("{" +
                                       "\"type\":\"project.ingest.list.error\"," +
                                       "\"code\":401," +
                                       "\"message\":\"@notEmpty()@\"," +
                                       "\"replyTo\":\"@notEmpty()@\"}"));
    }

    @Then("the following ingestion summaries are listed for project {string} with course names")
    public void theFollowingIngestionSummariesAreListedForProjectWithUrls(final String project, final List<String> expectedCourseNames) {
        messageOperations.receiveJSON(runner, action -> action.validationCallback(
                new ResponseMessageValidationCallback<ArrayList>(ArrayList.class) {
                    @Override
                    public void validate(final ArrayList ingestionSummaries, final Map<String, Object> headers, final TestContext context) {
                        assertEquals(expectedCourseNames.size(), ingestionSummaries.size());
                        Set<String> actualIngestionSummaryCourses = new HashSet<>(ingestionSummaries.size());

                        for (Object ingestionSummary : ingestionSummaries) {
                            actualIngestionSummaryCourses.add((String) ((Map) ingestionSummary).get("courseName"));
                        }

                        Set<String> expectedIngestionSummaryCourses = expectedCourseNames.stream()
                                .collect(Collectors.toSet());

                        assertEquals(expectedIngestionSummaryCourses, actualIngestionSummaryCourses);
                    }

                    @Override
                    public String getRootElementName() {
                        return "ingestionSummaries";
                    }

                    @Override
                    public String getType() {
                        return "project.ingest.list.ok";
                    }
                }));
    }

    @When("^\"([^\"]*)\" tries starting an ingestion request in project \"([^\"]*)\" with adapter type \"([^\"]*)\"$")
    public void triesStartingIngestion(String user, String project, String adapterType) {
        authenticationSteps.authenticateUser(user);
        messageOperations.sendJSON(runner, new PayloadBuilder()
                .addField("type", "project.ingest.start")
                .addField("ingestionId", interpolate(nameFrom(project, "ingestionId")))
                .addField("adapterType", adapterType)
                .build());
    }

    @Then("^the ingestion is not started due to missing permission level$")
    public void theIngestionIsNotStartedDueToMissingPermissionLevel() {
        messageOperations.receiveJSON(runner, action ->
                action.payload("{" +
                                       "\"type\":\"project.ingest.start.error\"," +
                                       "\"code\":401," +
                                       "\"message\":\"@notEmpty()@\"," +
                                       "\"replyTo\":\"@notEmpty()@\"}"));
    }

    @Then("^the ingestion is not started due to invalid adapter type$")
    public void theIngestionIsNotStartedDueToInvalidAdapterType() {
        messageOperations.receiveJSON(runner, action ->
                action.payload("{" +
                                       "\"type\":\"project.ingest.start.error\"," +
                                       "\"code\":400," +
                                       "\"message\":\"@notEmpty()@\"," +
                                       "\"replyTo\":\"@notEmpty()@\"}"));
    }

    @Then("^the ingestion is not started due to missing UPLOADED status$")
    public void theIngestionIsNotStartedDueToMissingUploadedStatus() {
        messageOperations.receiveJSON(runner, action ->
                action.payload("{" +
                                       "\"type\":\"project.ingest.start.error\"," +
                                       "\"code\":422," +
                                       "\"message\":\"@notEmpty()@\"," +
                                       "\"replyTo\":\"@notEmpty()@\"}"));
    }

    @Given("{string} has updated ingestion request in project {string} with status UPLOADED")
    public void hasUpdatedIngestion(String accountName, String project) {
        authenticationSteps.authenticateUser(accountName);
        String ingestionId = interpolate(nameFrom(project, "ingestionId"));
        String projectId = interpolate(nameFrom(project, "id"));
        String snsBody = "{\n" +
                "   \"Type\":\"Notification\",\n" +
                "   \"MessageId\":\"7a6789f0-02f0-5ed3-8a11-deebcd08f145\",\n" +
                "   \"TopicArn\":\"arn:aws:sns:us-east-2:167186109795:name_sns_topic\",\n" +
                "   \"Message\":{\n" +
                "      \"id\":\""+ingestionId+"\",\n" +
                "      \"projectId\":\""+projectId+"\",\n" +
                "      \"creatorId\":\"bda70280-e6dd-11e8-8487-2f0b656e8810\",\n" +
                "      \"configFields\":\"[{ 'key': 'value' },{ 'title': 'titleName' }]\",\n" +
                "      \"courseName\":\"Test Course\",\n" +
                "      \"ambrosiaUrl\":\"http://default.ambrosia.url\",\n" +
                "      \"status\":\"UPLOADED\",\n" +
                "      \"ingestionStats\":\"[{'elements':100}]]\"\n" +
                "   },\n" +
                "   \"Timestamp\":\"1987-04-23T17:17:44.897Z\",\n" +
                "   \"SignatureVersion\":\"1\",\n" +
                "   \"Signature\":\"string\",\n" +
                "   \"SigningCertURL\":\"url\",\n" +
                "   \"UnsubscribeURL\":\"url\",\n" +
                "   \"MessageAttributes\":{\n" +
                "      \"bearerToken\":{\n" +
                "         \"Type\":\"String\",\n" +
                "         \"Value\":\"D3ujbW0uS_Kf0OXbWz7Ey0EdhmakQeUq\"\n" +
                "      }\n" +
                "   }\n" +
                "}";

        runner.http(action -> action.client(httpClient)
                .send().post("/ingestion/upload/result/")
                .messageType(MessageType.JSON)
                .contentType(ContentType.APPLICATION_JSON.getMimeType())
                .accept(ContentType.APPLICATION_JSON.getMimeType())
                .payload(snsBody));

        runner.http(action -> action.client(httpClient)
                .receive()
                .response()
                .status(HttpStatus.OK));
    }

    @Then("^the ingestion request is successfully started")
    public void theIngestionIsSuccessfullyStarted() {
        messageOperations.receiveJSON(runner, action ->
                action.jsonPath("$.type", "project.ingest.start.ok")
                        .jsonPath("$.response.ingestionId", "@notEmpty()@")
                        .extractFromPayload("$.response.ingestionId", INGESTION_ID_VAR)
        );
    }


    @When("^\"([^\"]*)\" tries to update the ingestion request status in project \"([^\"]*)\"$")
    public void triesUpdatingIngestion(String user, String project) {
        authenticationSteps.authenticateUser(user);

        messageOperations.sendJSON(runner, new PayloadBuilder()
                .addField("type", "project.ingest.update")
                .addField("ingestionId", interpolate(INGESTION_ID_VAR))
                .addField("projectId", interpolate(nameFrom(project, "id")))
                .addField("status", "UPLOAD_FAILED")
                .build()
        );
    }

    @Then("^the ingestion is not updated due to missing permission level$")
    public void theIngestionIsNotUpdatedDueToMissingPermissionLevel() {
        messageOperations.receiveJSON(runner, action ->
                action.payload("{" +
                               "\"type\":\"project.ingest.update.error\"," +
                               "\"code\":401," +
                               "\"message\":\"@notEmpty()@\"," +
                               "\"replyTo\":\"@notEmpty()@\"}"));
    }

    @Then("^the ingestion request is successfully updated")
    public void theIngestionIsSuccessfullyUpdated() {
        messageOperations.receiveJSON(runner, action ->
                action.jsonPath("$.type", "project.ingest.update.ok")
        );
    }

    @When("^\"([^\"]*)\" tries deleting an ingestion request in project \"([^\"]*)\"$")
    public void triesDeletingIngestion(String user, String project) {
        authenticationSteps.authenticateUser(user);

        messageOperations.sendJSON(runner, new PayloadBuilder()
                .addField("type", "project.ingest.delete")
                .addField("ingestionId", interpolate(INGESTION_ID_VAR))
                .build()
        );
    }

    @Then("^the ingestion is not deleted due to missing permission level$")
    public void theIngestionIsNotDeletedDueToMissingPermissionLevel() {
        messageOperations.receiveJSON(runner, action ->
                action.payload("{" +
                               "\"type\":\"project.ingest.delete.error\"," +
                               "\"code\":401," +
                               "\"message\":\"@notEmpty()@\"," +
                               "\"replyTo\":\"@notEmpty()@\"}"));
    }

    @Then("^the ingestion request is successfully deleted")
    public void theIngestionIsSuccessfullyDeleted() {
        messageOperations.receiveJSON(runner, action ->
                action.jsonPath("$.type", "project.ingest.delete.ok")
        );
    }

    @When("^\"([^\"]*)\" tries creating an ingestion request in project \"([^\"]*)\" in workspace \"([^\"]*)\" with root element \"([^\"]*)\"$")
    public void triesCreatingIngestion(String user, String project, String workspace, String rootElement) {
        authenticationSteps.authenticateUser(user);

        messageOperations.sendJSON(runner, new PayloadBuilder()
                .addField("type", "project.ingest.request")
                .addField("projectId", interpolate(nameFrom(project, "id")))
                .addField("workspaceId", interpolate(getWorkspaceIdVar(workspace)))
                .addField("url", DEFAULT_URL)
                .addField("configFields", DEFAULT_CONFIG)
                .addField("courseName", DEFAULT_COURSE_NAME)
                .addField("rootElementId", interpolate(nameFrom(rootElement, "id")))
                .build()
        );
    }

    @When("^\"([^\"]*)\" tries to fetch the ingestion request in project \"([^\"]*)\" by root element \"([^\"]*)\"$")
    public void triesFetchingIngestion(String user, String project, String rootElement) {
        authenticationSteps.authenticateUser(user);

        messageOperations.sendJSON(runner, new PayloadBuilder()
                .addField("type", "project.ingestion.root.get")
                .addField("rootElementId", interpolate(nameFrom(rootElement, "id")))
                .addField("projectId", interpolate(nameFrom(project, "id")))
                .build()
        );
    }

    @Then("^the ingestion by root element is not fetched due to missing permission level$")
    public void theIngestionByRootElementIsNotFetchedDueToMissingPermissionLevel() {
        messageOperations.receiveJSON(runner, action ->
                action.payload("{" +
                                       "\"type\":\"project.ingestion.root.get.error\"," +
                                       "\"code\":401," +
                                       "\"message\":\"@notEmpty()@\"," +
                                       "\"replyTo\":\"@notEmpty()@\"}"));
    }

    @Then("^the ingestion request is successfully fetched by root element \"([^\"]*)\"$")
    public void theIngestionIsSuccessfullyFetchedByRootElement(String rootElement) {
        messageOperations.receiveJSON(runner, action ->
                action.jsonPath("$.type", "project.ingestion.root.get.ok")
                        .jsonPath("$.response.ingestionSummaries", "@notEmpty()@")
                        .extractFromPayload("$.response.ingestionSummaries[0].rootElementId", interpolate(nameFrom(rootElement, "id")))
        );
    }

    @And("{string} gets an event broadcast as {string} for project {string}")
    public void getsAnEventBroadcastForProject(String user, String status, String projectName) {
        messageOperations.receiveJSON(runner, action -> action
                .jsonPath("$.type", "workspace.project.broadcast")
                .jsonPath("$.response.projectId", interpolate(nameFrom(projectName, "id")))
                .jsonPath("$.response.ingestionId", "@notEmpty()@")
                .jsonPath("$.response.ingestionStatus", status)
                .jsonPath("$.response.rtmEvent", "PROJECT_EVENT"));
    }

    @When("^\"([^\"]*)\" tries creating an ingestion request in activity \"([^\"]*)\" in project \"([^\"]*)\" in workspace \"([^\"]*)\"$")
    public void triesCreatingActivityIngestion(String user, String course, String project, String workspace) {
        authenticationSteps.authenticateUser(user);

        messageOperations.sendJSON(runner, new PayloadBuilder()
                .addField("type", "project.ingest.request")
                .addField("projectId", interpolate(nameFrom(project, "id")))
                .addField("workspaceId", interpolate(getWorkspaceIdVar(workspace)))
                .addField("rootElementId", interpolate(nameFrom(course, "id")))//TODO need to update root element id
                .addField("url", DEFAULT_URL)
                .addField("configFields", DEFAULT_CONFIG)
                .addField("courseName", DEFAULT_COURSE_NAME)
                .build()
        );
    }

    @And("{string} gets an event broadcast as {string} for activity {string}")
    public void getsAnEventBroadcastForActivity(String user, String status, String activity) {
        messageOperations.receiveJSON(runner, action -> action
                .jsonPath("$.type", "author.activity.broadcast")
                .jsonPath("$.response.rootElementId", interpolate(nameFrom(activity, "id")))
                .jsonPath("$.response.ingestionId","@notEmpty()@")
                .jsonPath("$.response.ingestionStatus", status)
                .jsonPath("$.response.rtmEvent", "ACTIVITY_INGESTION"));
    }

    @When("^\"([^\"]*)\" tries deleting an ingestion request in activity \"([^\"]*)\"$")
    public void triesDeletingActivityIngestion(String user, String activity) {
        authenticationSteps.authenticateUser(user);

        messageOperations.sendJSON(runner, new PayloadBuilder()
                .addField("type", "project.ingest.delete")
                .addField("ingestionId", interpolate(INGESTION_ID_VAR))
                .build()
        );
    }

    @When("^\"([^\"]*)\" tries to update the ingestion request status in activity \"([^\"]*)\" and in project \"([^\"]*)\"$")
    public void triesUpdatingActivityIngestion(String user, String activity, String project) {
        authenticationSteps.authenticateUser(user);

        messageOperations.sendJSON(runner, new PayloadBuilder()
                .addField("type", "project.ingest.update")
                .addField("ingestionId", interpolate(INGESTION_ID_VAR))
                .addField("projectId", interpolate(nameFrom(project, "id")))
                .addField("status", "UPLOAD_FAILED")
                .build()
        );
    }

}
