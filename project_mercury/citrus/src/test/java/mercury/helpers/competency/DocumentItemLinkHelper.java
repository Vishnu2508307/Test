package mercury.helpers.competency;

import java.util.List;

import javax.annotation.Nonnull;

public class DocumentItemLinkHelper {


    public static String competencyDocumentItemLinkMutation(final String elementType, final String elementId,
                                                            final String documentId, final List<String> documentItemIds) {

        String items = resolveDocumentItemIds(documentId, documentItemIds);

        String input = String.format("{" +
                "elementType: %s," +
                "elementId: \"%s\"," +
                "documentItems: [" +
                  "%s" +
                "]}", elementType, elementId, items);

        return "mutation {" +
                 "competencyDocumentItemLink(input: " + input + ") {" +
                   "documentLink {" +
                     "elementId " +
                     "elementType " +
                     "documentItems { " +
                       "documentId " +
                       "documentItemId " +
                     "}" +
                   "}" +
                 "}" +
               "}";
    }

    public static String competencyDocumentItemUnlinkMutation(final String elementType, final String elementId,
                                                              final String documentId, final List<String> documentItemIds) {
        String items = resolveDocumentItemIds(documentId, documentItemIds);

        String input = String.format("{" +
                "elementType: %s," +
                "elementId: \"%s\"," +
                "documentItems: [" +
                  "%s" +
                "]}", elementType, elementId, items);

        return "mutation {" +
                "competencyDocumentItemUnlink(input: " + input + ") {" +
                  "documentLink {" +
                    "elementId " +
                    "elementType " +
                      "documentItems { " +
                        "documentId " +
                        "documentItemId " +
                      "}" +
                    "}" +
                  "}" +
                "}";    }

    @Nonnull
    private static String resolveDocumentItemIds(final String documentId, final List<String> documentItemIds) {
        String items = documentItemIds.stream()
                .map(documentItemId -> {
                    return "{documentId:\"" + documentId + "\", documentItemId:\"" + documentItemId + "\"},";
                }).reduce((prev, next) -> {
                    return prev + next;
                }).orElse("");

        // remove the last comma
        items = items.substring(0, items.length() - 1);
        return items;
    }
}
