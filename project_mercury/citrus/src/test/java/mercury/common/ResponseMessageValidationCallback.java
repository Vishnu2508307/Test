package mercury.common;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.IOException;

import com.consol.citrus.context.TestContext;
import com.consol.citrus.exceptions.CitrusRuntimeException;
import com.consol.citrus.message.Message;
import com.consol.citrus.validation.callback.AbstractValidationCallback;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.smartsparrow.rtm.message.send.BasicResponseMessage;

public abstract class ResponseMessageValidationCallback<T> extends AbstractValidationCallback<T> {

    /**
     * The result type
     */
    private Class<T> resultType;

    public ResponseMessageValidationCallback(Class<T> resultType) {
        this.resultType = resultType;
    }

    @Override
    public void validate(Message message, TestContext context) {
        validate(parseJson(message), message.getHeaders(), context);
    }

    /**
     * Returns a name for root element in response json: {"type":"some.type.ok", "response": {"rootElement" : {} } }
     * @return
     */
    public abstract String getRootElementName();

    public abstract String getType();

    private T parseJson(Message message) {
        ObjectMapper jsonMapper = applicationContext.getBean(ObjectMapper.class);

        try {
            BasicResponseMessage basicResponseMessage = jsonMapper.readValue(message.getPayload(String.class), BasicResponseMessage.class);
            assertEquals(getType(), basicResponseMessage.getType());
            return jsonMapper.readValue(jsonMapper.writeValueAsString(basicResponseMessage.getResponse().get(getRootElementName())), resultType);
        } catch (IOException e) {
            throw new CitrusRuntimeException("Failed to unmarshal message payload", e);
        }
    }
}
