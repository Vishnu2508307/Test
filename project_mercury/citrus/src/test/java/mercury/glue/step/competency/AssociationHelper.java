package mercury.glue.step.competency;

import static mercury.common.Variables.interpolate;

import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;

public class AssociationHelper {

    public static String competencyItemAssociationCreateMutation(String documentId, Map<String, String> fields, List<String> selectedFields) {

        String originItemId = fields.get("originItemId");
        String destinationItemId = fields.get("destinationItemId");
        String associationType = fields.get("associationType");

        String input = String.format("{" +
                "originItemId: \"%s\"," +
                "destinationItemId: \"%s\"," +
                "associationType: %s," +
                "documentId: \"%s\"}", interpolate(originItemId, "id"), interpolate(destinationItemId, "id"), associationType, documentId);

        return "mutation {" +
                "competencyItemAssociationCreate(input: " + input + ") {" +
                "association {" +
                StringUtils.join(selectedFields, " ") +
                "}" +
                "}" +
                "}";
    }

    public static String competencyItemAssociationDeleteMutation(String assocId, String documentId) {
        return "mutation {" +
                "competencyItemAssociationDelete(input: {documentId: \""+documentId+"\", associationId: \""+assocId+"\"}) {" +
                "documentId " +
                "associationId " +
                "}" +
                "}";
    }
}
