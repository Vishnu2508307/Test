package mercury.helpers.project;

import mercury.common.PayloadBuilder;

public class ProjectHelper {

    /**
     * Create a json message for creating a workspace
     *
     * @param projectId the project id.
     * @param limit     the limit, can be null
     * @return a json string representation of the message request
     */
    public static String summaryProjectAccountsRequest(String projectId, Integer limit) {
        PayloadBuilder pb = new PayloadBuilder()
                .addField("type", "workspace.project.collaborator.summary")
                .addField("projectId", projectId);
        if (limit != null) {
            pb.addField("limit", limit);
        }
        return pb.build();

    }
}