package mercury.common;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.jetty.util.UrlEncoded;

import com.consol.citrus.exceptions.CitrusRuntimeException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class PayloadBuilder {

    private ObjectMapper objectMapper;
    private Map<String, Object> properties;

    public PayloadBuilder() {
        this.objectMapper = new ObjectMapper();
        this.properties = new HashMap<>();
        addField("id", System.currentTimeMillis());
    }

    public PayloadBuilder addField(String key, Object value) {
        this.properties.put(key, value);
        return this;
    }

    public PayloadBuilder addAllFields(String key, Map<String, String> fields) {
        this.properties.put(key, fields);
        return this;
    }

    public PayloadBuilder addAll(Map<String, Object> fields) {
        this.properties.putAll(fields);
        return this;
    }

    public String build() {
        try {
            return objectMapper.writeValueAsString(properties);
        } catch (JsonProcessingException e) {
            throw new CitrusRuntimeException("unable to write payload", e);
        }
    }

    /**
     * Build urlEncodedForm string from a map of parameters
     *
     * @param params the form parameters
     * @return a url encoded form string
     */
    public static String getUrlEncodedForm(Map<String, String> params) {
        StringBuilder result = new StringBuilder();
        boolean first = true;

        for(Map.Entry<String, String> entry : params.entrySet()){

            if (first) {
                first = false;
            } else {
                result.append("&");
            }

            result.append(entry.getKey());
            result.append("=");
            result.append(UrlEncoded.encodeString(entry.getValue()));
        }
        return result.toString();
    }
}
