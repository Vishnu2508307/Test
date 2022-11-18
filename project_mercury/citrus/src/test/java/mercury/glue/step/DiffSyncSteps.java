package mercury.glue.step;

import static mercury.common.Variables.interpolate;
import static mercury.common.Variables.nameFrom;
import static mercury.glue.wiring.CitrusConfiguration.MESSAGE_OPERATIONS;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

import com.consol.citrus.annotations.CitrusResource;
import com.consol.citrus.context.TestContext;
import com.consol.citrus.dsl.runner.TestRunner;
import com.smartsparrow.courseware.payload.ActivityPayload;
import com.smartsparrow.data.InstanceType;
import com.smartsparrow.util.UUIDs;

import cucumber.api.java.en.Then;
import data.DiffMatchPatchCustom;
import data.Patch;
import data.Version;
import mercury.common.MessageOperations;
import mercury.common.PayloadBuilder;
import mercury.common.ResponseMessageValidationCallback;

public class DiffSyncSteps {
    @CitrusResource
    private TestRunner runner;

    @Autowired
    @Qualifier(MESSAGE_OPERATIONS)
    private MessageOperations messageOperations;

    @Autowired
    private AuthenticationSteps authenticationSteps;

    private static final InstanceType instanceType = InstanceType.DEFAULT;

    @Then("{string} initiated diff sync on entityType {string} and entityId {string}")
    public void initiatedDiffSyncOnEntityTypeAndEntityId(String user, String entityType, String entityId) {

        authenticationSteps.authenticateUser(user);

        String message = new PayloadBuilder()
                .addField("type", "diff.sync.start")
                .addField("entityType", entityType)
                .addField("entityId", interpolate(nameFrom(entityId, "id")))
                .build();

        messageOperations.sendJSON(runner, message, user);

        messageOperations.receiveJSON(runner, action ->
                action.payload("{\"type\":\"diff.sync.start.ok\",\"replyTo\":\"@notEmpty()@\"}"), user);
    }

    @Then("{string} initiates diff sync patch on entityType {string} and entityId {string} with config {string}")
    public void initiatesdDiffSyncPatchOnOnEntityTypeAndEntityId(String user, String entityType, String entityId, String config) {
        //
        DiffMatchPatchCustom dmp = new DiffMatchPatchCustom();

        LinkedList<DiffMatchPatchCustom.Patch> diffMatchPatch_one;
        String text1 = "";
        String text2 = config;
        LinkedList<DiffMatchPatchCustom.Diff> diffs_one = dmp.diffMain(text1, text2, false);
        diffMatchPatch_one = dmp.patchMake(diffs_one);

        List<Patch> patchesRequests = new ArrayList<>();
        Patch patchRequest = new Patch();
        patchRequest.setId(UUIDs.timeBased());
        patchRequest.setPatches(diffMatchPatch_one);
        patchRequest.setM(new Version().setValue(Long.valueOf(0)));
        patchRequest.setN(new Version().setValue(Long.valueOf(0)));
        patchesRequests.add(patchRequest);

        String message = new PayloadBuilder()
                .addField("type", "diff.sync.patch")
                .addField("entityType", entityType)
                .addField("entityId", interpolate(nameFrom(entityId, "id")))
                .addField("patches", patchesRequests)
                .build();

        messageOperations.sendJSON(runner, message, user);
    }

    @Then("{string} successfully fetched {string} activity config")
    public void successfullyFetchedActivityConfig(String user, String activityName, String activityConfig) {
        messageOperations.sendJSON(runner, "{" +
                "    \"type\": \"author.activity.get\"," +
                "    \"activityId\": \"" + interpolate(nameFrom(activityName, "id")) + "\"" +
                "}");

        messageOperations.receiveJSON(runner, action -> action.validationCallback(
                new ResponseMessageValidationCallback<ActivityPayload>(ActivityPayload.class) {
                    @Override
                    public String getRootElementName() {
                        return "activity";
                    }

                    @Override
                    public String getType() {
                        return "author.activity.get.ok";
                    }

                    @Override
                    @SuppressWarnings("unchecked")
                    public void validate(ActivityPayload activity, Map<String, Object> headers, TestContext context) {
                        assertNotNull(activity);
                        assertEquals(activity.getConfig(), activityConfig);
                    }
                }));
    }

    @Then("{string} should receive ACK notification with type {string}")
    public void shouldReceiveAckNotification(String user, String messageType) {
        messageOperations.receiveJSON(runner, action-> action
                .payload(clientAckBroadcastMessage(messageType)), user);
    }

    public static String clientAckBroadcastMessage(String messageType) {
        return "{" +
                "\"type\":\"diffSync.ack\"," +
                "\"response\":{" +
                "\"id\":\"" + "@notEmpty()@" + "\"," +
                "\"clientId\":\"" + "@notEmpty()@" + "\"," +
                "\"n\":\"" + "@notEmpty()@" + "\"," +
                "\"m\":\"" + "@notEmpty()@" + "\"," +
                "\"type\":\"ACK\"" +
                "}" +
                "}";
    }

    @Then("{string} should receive PATCH notification with type {string}")
    public void shouldReceivePATCHNotification(String user, String messageType) {
        messageOperations.receiveJSON(runner, action-> action
                .payload(clientPatchBroadcastMessage(messageType)), user);
    }

    public static String clientPatchBroadcastMessage(String clientId) {
        return "{" +
                "\"type\":\"diffSync.patch\"," +
                "\"response\":{" +
                "\"id\":\"" + "@notEmpty()@" + "\"," +
                "\"clientId\":\"" + "@notEmpty()@" + "\"," +
                "\"n\":\"" + "@notEmpty()@" + "\"," +
                "\"m\":\"" + "@notEmpty()@" + "\"," +
                "\"patches\":\"" + "@notEmpty()@" + "\"" +
                "}" +
                "}";
    }

    @Then("{string} diff sync patched is successfully")
    public void userDiffSyncPatchedIsSuccessfully(String user) {
        messageOperations.receiveJSON(runner, action ->
                action.payload("{\"type\":\"diff.sync.patch.ok\",\"replyTo\":\"@notEmpty()@\"}"), user);
    }

    @Then("{string} ended the diff sync on entityType {string} and entityId {string}")
    public void endedTheDiffSyncOnEntityTypeAndEntityId(String user, String entityType, String entityId) {
        authenticationSteps.authenticateUser(user);

        String message = new PayloadBuilder()
                .addField("type", "diff.sync.end")
                .addField("entityType", entityType)
                .addField("entityId", interpolate(nameFrom(entityId, "id")))
                .build();

        messageOperations.sendJSON(runner, message, user);

        messageOperations.receiveJSON(runner, action ->
                action.payload("{\"type\":\"diff.sync.end.ok\",\"replyTo\":\"@notEmpty()@\"}"), user);
    }
}
