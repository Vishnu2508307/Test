package mercury.helpers.courseware;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.BiConsumer;

import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.lang.StringUtils;

import com.consol.citrus.context.TestContext;
import com.smartsparrow.courseware.payload.ComponentPayload;

import mercury.common.PayloadBuilder;
import mercury.common.ResponseMessageValidationCallback;

public class ComponentHelper {

    public static String createComponentRequest(String type, String parentId, String pluginId, String pluginVersion, String config) {
        PayloadBuilder payloadBuilder = new PayloadBuilder()
                .addField("type", "author." + type + ".component.create")
                .addField(type + "Id", parentId)
                .addField("pluginId", pluginId)
                .addField("pluginVersion", pluginVersion);
        if (StringUtils.isNotBlank(config)) {
            payloadBuilder.addField("config", config);
        }
        return payloadBuilder.build();
    }

    public static String createComponentRequestWithSuppliedId(String type, String parentId, String pluginId, String pluginVersion, String componentId) {
        return new PayloadBuilder()
                .addField("type", "author." + type + ".component.create")
                .addField(type + "Id", parentId)
                .addField("pluginId", pluginId)
                .addField("pluginVersion", pluginVersion)
                .addField("componentId", componentId)
                .build();
    }

    public static String createComponentResponse(String type, String componentIdVar, String pluginId) {
        return "{" +
                "\"type\":\"author."+type+".component.create.ok\"," +
                "\"response\":{" +
                "\"component\":{" +
                "\"componentId\":\"@variable('" + componentIdVar + "')@\"," +
                "\"plugin\":{" +
                "\"pluginId\":\""+pluginId+"\"," +
                "\"name\":\"Course Citrus plugin\"," +
                "\"type\":\"course\"," +
                "\"version\":\"1.*\"," +
                "\"pluginFilters\":\"@notEmpty()@\"" +
                "}," +
                "\"parentId\":\"@notEmpty()@\"," +
                "\"parentType\":\""+type.toUpperCase()+"\"" +
                "}" +
                "}," +
                "\"replyTo\":\"@notEmpty()@\"" +
                "}";
    }

    public static String createComponentResponseWithConfig(String type, String componentIdVar, String pluginId, String config) {
        return "{" +
                "\"type\":\"author."+type+".component.create.ok\"," +
                "\"response\":{" +
                "\"component\":{" +
                "\"componentId\":\"@variable('" + componentIdVar + "')@\"," +
                "\"config\":\""+ StringEscapeUtils.escapeJava(config) +"\"," +
                "\"plugin\":{" +
                "\"pluginId\":\""+pluginId+"\"," +
                "\"name\":\"Course Citrus plugin\"," +
                "\"type\":\"course\"," +
                "\"version\":\"1.*\"," +
                "\"pluginFilters\":\"@notEmpty()@\"" +
                "}," +
                "\"parentId\":\"@notEmpty()@\"," +
                "\"parentType\":\""+type.toUpperCase()+"\"" +
                "}" +
                "}," +
                "\"replyTo\":\"@notEmpty()@\"" +
                "}";
    }
    public static String createComponentErrorResponse(String type, int statusCode, String errorMessage) {
        return "{" +
                "\"type\": \"author."+type+".component.create.error\"," +
                "\"code\": "+statusCode+"," +
                "\"message\": \"" + errorMessage + "\"," +
                "\"replyTo\": \"@notEmpty()@\"" +
                "}";
    }

    public static String replaceConfigRequest(String componentId, String config) {
        return new PayloadBuilder()
                .addField("type", "author.component.replace")
                .addField("componentId", componentId)
                .addField("config", config)
                .build();
    }

    public static String replaceConfigResponse(String componentId, String componentConfig) {
        return "{" +
                "\"type\": \"author.component.replace.ok\"," +
                    "\"response\": {" +
                        "\"componentConfig\": {" +
                        "\"id\": \"@notEmpty()@\"," +
                        "\"componentId\": \""+componentId+"\"," +
                        "\"config\" : \"mercury:escapeJson('"+componentConfig+"')\"" +
                        "}" +
                    "}," +
                "\"replyTo\": \"@notEmpty()@\"," +
                "}";
    }

    public static String replaceConfigErrorResponse(int code, String message) {
        return "{" +
                "\"type\": \"author.component.replace.error\"," +
                "\"code\": " + code + "," +
                "\"message\": \"" + message + "\"," +
                "\"replyTo\":\"@notEmpty()@\"" +
                "}";
    }

    public static String deleteComponentRequest(String type, String componentId, String parentId) {
        return new PayloadBuilder()
                .addField("type", "author." + type + ".component.delete")
                .addField("componentId", componentId)
                .addField(type + "Id", parentId)
                .build();
    }

    public static String deleteComponentResponse(String type, String componentId, String parentId) {
        return "{" +
                "\"type\":\"author."+type+".component.delete.ok\"," +
                "\"response\":{" +
                    "\"componentId\":\""+componentId+"\"," +
                    "\""+type+"Id\":\""+parentId+"\"" +
                "},\"replyTo\":\"@notEmpty()@\"}";
    }

    public static String deleteComponentErrorResponse(String type, int code, String message) {
        return "{" +
                "\"type\":\"author."+type+".component.delete.error\"," +
                "\"code\": " + code + "," +
                "\"message\": \"" + message + "\"," +
                "\"replyTo\":\"@notEmpty()@\"" +
                "}";
    }

    public static String componentGetRequest(String componentId) {
        return new PayloadBuilder()
                .addField("type", "author.component.get")
                .addField("componentId", componentId)
                .build();
    }

    public static String componentGetErrorResponse(int code, String errorMessage) {
        return "{" +
                    "\"type\":\"author.component.get.error\"," +
                    "\"code\":"+code+"," +
                    "\"message\":\""+errorMessage+"\"," +
                    "\"replyTo\":\"@notEmpty()@\"}";
    }

    public static ResponseMessageValidationCallback validateComponentGetResponse(
            BiConsumer<ComponentPayload, TestContext> consumer) {
        return new ResponseMessageValidationCallback<ComponentPayload>(ComponentPayload.class) {
            @Override
            public void validate(ComponentPayload payload, Map<String, Object> headers, TestContext context) {
                consumer.accept(payload, context);
            }

            @Override
            public String getRootElementName() {
                return "component";
            }

            @Override
            public String getType() {
                return "author.component.get.ok";
            }
        };
    }

    public static String multiDeleteComponentRequest(String type, List<String> componentIds, String parentId) {
        return new PayloadBuilder()
                .addField("type", "author." + type + ".components.delete")
                .addField("componentIds", componentIds)
                .addField(type + "Id", parentId)
                .build();
    }

    public static String moveComponentsRequest(List<String> componentIds, String elementId, String elementType) {
        return new PayloadBuilder()
                .addField("type", "author.components.move")
                .addField("componentIds", componentIds)
                .addField("elementId", elementId)
                .addField("elementType", elementType.toUpperCase())
                .build();
    }
}
