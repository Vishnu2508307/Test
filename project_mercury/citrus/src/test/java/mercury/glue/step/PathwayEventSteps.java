package mercury.glue.step;

import static mercury.common.Variables.interpolate;
import static mercury.common.Variables.nameFrom;
import static org.junit.Assert.assertEquals;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;

import com.consol.citrus.annotations.CitrusResource;
import com.consol.citrus.context.TestContext;
import com.consol.citrus.dsl.runner.TestRunner;
import com.consol.citrus.validation.json.JsonMappingValidationCallback;
import com.smartsparrow.courseware.data.CoursewareElementType;
import com.smartsparrow.courseware.data.WalkableChild;
import com.smartsparrow.rtm.message.send.BasicResponseMessage;
import com.smartsparrow.util.Enums;

import cucumber.api.java.en.Then;
import mercury.common.MessageOperations;

public class PathwayEventSteps {

    @CitrusResource
    private TestRunner runner;

    @Autowired
    private MessageOperations messageOperations;

    @Then("^\"([^\"]*)\" should receive an action \"([^\"]*)\" message for the \"([^\"]*)\" pathway$")
    public void shouldReceiveAnActionMessageForThePathway(String clientName, String coursewareAction, String pathwayName) {
        messageOperations.receiveJSON(runner, action ->
                action.jsonPath("$.response.elementId", interpolate(nameFrom(pathwayName, "id")))
                        .jsonPath("$.response.action", coursewareAction)
                        .jsonPath("$.response.rtmEvent",
                                  coursewareAction.startsWith("PATHWAY") ? coursewareAction : "PATHWAY_" + coursewareAction)
                        .jsonPath("$.response.elementType", "PATHWAY"), clientName);
    }

    @SuppressWarnings("unchecked")
    @Then("^\"([^\"]*)\" should receive an action \"([^\"]*)\" message for the \"([^\"]*)\" pathway with walkables")
    public void shouldReceiveAnActionMessageForTheActivityWithPathways(String clientName, String coursewareAction,
                                                                       String pathwayName, Map<String, String> walkables) {

        messageOperations.receiveJSON(runner, action -> action.validationCallback(
                new JsonMappingValidationCallback<BasicResponseMessage>(BasicResponseMessage.class) {

                    @Override
                    public void validate(BasicResponseMessage payload, Map<String, Object> headers, TestContext context) {
                        assertEquals("author.activity.broadcast", payload.getType());
                        Map<String, Object> response = payload.getResponse();
                        assertEquals(context.getVariable(nameFrom(pathwayName, "id")), response.get("elementId"));
                        assertEquals(coursewareAction, response.get("action"));
                        assertEquals(coursewareAction.startsWith("PATHWAY") ? coursewareAction : "PATHWAY_" + coursewareAction,
                                     response.get("rtmEvent"));
                        assertEquals("PATHWAY", response.get("elementType"));

                        List<WalkableChild> expectedWalkables = walkables.entrySet().stream()
                                .map(entry -> new WalkableChild()
                                        .setElementId(getId(context, entry.getKey()))
                                        .setElementType(getType(entry.getValue())))
                                .collect(Collectors.toList());

                        List<Map<String, String>> list = (List<Map<String, String>>) response.get("walkables");

                        List<WalkableChild> actualWalkables = list.stream().map(item -> new WalkableChild()
                                .setElementId(getId(item.get("elementId")))
                                .setElementType(getType(item.get("elementType"))))
                                .collect(Collectors.toList());

                        assertEquals(expectedWalkables, actualWalkables);
                    }
                }), clientName);
    }

    private UUID getId(TestContext context, String walkableName) {
        String variableName = nameFrom(walkableName, "id");
        return UUID.fromString(context.getVariable(variableName));
    }

    private UUID getId(String idValue) {
        return UUID.fromString(idValue);
    }


    private CoursewareElementType getType(String typeName) {
        return Enums.of(CoursewareElementType.class, typeName);
    }
}
