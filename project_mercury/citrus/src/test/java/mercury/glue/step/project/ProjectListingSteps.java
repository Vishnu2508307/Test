package mercury.glue.step.project;

import static mercury.common.Variables.interpolate;
import static mercury.common.Variables.nameFrom;
import static mercury.glue.wiring.CitrusConfiguration.MESSAGE_OPERATIONS;
import static mercury.helpers.workspace.WorkspaceHelper.getWorkspaceIdVar;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

import com.consol.citrus.annotations.CitrusResource;
import com.consol.citrus.context.TestContext;
import com.consol.citrus.dsl.runner.TestRunner;

import cucumber.api.java.en.Then;
import cucumber.api.java.en.When;
import mercury.common.MessageOperations;
import mercury.common.PayloadBuilder;
import mercury.common.ResponseMessageValidationCallback;
import mercury.glue.step.AuthenticationSteps;

public class ProjectListingSteps {

    @CitrusResource
    private TestRunner runner;

    @Autowired
    @Qualifier(MESSAGE_OPERATIONS)
    private MessageOperations messageOperations;

    @Autowired
    private AuthenticationSteps authenticationSteps;

    @Autowired
    private ProjectCreateSteps projectCreateSteps;

    @When("{string} lists all the projects inside workspace {string}")
    public void listsAllTheProjectsInsideWorkspace(String accountName, String workspaceName) {
        authenticationSteps.authenticateUser(accountName);

        messageOperations.sendJSON(runner, new PayloadBuilder()
                .addField("type", "workspace.project.list")
                .addField("workspaceId", interpolate(getWorkspaceIdVar(workspaceName)))
                .build());
    }

    @Then("the following projects are listed for workspace {string}")
    public void theFollowingProjectsAreListedForWorkspace(final String workspaceName, final List<String> expectedProjectNames) {
        messageOperations.receiveJSON(runner, action -> action.validationCallback(
                new ResponseMessageValidationCallback<ArrayList>(ArrayList.class) {
                    @Override
                    public void validate(final ArrayList projects, final Map<String, Object> headers, final TestContext context) {
                        assertEquals(expectedProjectNames.size(), projects.size());
                        Set<String> actualProjects = new HashSet<>(projects.size());

                        for (Object project : projects) {
                            actualProjects.add((String) ((Map) project).get("id"));
                        }

                        Set<String> expectedProjects = expectedProjectNames.stream()
                                .map(projectName -> context.getVariable(nameFrom(projectName, "id")))
                                .collect(Collectors.toSet());

                        assertEquals(expectedProjects, actualProjects);
                    }

                    @Override
                    public String getRootElementName() {
                        return "projects";
                    }

                    @Override
                    public String getType() {
                        return "workspace.project.list.ok";
                    }
                }));
    }

    @Then("{string} can list the following projects from workspace {string}")
    public void canListTheFollowingProjectsFromWorkspace(final String accountName, final String workspaceName,
                                                         final List<String> expectedProjectNames) {
        listsAllTheProjectsInsideWorkspace(accountName, workspaceName);
        theFollowingProjectsAreListedForWorkspace(workspaceName, expectedProjectNames);
    }

    @Then("{string} projects list for workspace {string} is empty")
    public void projectsListForWorkspaceIsEmpty(String accountName, String workspaceName) {
        listsAllTheProjectsInsideWorkspace(accountName, workspaceName);

        messageOperations.receiveJSON(runner, action -> action
                .jsonPath("$.type", "workspace.project.list.ok")
                .jsonPath("$.response.projects", new ArrayList<>())
        );
    }

    @Then("projects are listed for workspace with permission {string}")
    public void projectsAreListedForWorkspaceWithPermission(final String workspaceName, final Map<String, String> dataTable) {
        messageOperations.receiveJSON(runner, action -> action.validationCallback(
                new ResponseMessageValidationCallback<ArrayList>(ArrayList.class) {
                    @Override
                    public void validate(final ArrayList projects, final Map<String, Object> headers, final TestContext context) {
                        assertEquals(dataTable.size(), projects.size());
                        Set<Map> actualProjects = new HashSet<>();

                        for (Object project : projects) {
                            Map<String, String> actual = new HashMap<>();
                            actual.put("id", (String) ((Map) project).get("id"));
                            actual.put("permission", (String) ((Map) project).get("permissionLevel"));
                            actualProjects.add(actual);
                        }

                        Set<Map> expectedProjects = new HashSet<>();
                        for (Map.Entry<String, String> user : dataTable.entrySet()) {
                            Map<String, String> expected = new HashMap<>();
                            expected.put("id", context.getVariable(nameFrom(user.getKey(), "id")));
                            expected.put("permission", user.getValue());
                            expectedProjects.add(expected);
                        }

                        assertEquals(expectedProjects, actualProjects);
                    }

                    @Override
                    public String getRootElementName() {
                        return "projects";
                    }

                    @Override
                    public String getType() {
                        return "workspace.project.list.ok";
                    }
                }));
    }
}
