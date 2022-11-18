package mercury.helpers.courseware;

import java.util.Map;
import java.util.function.BiConsumer;

import com.consol.citrus.context.TestContext;
import com.consol.citrus.validation.callback.ValidationCallback;
import com.smartsparrow.courseware.payload.ActivityPayload;
import com.smartsparrow.courseware.payload.InteractivePayload;

import mercury.common.PayloadBuilder;
import mercury.common.ResponseMessageValidationCallback;

public class InteractiveHelper {

    public static ResponseMessageValidationCallback validateInteractiveGetResponse(
            BiConsumer<InteractivePayload, TestContext> consumer) {
        return new ResponseMessageValidationCallback<InteractivePayload>(InteractivePayload.class) {
            @Override
            public void validate(InteractivePayload payload, Map<String, Object> headers, TestContext context) {
                consumer.accept(payload, context);
            }

            @Override
            public String getRootElementName() {
                return "interactive";
            }

            @Override
            public String getType() {
                return "author.interactive.get.ok";
            }
        };
    }

    public static ValidationCallback interactiveResponseOk(BiConsumer<InteractivePayload, TestContext> consumer,
                                                        String rootElement, String type) {
        return new ResponseMessageValidationCallback<InteractivePayload>(InteractivePayload.class) {
            @Override
            public void validate(InteractivePayload payload, Map<String, Object> headers, TestContext context) {
                consumer.accept(payload, context);
            }

            @Override
            public String getRootElementName() {
                return rootElement;
            }

            @Override
            public String getType() {
                return type;
            }
        };
    }

    public static String getInteractiveRequest(String interactiveId) {
        return new PayloadBuilder()
                .addField("type", "author.interactive.get")
                .addField("interactiveId", interactiveId)
                .build();
    }

    public static String getInteractiveErrorResponse(int code, String errorMessage) {
        return "{" +
                    "\"type\":\"author.interactive.get.error\"," +
                    "\"code\":"+code+"," +
                    "\"message\":\""+errorMessage+"\"," +
                    "\"replyTo\":\"@notEmpty()@\"}";
    }

    public static String duplicateInteractive(String interactiveId, String pathwayId, Integer index) {
        PayloadBuilder pb = new PayloadBuilder();
        pb.addField("type", "author.interactive.duplicate");
        pb.addField("interactiveId", interactiveId);
        pb.addField("pathwayId", pathwayId);
        if (index != null) {
            pb.addField("index", index);
        }
        return pb.build();
    }

    public static String moveInteractiveToPathway(String interactiveId, String pathwayId, Integer index) {
        PayloadBuilder pb = new PayloadBuilder();
        pb.addField("type", "author.interactive.move");
        pb.addField("interactiveId", interactiveId);
        pb.addField("pathwayId", pathwayId);
        pb.addField("index", index);
        return pb.build();
    }

    public static ValidationCallback interactiveMoveOk(BiConsumer<InteractivePayload, TestContext> consumer) {
        return interactiveResponseOk(consumer, "interactive", "author.interactive.move.ok");
    }


}
