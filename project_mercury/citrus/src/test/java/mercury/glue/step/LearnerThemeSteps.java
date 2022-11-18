package mercury.glue.step;

import static mercury.common.Variables.interpolate;
import static mercury.common.Variables.nameFrom;
import static mercury.glue.wiring.CitrusConfiguration.MESSAGE_OPERATIONS;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.util.LinkedHashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

import com.consol.citrus.annotations.CitrusResource;
import com.consol.citrus.context.TestContext;
import com.consol.citrus.dsl.runner.TestRunner;
import com.smartsparrow.learner.data.LearnerSelectedThemePayload;
import com.smartsparrow.learner.data.LearnerThemeVariant;

import cucumber.api.java.en.Then;
import mercury.common.MessageOperations;
import mercury.common.PayloadBuilder;
import mercury.common.ResponseMessageValidationCallback;

public class LearnerThemeSteps {

    @CitrusResource
    private TestRunner runner;

    @Autowired
    @Qualifier(MESSAGE_OPERATIONS)
    private MessageOperations messageOperations;

    @Autowired
    private AuthenticationSteps authenticationSteps;


    @Then("{string} fetched selected theme for {string} successfully")
    public void fetchedSelectedThemeForSuccessfully(String user, String elementName) {

        authenticationSteps.authenticateUser(user);

        String message = new PayloadBuilder()
                .addField("type", "learner.selected.theme.get")
                .addField("elementId", interpolate(nameFrom(elementName, "id")))
                .build();

        messageOperations.sendJSON(runner, message);

        messageOperations.receiveJSON(runner, action -> action.validationCallback(
                new ResponseMessageValidationCallback<LearnerSelectedThemePayload>(LearnerSelectedThemePayload.class) {
                    @Override
                    public void validate(LearnerSelectedThemePayload selectedThemePayload,
                                         Map<String, Object> headers,
                                         TestContext context) {

                        assertNotNull(selectedThemePayload.getThemeId());
                        assertNotNull(selectedThemePayload.getElementId());
                        assertNotNull(selectedThemePayload.getThemeName());
                        assertNotNull(selectedThemePayload.getThemeVariants());

                        for (int i = 0; i < selectedThemePayload.getThemeVariants().size(); i++) {
                            LearnerThemeVariant learnerThemeVariant = selectedThemePayload.getThemeVariants().get(i);
                            assertNotNull(learnerThemeVariant.getThemeId());
                            assertNotNull(learnerThemeVariant.getVariantId());
                            assertNotNull(learnerThemeVariant.getVariantName());
                            assertNotNull(learnerThemeVariant.getState());
                            assertNull(learnerThemeVariant.getConfig());

                        }
                    }

                    @Override
                    public String getRootElementName() {
                        return "selectedThemePayload";
                    }

                    @Override
                    public String getType() {
                        return "learner.selected.theme.get.ok";
                    }
                }
        ));
    }

    @Then("{string} found no selected theme for {string}")
    public void foundNoSelectedThemeFor(String user, String elementName) {
        authenticationSteps.authenticateUser(user);

        String message = new PayloadBuilder()
                .addField("type", "learner.selected.theme.get")
                .addField("elementId", interpolate(nameFrom(elementName, "id")))
                .build();

        messageOperations.sendJSON(runner, message);

        messageOperations.receiveJSON(runner, action -> action.validationCallback(
                new ResponseMessageValidationCallback<LearnerSelectedThemePayload>(LearnerSelectedThemePayload.class) {
                    @Override
                    public void validate(LearnerSelectedThemePayload selectedThemePayload,
                                         Map<String, Object> headers,
                                         TestContext context) {
                        assertNotNull(selectedThemePayload);
                        assertNull(selectedThemePayload.getThemeId());
                        assertNull(selectedThemePayload.getElementId());
                    }

                    @Override
                    public String getRootElementName() {
                        return "selectedThemePayload";
                    }

                    @Override
                    public String getType() {
                        return "learner.selected.theme.get.ok";
                    }
                }
        ));
    }

    @Then("{string} get following learner theme variant info for {string} and theme {string}")
    public void getFollowingLearnerThemeVariantInfoForAndTheme(String user,
                                                               String variantName,
                                                               String themeName,
                                                               Map<String, String> expectedvariantName) {
        authenticationSteps.authenticateUser(user);

        String message = new PayloadBuilder()
                .addField("type", "learner.theme.variant.get")
                .addField("themeId", interpolate(nameFrom(themeName, "id")))
                .addField("variantId", interpolate(nameFrom(variantName, "id")))
                .build();

        messageOperations.sendJSON(runner, message);

        messageOperations.receiveJSON(runner, action -> action.validationCallback(
                new ResponseMessageValidationCallback<LearnerThemeVariant>(LearnerThemeVariant.class) {
                    @Override
                    public void validate(final LearnerThemeVariant actualThemeVariant,
                                         final Map<String, Object> headers,
                                         final TestContext context) {
                        assertNotNull(actualThemeVariant);
                        String variantName = actualThemeVariant.getVariantName();
                        assertNotNull(variantName);
                        assertEquals(expectedvariantName.get("variantName"), variantName);
                        assertNotNull(actualThemeVariant.getConfig());
                    }

                    @Override
                    public String getRootElementName() {
                        return "themeVariant";
                    }

                    @Override
                    public String getType() {
                        return "learner.theme.variant.get.ok";
                    }
                }));
    }
}
