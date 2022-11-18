package mercury.helpers.workspace;

import java.util.List;
import java.util.Map;

import com.consol.citrus.exceptions.CitrusRuntimeException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import mercury.common.PayloadBuilder;

public class WorkspaceHelper {

    /**
     * Create a json message for creating a workspace
     *
     * @param name        the workspace name. This will also be used to extract fields into the citrus context
     * @param description the workspace description
     * @return a json string representation of the message request
     */
    public static String createWorkspaceRequest(String name, String description) {
        return new PayloadBuilder()
                .addField("type", "workspace.create")
                .addField("name", name)
                .addField("description", description).build();
    }

    /**
     * Create a json message for updating a workspace
     *
     * @param workspaceId the id of the workspace to update
     * @param fields      the fields to update
     * @return a json string representation of the message request
     */
    public static String updateWorkspaceRequest(String workspaceId, Map<String, String> fields) {
        PayloadBuilder builder = new PayloadBuilder()
                .addField("type", "workspace.change")
                .addField("workspaceId", workspaceId);

        fields.forEach(builder::addField);

        return builder.build();
    }

    /**
     * Create a json message for deleting a workspace
     *
     * @param workspaceId  the id of the workspace to delete
     * @param name         the workspace name
     * @param user         the user account attempting to delete the workspace
     * @param subscription the subscription level of the user account
     * @return a json string representation of the message request
     */
    public static String deleteWorkspaceRequest(String workspaceId, String name, String user, String subscription) {
        return new PayloadBuilder()
                .addField("type", "workspace.delete")
                .addField("workspaceId", workspaceId)
                .addField("name", name)
                .addField("subscriptionId", subscription).build();
    }

    /**
     * Create a json message for grant workspace permission
     *
     * @param workspaceId     the workspace to share
     * @param collaboratorType the type of collaborator. Either `team` or `account`
     * @param ids the list of collaboratorIds
     * @param permissionLevel the permission level the account will have on the workspace
     * @return a json string representation of the message request
     */
    public static String grantWorkspacePermissionRequest(String workspaceId, String collaboratorType, List<String> ids,
                                                         String permissionLevel) {
        return new PayloadBuilder()
                .addField("type", "workspace.permission.grant")
                .addField("workspaceId", workspaceId)
                .addField(collaboratorType + "Ids", ids)
                .addField("permissionLevel", permissionLevel)
                .build();
    }

    /**
     * Create a json message for revoking a workspace permission
     *
     * @param workspaceId the workspace the permission refers to
     * @param accountId   the account holding the permission
     * @return a json string representation of the message request
     */
    public static String revokeWorkspacePermissionRequest(String workspaceId, String accountId) {
        return new PayloadBuilder()
                .addField("type", "workspace.permission.revoke")
                .addField("workspaceId", workspaceId)
                .addField("accountId", accountId)
                .build();
    }

    /**
     * Verify the payload for a workspace successful response
     *
     * @param name the workspace name
     * @return a json string representing the message response
     */
    public static String createWorkspaceResponse(String name) {
        return "{" +
                "\"type\":\"workspace.create.ok\"," +
                "\"response\":{" +
                "\"workspace\":{" +
                "\"id\":\"@notEmpty()@\"," +
                "\"subscriptionId\":\"@notEmpty()@\"," +
                "\"name\":\"" + name + "\"," +
                "\"description\":\"@notEmpty()@\"" +
                "}" +
                "},\"replyTo\":\"@notEmpty()@\"}";
    }

    /**
     * Verify a workspace grant permission message response
     *
     * @param collaboratorIds a list of collaborator ids
     * @param collaboratorType either a `team` or `account` collaborator
     * @param workspaceId the workspace the permission was granted for
     * @param permission  the granted permission level
     * @return a json string representation of the message response
     */
    public static String grantWorkspacePermissionResponse(List<String> collaboratorIds, String collaboratorType,
                                                          String workspaceId, String permission) {
        ObjectMapper om = new ObjectMapper();
        try {
            return "{" +
                    "\"type\":\"workspace.permission.grant.ok\"," +
                    "\"response\":{" +
                    "\""+collaboratorType+"Ids\":" + om.writeValueAsString(collaboratorIds)  + "," +
                    "\"permissionLevel\":\"" + permission + "\"," +
                    "\"workspaceId\":\"" + workspaceId + "\"" +
                    "},\"replyTo\":\"@notEmpty()@\"}";
        } catch (JsonProcessingException e) {
            throw new CitrusRuntimeException(e.getMessage());
        }
    }

    /**
     * Verify a workspace update successful message response
     *
     * @param workspaceId the workspace id
     * @param name        the workspace name
     * @param description the workspace description
     * @return a json string representation of the message response
     */
    public static String updateWorkspaceResponse(String workspaceId, String name, String description) {
        return "{" +
                "\"type\":\"workspace.change.ok\"," +
                "\"response\":{" +
                "\"workspace\":{" +
                "\"id\":\"" + workspaceId + "\"," +
                "\"subscriptionId\":\"@notEmpty()@\"," +
                "\"name\":\"" + name + "\"," +
                "\"description\":\"" + description + "\"" +
                "}" +
                "},\"replyTo\":\"@notEmpty()@\"}";
    }

    /**
     * Create a json message for creating a workspace
     *
     * @param workspaceId the workspace id.
     * @param limit       the limit, can be null
     * @return a json string representation of the message request
     */
    public static String summaryWorkspaceAccountsRequest(String workspaceId, Integer limit) {
        PayloadBuilder pb = new PayloadBuilder()
                .addField("type", "workspace.collaborator.summary")
                .addField("workspaceId", workspaceId);
        if (limit != null) {
            pb.addField("limit", limit);
        }
        return pb.build();

    }

    public static String getWorkspaceIdVar(String workspaceName) {
        return workspaceName + "_workspace_id";
    }

    public static String revokeCollaboratorTeamPermission(String workspaceId, String teamId) {
        return new PayloadBuilder()
                .addField("type", "workspace.permission.revoke")
                .addField("workspaceId", workspaceId)
                .addField("teamId", teamId)
                .build();
    }
}
