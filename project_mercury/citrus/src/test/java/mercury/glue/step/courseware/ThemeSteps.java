package mercury.glue.step.courseware;

import static mercury.common.Variables.interpolate;
import static mercury.common.Variables.nameFrom;
import static mercury.glue.wiring.CitrusConfiguration.MESSAGE_OPERATIONS;
import static mercury.helpers.workspace.WorkspaceHelper.getWorkspaceIdVar;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.junit.Assert;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

import com.consol.citrus.annotations.CitrusResource;
import com.consol.citrus.context.TestContext;
import com.consol.citrus.dsl.runner.TestRunner;
import com.smartsparrow.workspace.data.IconLibrary;
import com.smartsparrow.workspace.data.IconLibraryState;

import cucumber.api.java.en.Then;
import cucumber.api.java.en.When;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import mercury.common.MessageOperations;
import mercury.common.PayloadBuilder;
import mercury.common.ResponseMessageValidationCallback;
import mercury.glue.step.AuthenticationSteps;

public class ThemeSteps {

    @CitrusResource
    private TestRunner runner;

    @Autowired
    @Qualifier(MESSAGE_OPERATIONS)
    private MessageOperations messageOperations;

    @Autowired
    private AuthenticationSteps authenticationSteps;

    @When("{string} creates theme {string} for workspace {string}")
    public void createsThemeForWorkspaceWithConfig(String user, String themeName, String workspaceName) {
        authenticationSteps.authenticateUser(user);

        String message = new PayloadBuilder()
                .addField("type", "author.theme.create")
                .addField("name", themeName)
                .addField("workspaceId", interpolate(getWorkspaceIdVar(workspaceName)))
                .build();

        messageOperations.sendJSON(runner, message);
    }

    @Then("{string} is successfully created")
    public void themeIsSuccessfullyCreated(String themeName) {
        messageOperations.receiveJSON(runner, action ->
                action.payload("{" +
                                       "\"type\":\"author.theme.create.ok\"," +
                                       "\"response\":{" +
                                       "\"theme\":{" +
                                       "\"id\":\"@notEmpty()@\"," +
                                       "\"name\":\"@notEmpty()@\"" +
                                       "}" +
                                       "},\"replyTo\":\"@notEmpty()@\"}")
                        .extractFromPayload("$.response.theme.id", nameFrom(themeName, "id")));
    }

    @Then("the theme is not created due to missing permission level")
    public void theThemeIsNotCreatedDueToMissingPermissionLevel() {
        messageOperations.receiveJSON(runner, action ->
                action.payload("{" +
                                       "\"type\":\"author.theme.create.error\"," +
                                       "\"code\":401," +
                                       "\"message\":\"@notEmpty()@\"," +
                                       "\"replyTo\":\"@notEmpty()@\"}"));
    }

    @When("{string} updates theme {string}")
    public void updatesThemeWithConfig(String user, String themeName) {
        authenticationSteps.authenticateUser(user);

        String message = new PayloadBuilder()
                .addField("type", "author.theme.update")
                .addField("themeId", interpolate(nameFrom(themeName, "id")))
                .addField("name", themeName)
                .build();

        messageOperations.sendJSON(runner, message);
    }

    @Then("{string} is successfully updated")
    public void isSuccessfullyUpdated(String themeName) {
        messageOperations.receiveJSON(runner, action ->
                action.payload("{" +
                                       "\"type\":\"author.theme.update.ok\"," +
                                       "\"response\":{" +
                                       "\"theme\":{" +
                                       "\"id\":\"@notEmpty()@\"," +
                                       "\"name\":\"@notEmpty()@\"" +
                                       "}" +
                                       "},\"replyTo\":\"@notEmpty()@\"}")
                        .extractFromPayload("$.response.theme.id", nameFrom(themeName, "id")));
    }

    @When("{string} updates theme {string} with name {string}")
    public void updatesThemeWithNameAndConfig(String user, String themeName, String newThemeName) {
        authenticationSteps.authenticateUser(user);

        String message = new PayloadBuilder()
                .addField("type", "author.theme.update")
                .addField("themeId", interpolate(nameFrom(themeName, "id")))
                .addField("name", newThemeName)
                .build();

        messageOperations.sendJSON(runner, message);
    }

