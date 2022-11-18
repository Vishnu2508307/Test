package mercury.helpers.competency;

import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;

public class DocumentItemHelper {

    public static String competencyDocumentCreateMutation(String workspaceId, String title, List<String> selectedFields) {
        return "mutation {" +
                 "competencyDocumentCreate(input: {workspaceId: \""+workspaceId+"\", title: \""+title+"\"}) {" +
                   "document {" +
                     StringUtils.join(selectedFields, " ") +
                   "}" +
                 "}" +
               "}";
    }

    public static String competencyDocumentItemCreateMutation(String documentId, Map<String, String> fields, List<String> selectedFields) {

        String fullStatement = fields.get("fullStatement");
        String abbreviatedStatement = fields.get("abbreviatedStatement");
        String humanCodingScheme = fields.get("humanCodingScheme");

        String input = String.format("{" +
                        "fullStatement: \"%s\"," +
                        "abbreviatedStatement: \"%s\"," +
                        "humanCodingScheme: \"%s\"," +
                        "documentId: \"%s\"}", fullStatement, abbreviatedStatement, humanCodingScheme, documentId);

        return "mutation {" +
                  "competencyDocumentItemCreate(input: " + input + ") {" +
                    "documentItem {" +
                      StringUtils.join(selectedFields, " ") +
                    "}" +
                  "}" +
               "}";
    }

    public static String competencyDocumentItemUpdateMutation(String itemId, String documentId,
                                                              Map<String, String> fields, List<String> selectedFields) {
        String fullStatement = fields.get("fullStatement");
        String abbreviatedStatement = fields.get("abbreviatedStatement");
        String humanCodingScheme = fields.get("humanCodingScheme");

        String input = String.format("{" +
                "fullStatement: \"%s\"," +
                "abbreviatedStatement: \"%s\"," +
                "humanCodingScheme: \"%s\"," +
                "id: \"%s\"," +
                "documentId: \"%s\"}", fullStatement, abbreviatedStatement, humanCodingScheme, itemId, documentId);

        return "mutation {" +
                 "CompetencyDocumentItemUpdate(input: " + input + ") {" +
                   "documentItem {" +
                     StringUtils.join(selectedFields, " ") +
                  "}" +
                "}" +
              "}";
    }

    public static String competencyDocumentItemDeleteMutation(String itemId, String documentId) {
        return "mutation {" +
                  "competencyDocumentItemDelete(input: {documentId: \""+documentId+"\", id: \""+itemId+"\"}) {" +
                    "documentItem {" +
                      "documentId " +
                      "id " +
                   "}" +
                 "}" +
               "}";
    }

    public static String learnerDocumentItemLink(final String cohortId, final String deploymentId) {
        return "    {\n" +
               "      learn {\n" +
               "        cohort(cohortId: \""+cohortId+"\") {\n" +
               "          deployment(deploymentId: \""+deploymentId+"\") {\n" +
               "            activity {\n" +
               "              pathways {\n" +
               "                id\n" +
               "                walkables {\n" +
               "                  edges {\n" +
               "                    node {\n" +
               "                      id\n" +
               "                      elementType\n" +
               "                      config\n" +
               "                      linkedDocumentItems {\n" +
               "                        edges {\n" +
               "                          node {\n" +
               "                            id\n" +
               "                          }" +
               "                        }" +
               "                      }\n" +
               "                    }\n" +
               "                  }\n" +
               "                }\n" +
               "              }\n" +
               "            }\n" +
               "          }\n" +
               "        }\n" +
               "      }\n" +
               "    }";
    }

    public static String learnerDocumentItemsQuery(final String documentId) {
        return "{\n" +
                "  learn {\n" +
                "    learnerDocument(documentId: \""+documentId+"\") {\n" +
                "      createdAt\n" +
                "      createdBy\n" +
                "      documentId\n" +
                "      title\n" +
                "      modifiedAt\n" +
                "      modifiedBy\n" +
                "      documentItems {\n" +
                "        edges {\n" +
                "          node {\n" +
                "            id\n" +
                "            humanCodingScheme\n" +
                "            createdAt\n" +
                "            modifiedAt\n" +
                "          }\n" +
                "        }\n" +
                "      }\n" +
                "    }\n" +
                "  }\n" +
                "}";
    }

    public static String learnerDocumentItemAssociationsQuery(final String documentId) {
        return "{\n" +
                "  learn {\n" +
                "    learnerDocument(documentId: \""+documentId+"\") {\n" +
                "      createdAt\n" +
                "      createdBy\n" +
                "      documentId\n" +
                "      title\n" +
                "      modifiedAt\n" +
                "      modifiedBy\n" +
                "      documentItems {\n" +
                "        edges {\n" +
                "          node {\n" +
                "            id\n" +
                "            humanCodingScheme\n" +
                "            createdAt\n" +
                "            modifiedAt\n" +
                "            associationsFrom {\n" +
                "              edges {\n" +
                "                node {\n" +
                "                  associationId\n" +
                "                  associationType\n" +
                "                  createdAt\n" +
                "                  createdBy\n" +
                "                  destinationItemId\n" +
                "                  documentId\n" +
                "                  originItemId\n" +
                "                }\n" +
                "              }\n" +
                "            }\n" +
                "            associationsTo {\n" +
                "              edges {\n" +
                "                node {\n" +
                "                  associationId\n" +
                "                  associationType\n" +
                "                  createdAt\n" +
                "                  createdBy\n" +
                "                  destinationItemId\n" +
                "                  documentId\n" +
                "                  originItemId\n" +
                "                }\n" +
                "              }\n" +
                "            }\n" +
                "          }\n" +
                "        }\n" +
                "      }\n" +
                "    }\n" +
                "  }\n" +
                "}";
    }
}
