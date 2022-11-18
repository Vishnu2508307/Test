package mercury.helpers.subscription;

import java.util.List;

import com.consol.citrus.exceptions.CitrusRuntimeException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import mercury.common.PayloadBuilder;

public class SubscriptionHelper {

    /**
     * Create a subscription grant message request
     *
     * @param subscriptionId the subscription to share
     * @param collaboratorType the type of collaborator. Either `team` or `account`
     * @param ids the list of collaboratorIds
     * @param permissionLevel the permission the account will have over the subscription
     * @return a json string representation of the message response
     */
    public static String createSubscriptionPermissionGrantRequest(String subscriptionId, String collaboratorType,
                                                                  List<String> ids, String permissionLevel) {
        return new PayloadBuilder()
                .addField("type", "iam.subscription.permission.grant")
                .addField("subscriptionId", subscriptionId)
                .addField(collaboratorType + "Ids", ids)
                .addField("permissionLevel", permissionLevel).build();
    }

    /**
     * Verify a subscription grant permission message response
     *
     * @param collaboratorIds a list of collaborator ids
     * @param collaboratorType either a `team` or `account` collaborator
     * @param subscriptionId the subscription the permission was granted for
     * @param permission  the granted permission level
     * @return a json string representation of the message response
     */
    public static String grantSubscriptionPermissionResponse(List<String> collaboratorIds, String collaboratorType,
                                                          String subscriptionId, String permission) {
        ObjectMapper om = new ObjectMapper();
        try {
            return "{" +
                    "\"type\":\"iam.subscription.permission.grant.ok\"," +
                    "\"response\":{" +
                    "\""+collaboratorType+"Ids\":" + om.writeValueAsString(collaboratorIds)  + "," +
                    "\"permissionLevel\":\"" + permission + "\"," +
                    "\"subscriptionId\":\"" + subscriptionId + "\"" +
                    "},\"replyTo\":\"@notEmpty()@\"}";
        } catch (JsonProcessingException e) {
            throw new CitrusRuntimeException(e.getMessage());
        }
    }
}