    @Then("the theme is not updated due to missing permission level")
    public void theThemeIsNotUpdatedDueToMissingPermissionLevel() {
        messageOperations.receiveJSON(runner, action ->
                action.payload("{" +
                                       "\"type\":\"author.theme.update.error\"," +
                                       "\"code\":401," +
                                       "\"message\":\"@notEmpty()@\"," +
                                       "\"replyTo\":\"@notEmpty()@\"}"));
    }

    @When("{string} lists the themes")
    public void listsTheThemes(String user) {
        authenticationSteps.authenticateUser(user);

        messageOperations.sendJSON(runner, new PayloadBuilder()
                .addField("type", "author.theme.list").build());
    }

    @When("{string} deletes theme {string}")
    public void deletesTheme(String user, String themeName) {
        authenticationSteps.authenticateUser(user);

        String message = new PayloadBuilder()
                .addField("type", "author.theme.delete")
                .addField("themeId", interpolate(nameFrom(themeName, "id")))
                .build();

        messageOperations.sendJSON(runner, message);
    }

    @Then("{string} is successfully deleted")
    public void isSuccessfullyDeleted(String user) {
        messageOperations.receiveJSON(runner, action ->
                action.payload("{" +
                                       "\"type\":\"author.theme.delete.ok\"," +
                                       "\"replyTo\":\"@notEmpty()@\"}"));
    }

    @Then("delete fails for {string} due to missing permission level")
    public void deleteFailsForDueToMissingPermissionLevel(String user) {
        messageOperations.receiveJSON(runner, action ->
                action.payload("{" +
                                       "\"type\":\"author.theme.delete.error\"," +
                                       "\"code\":401," +
                                       "\"message\":\"@notEmpty()@\"," +
                                       "\"replyTo\":\"@notEmpty()@\"}"));
    }

    @Then("the theme has empty list")
    public void theThemeHasEmptyList() {
        messageOperations.receiveJSON(runner, action -> action.validationCallback(
                new ResponseMessageValidationCallback<ArrayList>(ArrayList.class) {
                    @Override
                    public void validate(ArrayList themes, Map<String, Object> headers, TestContext context) {
                        assertEquals(0, themes.size());
                        assertTrue(themes.isEmpty());
                    }

                    @Override
                    public String getRootElementName() {
                        return "themePayload";
                    }

                    @Override
                    public String getType() {
                        return "author.theme.list.ok";
                    }
                }
        ));
    }

    @When("{string} created theme {string} for workspace {string}")
    public void createdThemeForWorkspace(String user, String themeName, String workspaceName) {
        authenticationSteps.authenticateUser(user);

        String message = new PayloadBuilder()
                .addField("type", "author.theme.create")
                .addField("name", themeName)
                .addField("workspaceId", interpolate(getWorkspaceIdVar(workspaceName)))
                .build();

        messageOperations.sendJSON(runner, message);

        messageOperations.receiveJSON(runner, action ->
                action.payload("{" +
                                       "\"type\":\"author.theme.create.ok\"," +
                                       "\"response\":{" +
                                       "\"theme\":{" +
                                       "\"id\":\"@notEmpty()@\"," +
                                       "\"name\":\"@notEmpty()@\"" +
                                       "}" +
                                       "},\"replyTo\":\"@notEmpty()@\"}")
                        .extractFromPayload("$.response.theme.id", nameFrom(themeName, "id")));
    }

    @Then("the theme variant is not created due to missing permission level")
    public void theThemeVariantIsNotCreatedDueToMissingPermissionLevel() {
        messageOperations.receiveJSON(runner, action ->
                action.payload("{" +
                                       "\"type\":\"author.theme.variant.create.error\"," +
                                       "\"code\":401," +
                                       "\"message\":\"@notEmpty()@\"," +
                                       "\"replyTo\":\"@notEmpty()@\"}"));
    }

