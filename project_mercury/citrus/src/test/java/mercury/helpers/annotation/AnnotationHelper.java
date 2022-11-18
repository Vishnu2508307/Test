package mercury.helpers.annotation;

public class AnnotationHelper {

    public static String coursewareAnnotationCreateMutation(String rootElementId, String motivation, String elementId,
                                                            String jsonBody, String jsonTarget) {
        return "mutation {\n" +
                "AnnotationForCoursewareCreate(\n" +
                    "elementId: \"" + elementId + "\",\n" +
                    "rootElementId:\"" + rootElementId + "\",\n" +
                    "annotation: {\n" +
                        "motivation: " + motivation + ",\n" +
                        "target: \"" + jsonTarget + "\",\n" +
                        "body: \"" + jsonBody + "\"\n" +
                    "}" +
                ") {\n" +
                    "body\n" +
                    "id\n" +
                    "created\n" +
                    "creator {\n" +
                        "id\n" +
                    "}\n" +
                    "modified\n" +
                    "motivation\n" +
                    "target\n" +
                    "type\n" +
                   "}\n" +
              "}";
    }

    public static String deploymentAnnotationCreateMutation(String deploymentId, String elementId, String body,
                                                            String target, String motivation) {
        return "mutation {\n" +
                "  AnnotationForDeploymentCreate(\n" +
                "    deploymentId: \"" + deploymentId + "\",\n" +
                "    elementId: \"" + elementId + "\",\n" +
                "    annotation: {\n" +
                "      body: \"" + body + "\",\n" +
                "      target: \"" + target + "\",\n" +
                "      motivation: " + motivation + "\n" +
                "    }) {\n" +
                "    body\n" +
                "    id\n" +
                "    created\n" +
                "    creator {\n" +
                "      id\n" +
                "    }\n" +
                "    modified\n" +
                "    motivation\n" +
                "    target\n" +
                "    type\n" +
                "  }\n" +
                "}";
    }

    public static String coursewareAnnotationsQuery(String rootElementId, String motivation, String elementId) {

        String elementQuery = "";


        if (elementId != null) {
            elementQuery = ", elementId: \"" + elementId + "\"";
        }

        return "query {\n" +
                "  AnnotationsByCourseware(\n" +
                "    rootElementId:\"" + rootElementId + "\",\n" +
                "    motivation: " + motivation + " \n" +
                "    " + elementQuery + ") {\n" +
                "      id\n" +
                "      body\n" +
                "      motivation\n" +
                "      target\n" +
                "      created\n" +
                "    }\n" +
                "}";
    }

    public static String deploymentAccountAnnotationsQuery(String deploymentId, String creatorAccountId, String motivation,
                                                    String elementId) {

        String elementQuery = "";


        if (elementId != null) {
            elementQuery = ", elementId: \"" + elementId + "\"";
        }

        return "query {\n" +
                "  AnnotationsByDeploymentAccount(\n" +
                "    deploymentId:\"" + deploymentId + "\",\n" +
                "    creatorAccountId:\"" + creatorAccountId + "\",\n" +
                "    motivation: " + motivation + ",\n" +
                "    " + elementQuery + ") {\n" +
                "    body\n" +
                "    id\n" +
                "    created\n" +
                "    modified\n" +
                "    motivation\n" +
                "    target\n" +
                "    type\n" +
                "    creator {\n" +
                "      id\n" +
                "    }\n" +
                "  }\n" +
                "}";
    }

    public static String deploymentAnnotationsQuery(String deploymentId, String motivation,
                                                           String elementId) {

        String elementQuery = "";


        if (elementId != null) {
            elementQuery = ", elementId: \"" + elementId + "\"";
        }

        return "query {\n" +
                "  AnnotationsByDeployment(\n" +
                "    deploymentId:\"" + deploymentId + "\",\n" +
                "    motivation: " + motivation + ",\n" +
                "    " + elementQuery + ") {\n" +
                "    body\n" +
                "    id\n" +
                "    created\n" +
                "    modified\n" +
                "    motivation\n" +
                "    target\n" +
                "    type\n" +
                "    creator {\n" +
                "      id\n" +
                "    }\n" +
                "  }\n" +
                "}";
    }

    public static String coursewareAnnotationDeleteMutation(String annotationId) {
        return "mutation {\n" +
                "  AnnotationForCoursewareDelete(annotationId: {\n" +
                "    id: \""+annotationId+ "\"\n" +
                "  }) {\n" +
                "    id\n" +
                "  }\n" +
                "}";
    }
}
