package mercury.glue.step.courseware;

import java.util.List;

import mercury.common.PayloadBuilder;

public class CoursewareAssetHelper {

    public static String addAssetRequest(String elementId, String elementType, String assetUrn) {
        PayloadBuilder payload = new PayloadBuilder()
                .addField("type", "author.courseware.asset.add")
                .addField("elementId", elementId)
                .addField("elementType", elementType.toUpperCase())
                .addField("assetURN", assetUrn);
        return payload.build();
    }

    public static String removeAssetRequest(String elementId, String elementType, String assetUrn) {
        PayloadBuilder payload = new PayloadBuilder()
                .addField("type", "author.courseware.asset.remove")
                .addField("elementId", elementId)
                .addField("elementType", elementType.toUpperCase())
                .addField("assetURN", assetUrn);
        return payload.build();
    }

    public static String removeAssetsRequest(String elementId, String elementType, List<String> assetUrn) {
        PayloadBuilder payload = new PayloadBuilder()
                .addField("type", "author.courseware.assets.remove")
                .addField("elementId", elementId)
                .addField("elementType", elementType.toUpperCase())
                .addField("assetURN", assetUrn);
        return payload.build();
    }

    public static String addAssetErrorResponse(int code, String errorMessage) {
        return "{" +
                "\"type\":\"author.courseware.asset.add.error\"," +
                "\"code\":" + code + "," +
                "\"message\":\"" + errorMessage + "\"," +
                "\"replyTo\":\"@notEmpty()@\"}";
    }

    public static String removeAssetErrorResponse(int code, String errorMessage) {
        return "{" +
                "\"type\":\"author.courseware.asset.remove.error\"," +
                "\"code\":" + code + "," +
                "\"message\":\"" + errorMessage + "\"," +
                "\"replyTo\":\"@notEmpty()@\"}";
    }
}