    @Then("{string} list following themes")
    public void listFollowingThemes(String user, final List<String> expectedThemePayload) {
        authenticationSteps.authenticateUser(user);

        messageOperations.sendJSON(runner, new PayloadBuilder()
                .addField("type", "author.theme.list").build());

        messageOperations.receiveJSON(runner, action -> action.validationCallback(
                new ResponseMessageValidationCallback<ArrayList>(ArrayList.class) {
                    @Override
                    public void validate(ArrayList themePayload, Map<String, Object> headers, TestContext context) {

                        assertEquals(expectedThemePayload.size(), themePayload.size());

                        Set<String> expectedThemes = expectedThemePayload.stream()
                                .map(themeName -> context.getVariable(nameFrom(themeName, "id")))
                                .collect(Collectors.toSet());

                        for (int i = 0; i < themePayload.size(); i++) {
                            final Map actualThemepayload = (Map) themePayload.get(i);

                            // check theme info
                            Assert.assertTrue(expectedThemes.contains(actualThemepayload.get("id")));
                            Assert.assertTrue(expectedThemePayload.contains(actualThemepayload.get("name")));
                            assertNotNull(actualThemepayload.get("permissionLevel"));

                            // check theme variant info
                            List themeVariant = ((ArrayList) actualThemepayload.get("themeVariants"));
                            assertNotNull(((LinkedHashMap) themeVariant.get(0)).get("variantName"));
                            assertNotNull(((LinkedHashMap) themeVariant.get(0)).get("variantId"));
                            assertNotNull(((LinkedHashMap) themeVariant.get(0)).get("themeId"));

                            // check icon library info
                            List iconLibraries = ((ArrayList) actualThemepayload.get("iconLibraries"));
                            if (iconLibraries != null && !iconLibraries.isEmpty()) {
                                assertNotNull(((LinkedHashMap) iconLibraries.get(0)).get("name"));
                                assertNotNull(((LinkedHashMap) iconLibraries.get(0)).get("status"));
                            }

                        }
                    }

                    @Override
                    public String getRootElementName() {
                        return "themePayload";
                    }

                    @Override
                    public String getType() {
                        return "author.theme.list.ok";
                    }
                }
        ));
    }

    @When("{string} deletes theme variant {string} for theme {string}")
    public void deletesThemeVariantForTheme(String user, String variantName, String themeName) {
        authenticationSteps.authenticateUser(user);

        String message = new PayloadBuilder()
                .addField("type", "author.theme.variant.delete")
                .addField("themeId", interpolate(nameFrom(themeName, "id")))
                .addField("variantId", interpolate(nameFrom(variantName, "id")))
                .build();

        messageOperations.sendJSON(runner, message);
    }

    @Then("theme variant deleted successfully")
    public void themeVariantDeletedSuccessfully() {
        messageOperations.receiveJSON(runner, action ->
                action.payload("{" +
                                       "\"type\":\"author.theme.variant.delete.ok\"," +
                                       "\"replyTo\":\"@notEmpty()@\"}"));
    }

    @Then("theme variant not deleted due to missing permission level")
    public void themeVariantNotDeletedDueToMissingPermissionLevel() {
        messageOperations.receiveJSON(runner, action ->
                action.payload("{" +
                                       "\"type\":\"author.theme.variant.delete.error\"," +
                                       "\"code\":401," +
                                       "\"message\":\"@notEmpty()@\"," +
                                       "\"replyTo\":\"@notEmpty()@\"}"));
    }

    @When("{string} creates {string} theme variant {string} for theme {string} with config")
    public void createsDefaultThemeVariantForThemeWithConfig(String user,
                                                             String state,
                                                             String variantName,
                                                             String themeName,
                                                             String config) {

        authenticationSteps.authenticateUser(user);

        String message = new PayloadBuilder()
                .addField("type", "author.theme.variant.create")
                .addField("themeId", interpolate(nameFrom(themeName, "id")))
                .addField("variantName", variantName)
                .addField("config", config)
                .addField("state", state)
                .build();

        messageOperations.sendJSON(runner, message);
    }

