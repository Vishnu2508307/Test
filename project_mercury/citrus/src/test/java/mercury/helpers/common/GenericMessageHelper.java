package mercury.helpers.common;

import static com.google.common.base.Preconditions.checkArgument;

import java.io.IOException;
import java.util.Map;

import com.consol.citrus.message.Message;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.smartsparrow.rtm.message.send.BasicResponseMessage;

public class GenericMessageHelper {

    /**
     * Validate the unauthorized response for a message response
     *
     * @param type the expected message type
     * @return a json string representation of the unauthorized response
     */
    public static String getUnauthorizedResponse(String type) {
        return "{" +
                "\"type\":\"" + type + "\"," +
                "\"code\":401," +
                "\"message\":\"@notEmpty()@\"," +
                "\"replyTo\":\"@notEmpty()@\"" +
                "}";
    }

    /**
     * Convert a received citrus message to a basic response message
     * @param message the message to convert
     * @return a basicResponseMessage
     * @throws IOException when the conversion fails
     */
    public static BasicResponseMessage convertToBasicResponseMessage(Message message) throws IOException {
        return new ObjectMapper().readValue(message.getPayload().toString(), BasicResponseMessage.class);
    }

    /**
     * Extracts a specific response field and converts it to the supplied Type T
     *
     * @param typeReference the expected return type
     * @param response the response map to extract the field from
     * @param field the field to extract from the response
     * @param <T> the return type
     * @throws IOException when the conversion fails
     * @throws IllegalArgumentException when the supplied response does not contain the supplied field
     */
    public static <T> T extractResponseField(TypeReference<T> typeReference, Map<String, Object> response, String field)
            throws IOException {
        checkArgument(response.containsKey(field),
                String.format("field %s not present in supplied response map argument", field));
        ObjectMapper objectMapper = new ObjectMapper();
        return objectMapper.readValue(objectMapper.writeValueAsString(response.get(field)), typeReference);
    }
}
