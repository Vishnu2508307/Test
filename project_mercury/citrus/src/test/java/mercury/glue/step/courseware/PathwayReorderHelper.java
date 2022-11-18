package mercury.glue.step.courseware;

import java.util.List;

import mercury.common.PayloadBuilder;

public class PathwayReorderHelper {

    public static String pathwayReorderRequest(String pathwayId, List<String> walkableIds) {
        PayloadBuilder builder = new PayloadBuilder();
        builder.addField("type", "author.pathway.walkable.reorder");
        builder.addField("pathwayId", pathwayId);
        builder.addField("walkableIds", walkableIds);
        return builder.build();
    }

    public static String pathwayReorderOkResponse() {
        return "{" +
                "  \"type\": \"author.pathway.walkable.reorder.ok\"," +
                "  \"replyTo\": \"@notEmpty()@\"" +
                "}";
    }

    public static String pathwayReorderErrorResponse(int code, String message) {
        return "{" +
                "  \"type\": \"author.pathway.walkable.reorder.error\"," +
                "  \"code\": " + code + "," +
                "  \"message\": \"" + message + "\"," +
                "  \"replyTo\": \"@notEmpty()@\"" +
                "}";
    }


}