    @When("{string} creates theme variant {string} for theme {string} with config")
    public void createsThemeVariantForThemeWithConfig(String user,
                                                      String variantName,
                                                      String themeName,
                                                      String config) {
        authenticationSteps.authenticateUser(user);

        String message = new PayloadBuilder()
                .addField("type", "author.theme.variant.create")
                .addField("themeId", interpolate(nameFrom(themeName, "id")))
                .addField("variantName", variantName)
                .addField("config", config)
                .build();

        messageOperations.sendJSON(runner, message);
    }

    @Then("default theme variant {string} is created successfully")
    public void defaultThemeVariantIsCreatedSuccessfully(String variantName) {
        messageOperations.receiveJSON(runner, action ->
                action.payload("{" +
                                       "\"type\":\"author.theme.variant.create.ok\"," +
                                       "\"response\":{" +
                                       "\"themeVariant\":{" +
                                       "\"themeId\":\"@notEmpty()@\"," +
                                       "\"variantId\":\"@notEmpty()@\"," +
                                       "\"variantName\":\"@notEmpty()@\"," +
                                       "\"state\":\"@notEmpty()@\"," +
                                       "\"config\":\"@notEmpty()@\"" +
                                       "}" +
                                       "},\"replyTo\":\"@notEmpty()@\"}")
                        .extractFromPayload("$.response.themeVariant.variantId", nameFrom(variantName, "id")));
    }

    @Then("theme variant {string} is created successfully")
    public void themeVariantIsCreatedSuccessfully(String variantName) {
        messageOperations.receiveJSON(runner, action ->
                action.payload("{" +
                                       "\"type\":\"author.theme.variant.create.ok\"," +
                                       "\"response\":{" +
                                       "\"themeVariant\":{" +
                                       "\"themeId\":\"@notEmpty()@\"," +
                                       "\"variantId\":\"@notEmpty()@\"," +
                                       "\"variantName\":\"@notEmpty()@\"," +
                                       "\"config\":\"@notEmpty()@\"" +
                                       "}" +
                                       "},\"replyTo\":\"@notEmpty()@\"}")
                        .extractFromPayload("$.response.themeVariant.variantId", nameFrom(variantName, "id")));
    }

    @When("{string} updates variant {string} and theme {string} with variant name {string} and config")
    public void updatesVariantAndThemeWithVariantNameAndConfig(String user,
                                                               String variantId,
                                                               String themeName,
                                                               String variantName,
                                                               String config) {
        authenticationSteps.authenticateUser(user);

        String message = new PayloadBuilder()
                .addField("type", "author.theme.variant.update")
                .addField("themeId", interpolate(nameFrom(themeName, "id")))
                .addField("variantId", interpolate(nameFrom(variantId, "id")))
                .addField("variantName", variantName)
                .addField("config", config)
                .build();

        messageOperations.sendJSON(runner, message);

    }

    @Then("default theme variant {string} updated successfully")
    public void defaultThemeVariantUpdatedSuccessfully(String variantId) {
        messageOperations.receiveJSON(runner, action ->
                action.payload("{" +
                                       "\"type\":\"author.theme.variant.update.ok\"," +
                                       "\"response\":{" +
                                       "\"themeVariant\":{" +
                                       "\"themeId\":\"@notEmpty()@\"," +
                                       "\"variantId\":\"@notEmpty()@\"," +
                                       "\"variantName\":\"@notEmpty()@\"," +
                                       "\"state\":\"@notEmpty()@\"," +
                                       "\"config\":\"@notEmpty()@\"" +
                                       "}" +
                                       "},\"replyTo\":\"@notEmpty()@\"}")
                        .extractFromPayload("$.response.themeVariant.variantId", nameFrom(variantId, "id")));
    }

