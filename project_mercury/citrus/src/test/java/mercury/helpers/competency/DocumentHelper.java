package mercury.helpers.competency;

import static mercury.common.Variables.interpolate;
import static mercury.common.Variables.nameFrom;
import static mercury.helpers.common.GenericMessageHelper.convertToBasicResponseMessage;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.annotation.Nonnull;

import org.apache.commons.lang3.StringUtils;

import com.consol.citrus.validation.callback.ValidationCallback;
import com.smartsparrow.rtm.message.send.BasicResponseMessage;

public class DocumentHelper {

    public static String fetchDocumentInWorkspaceQuery(String workspaceId) {
        return "{\n" +
                "  workspace(workspaceId: \""+workspaceId+"\") {\n" +
                "    competencyDocuments {\n" +
                "      edges {\n" +
                "        node {\n" +
                "          id\n" +
                "          title\n" +
                "          items {\n" +
                "            edges {\n" +
                "              node {\n" +
                "                id\n" +
                "                fullStatement\n" +
                "              }\n" +
                "            }\n" +
                "          }\n" +
                "        }\n" +
                "      }\n" +
                "    }\n" +
                "  }\n" +
                "}";
    }

    public static String fetchDocumentQuery() {
        return "{\n" +
                "  documents {\n" +
                "    edges {\n" +
                "      node {\n" +
                "        id\n" +
                "        title\n" +
                "        items {\n" +
                "          edges {\n" +
                "            node {\n" +
                "              id\n" +
                "              fullStatement\n" +
                "            }\n" +
                "          }\n" +
                "        }\n" +
                "      }\n" +
                "    }\n" +
                "  }\n" +
                "}";
    }

    @SuppressWarnings("unchecked")
    public static ValidationCallback fetchDocumentInWorkspaceValidationCallback(Map<String, String> fields, Map<String, String> documentName) {
        return (message, context) -> {
            try {
                BasicResponseMessage responseMessage = convertToBasicResponseMessage(message);
                Map<String, Object> response = responseMessage.getResponse();
                Map<String, Object> data = (Map<String, Object>) response.get("data");
                Map<String, Object> workspace = (Map<String, Object>) data.get("workspace");
                Map<String, Object> competencyDocuments = (Map<String, Object>) workspace.get("competencyDocuments");
                List<Object> edges = (List<Object>) competencyDocuments.get("edges");

                edges.forEach(edge -> {
                    Map<String, Object> node = (Map<String, Object>) edge;
                    Map<String, Object> document = (Map<String, Object>) node.get("node");

                    final String documentId = document.get("id").toString();
                    final String documentTitle = document.get("title").toString();

                    assertEquals(documentId, context.getVariable(interpolate(nameFrom(documentName.get(documentTitle), "id"))));

                    final Map<String, Object> items = (Map<String, Object>) document.get("items");
                    List<Object> itemEdges = (List<Object>) items.get("edges");

                    itemEdges.forEach(itemEdge -> {
                        Map<String, Object> documentItemNode = (Map<String, Object>) itemEdge;
                        Map<String, Object> documentItem = (Map<String, Object>) documentItemNode.get("node");

                        final String documentItemId = (String) documentItem.get("id");
                        List<String> expectedDocumentItemIds = Arrays.stream(fields.get(documentTitle).split(","))
                                .map(itemName -> context.getVariable(interpolate(nameFrom(itemName, "id"))))
                                .collect(Collectors.toList());

                        assertTrue(expectedDocumentItemIds.contains(documentItemId));

                    });
                });

            } catch (IOException e) {
                fail(e.getMessage());
            }
        };
    }

    @SuppressWarnings("unchecked")
    public static ValidationCallback fetchDocumentValidationCallback(Map<String, String> fields, Map<String, String> documentName) {
        return (message, context) -> {
            try {
                BasicResponseMessage responseMessage = convertToBasicResponseMessage(message);
                Map<String, Object> response = responseMessage.getResponse();
                Map<String, Object> data = (Map<String, Object>) response.get("data");
                Map<String, Object> competencyDocuments = (Map<String, Object>) data.get("documents");
                List<Object> edges = (List<Object>) competencyDocuments.get("edges");

                edges.forEach(edge -> {
                    Map<String, Object> node = (Map<String, Object>) edge;
                    Map<String, Object> document = (Map<String, Object>) node.get("node");

                    final String documentId = document.get("id").toString();
                    final String documentTitle = document.get("title").toString();

                    assertEquals(documentId, context.getVariable(interpolate(nameFrom(documentName.get(documentTitle), "id"))));

                    final Map<String, Object> items = (Map<String, Object>) document.get("items");
                    List<Object> itemEdges = (List<Object>) items.get("edges");

                    itemEdges.forEach(itemEdge -> {
                        Map<String, Object> documentItemNode = (Map<String, Object>) itemEdge;
                        Map<String, Object> documentItem = (Map<String, Object>) documentItemNode.get("node");

                        final String documentItemId = (String) documentItem.get("id");
                        List<String> expectedDocumentItemIds = Arrays.stream(fields.get(documentTitle).split(","))
                                .map(itemName -> context.getVariable(interpolate(nameFrom(itemName, "id"))))
                                .collect(Collectors.toList());

                        assertTrue(expectedDocumentItemIds.contains(documentItemId));

                    });
                });

            } catch (IOException e) {
                fail(e.getMessage());
            }
        };
    }

    public static String fetchCompetencyMet(@Nonnull final String studentId, @Nonnull final String documentId) {
        return String.format("{" +
                " learn {" +
                "   competencyDocumentMet(documentId: \"%s\") {" +
                "     edges {" +
                "       node {" +
                "         documentItemId " +
                "         value " +
                "         confidence " +
                "       }" +
                "     }" +
                "   }" +
                " }" +
               "}", documentId);
    }

    public static String competencyDocumentDeleteMutation(String workspaceId, String documentId) {
    return "mutation {" +
            "competencyDocumentDelete(input: {workspaceId: \"" + workspaceId + "\", documentId: \"" + documentId + "\"}) {" +
                "document {" +
                        "workspaceId " +
                        "documentId " +
                    "}" +
                "}" +
            "}";
    }

    public static String competencyDocumentUpdateMutation(String workspaceId, String documentId,
                                                          Map<String, String> fields, List<String> selectedFields) {

        String documentTitle = fields.get("title");

        String input = String.format("{workspaceId: \"%s\", documentId: \"%s\", title: \"%s\"}",
                                    workspaceId,
                                    documentId,
                                    documentTitle);

        return "mutation {" +
                "competencyDocumentUpdate(input:" + input +") {" +
                        "document {" +
                            "workspaceId " +
                            "documentId " +
                             StringUtils.join(selectedFields, " ") +
                        "}" +
                    "}" +
                "}";
    }
}