    @Then("theme variant {string} updated successfully")
    public void themeVariantUpdatedSuccessfully(String variantId) {
        messageOperations.receiveJSON(runner, action ->
                action.payload("{" +
                                       "\"type\":\"author.theme.variant.update.ok\"," +
                                       "\"response\":{" +
                                       "\"themeVariant\":{" +
                                       "\"themeId\":\"@notEmpty()@\"," +
                                       "\"variantId\":\"@notEmpty()@\"," +
                                       "\"variantName\":\"@notEmpty()@\"," +
                                       "\"config\":\"@notEmpty()@\"" +
                                       "}" +
                                       "},\"replyTo\":\"@notEmpty()@\"}")
                        .extractFromPayload("$.response.themeVariant.variantId", nameFrom(variantId, "id")));
    }

    @Then("theme variant is not updated due to missing permission level")
    public void theThemeVariantIsNotUpdatedDueToMissingPermissionLevel() {
        messageOperations.receiveJSON(runner, action ->
                action.payload("{" +
                                       "\"type\":\"author.theme.variant.update.error\"," +
                                       "\"code\":401," +
                                       "\"message\":\"@notEmpty()@\"," +
                                       "\"replyTo\":\"@notEmpty()@\"}"));
    }

    @Then("{string} get following theme variant from {string} and theme {string}")
    public void getFollowingThemeVariantFromAndTheme(String user,
                                                     String variantName,
                                                     String themeName,
                                                     Map<String, String> expectedvariantName) {
        authenticationSteps.authenticateUser(user);

        String message = new PayloadBuilder()
                .addField("type", "workspace.theme.variant.get")
                .addField("themeId", interpolate(nameFrom(themeName, "id")))
                .addField("variantId", interpolate(nameFrom(variantName, "id")))
                .build();

        messageOperations.sendJSON(runner, message);

        messageOperations.receiveJSON(runner, action -> action.validationCallback(
                new ResponseMessageValidationCallback<LinkedHashMap>(LinkedHashMap.class) {
                    @Override
                    public void validate(final LinkedHashMap actualThemeVariant,
                                         final Map<String, Object> headers,
                                         final TestContext context) {
                        assertNotNull(actualThemeVariant);
                        String variantName = (String) actualThemeVariant.get("variantName");
                        assertNotNull(variantName);
                        assertEquals(expectedvariantName.get("variantName"), variantName);
                        assertNotNull((String) actualThemeVariant.get("config"));
                    }

                    @Override
                    public String getRootElementName() {
                        return "themeVariant";
                    }

                    @Override
                    public String getType() {
                        return "workspace.theme.variant.get.ok";
                    }
                }));
    }

    @Then("{string} get empty theme variant from {string} and theme {string}")
    public void getEmptyThemeVariantFromAndTheme(String user, String variantName, String themeName) {
        authenticationSteps.authenticateUser(user);

        String message = new PayloadBuilder()
                .addField("type", "workspace.theme.variant.get")
                .addField("themeId", interpolate(nameFrom(themeName, "id")))
                .addField("variantId", interpolate(nameFrom(variantName, "id")))
                .build();

        messageOperations.sendJSON(runner, message);

        messageOperations.receiveJSON(runner, action -> action.validationCallback(
                new ResponseMessageValidationCallback<LinkedHashMap>(LinkedHashMap.class) {
                    @Override
                    public void validate(final LinkedHashMap actualThemeVariant,
                                         final Map<String, Object> headers,
                                         final TestContext context) {
                        assertNotNull(actualThemeVariant);
                        assertNull(actualThemeVariant.get("variantName"));
                        assertNull(actualThemeVariant.get("config"));
                    }

                    @Override
                    public String getRootElementName() {
                        return "themeVariant";
                    }

                    @Override
                    public String getType() {
                        return "workspace.theme.variant.get.ok";
                    }
                }));
    }

    @When("{string} associate theme {string} with icon libraries")
    public void associateThemeWithIconLibraries(String user, String themeName, List<IconLibraryInfo> iconLibraries) {
        authenticationSteps.authenticateUser(user);

        List<IconLibrary> iconLibraryList = iconLibraries.stream()
                .map(iconLibraryInfo -> {
                    IconLibrary iconLibrary = new IconLibrary()
                            .setName(iconLibraryInfo.name);
                    if (iconLibraryInfo.status != null && iconLibraryInfo.status.equals("SELECTED")) {
                        iconLibrary.setStatus(Enum.valueOf(IconLibraryState.class, iconLibraryInfo.status));
                    }
                    return iconLibrary;
                }).collect(Collectors.toList());

        String message = new PayloadBuilder()
                .addField("type", "author.theme.icon.library.associate")
                .addField("themeId", interpolate(nameFrom(themeName, "id")))
                .addField("iconLibraries", iconLibraryList)
                .build();

        messageOperations.sendJSON(runner, message);
    }

    @SuppressFBWarnings(value = "UWF_UNWRITTEN_FIELD", justification = "The citrus writes to this fields with values from feature files.")
    public static class IconLibraryInfo {
        public String name;
        public String status;
    }

    @Then("the association is created successfully")
    public void theAssociationIsCreatedSuccessfully() {
        messageOperations.receiveJSON(runner, action -> action.validationCallback(
                new ResponseMessageValidationCallback<ArrayList>(ArrayList.class) {
                    @Override
                    public void validate(final ArrayList iconLibrariesByTheme,
                                         final Map<String, Object> headers,
                                         final TestContext context) {
                        assertNotNull(iconLibrariesByTheme);

                        for (int i = 0; i < iconLibrariesByTheme.size(); i++) {
                            final Map iconLibraryByTheme = (Map) iconLibrariesByTheme.get(i);
                            assertNotNull(iconLibraryByTheme);
                            assertNotNull(iconLibraryByTheme.get("themeId"));
                            assertNotNull(iconLibraryByTheme.get("iconLibrary"));
                            assertNotNull(iconLibraryByTheme.get("status"));
                        }
                    }

                    @Override
                    public String getRootElementName() {
                        return "iconLibrariesByTheme";
                    }

                    @Override
                    public String getType() {
                        return "author.theme.icon.library.associate.ok";
                    }
                }));
    }

    @When("{string} associate activity {string} with icon libraries")
    public void associateActivityWithIconLibraries(String user,
                                                   String elementName,
                                                   List<IconLibraryInfo> iconLibraries) {
        authenticationSteps.authenticateUser(user);

        List<IconLibrary> iconLibraryList = iconLibraries.stream()
                .map(iconLibraryInfo -> {
                    IconLibrary iconLibrary = new IconLibrary()
                            .setName(iconLibraryInfo.name);
                    if (iconLibraryInfo.status != null && iconLibraryInfo.status.equals("SELECTED")) {
                        iconLibrary.setStatus(Enum.valueOf(IconLibraryState.class, iconLibraryInfo.status));
                    }
                    return iconLibrary;
                }).collect(Collectors.toList());

        String message = new PayloadBuilder()
                .addField("type", "author.activity.icon.library.associate")
                .addField("activityId", interpolate(nameFrom(elementName, "id")))
                .addField("iconLibraries", iconLibraryList)
                .build();

        messageOperations.sendJSON(runner, message);
    }

    @Then("icon libraries associated successfully with activity")
    public void iconLibrariesAssociatedSuccessfullyWithActivity() {
        messageOperations.receiveJSON(runner, action -> action.validationCallback(
                new ResponseMessageValidationCallback<ArrayList>(ArrayList.class) {
                    @Override
                    public void validate(final ArrayList iconLibrariesByActivityTheme,
                                         final Map<String, Object> headers,
                                         final TestContext context) {
                        assertNotNull(iconLibrariesByActivityTheme);

                        for (int i = 0; i < iconLibrariesByActivityTheme.size(); i++) {
                            final Map iconLibraryByActivityTheme = (Map) iconLibrariesByActivityTheme.get(i);
                            assertNotNull(iconLibraryByActivityTheme);
                            assertNotNull(iconLibraryByActivityTheme.get("activityId"));
                            assertNotNull(iconLibraryByActivityTheme.get("iconLibrary"));
                            assertNotNull(iconLibraryByActivityTheme.get("status"));
                        }
                    }

                    @Override
                    public String getRootElementName() {
                        return "activityThemeIconLibraries";
                    }

                    @Override
                    public String getType() {
                        return "author.activity.icon.library.associate.ok";
                    }
                }));
    }

}
