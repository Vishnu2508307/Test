package mercury.common;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class Event {

    private String type;
    private Map<String, Object> fields = new HashMap<>();
    private String rtmSubscriptionIdVariable;

    public Event(String type) {
        this.type = type;
    }

    public String getType() {
        return type;
    }

    public Event setType(String type) {
        this.type = type;
        return this;
    }

    public Map<String, Object> getFields() {
        return fields;
    }

    public Event setFields(Map<String, Object> fields) {
        this.fields = fields;
        return this;
    }

    public Event addField(String key, Object value) {
        this.fields.put(key, value);
        return this;
    }

    public String getRtmSubscriptionIdVariable() {
        return rtmSubscriptionIdVariable;
    }

    public Event setRtmSubscriptionIdVariable(String rtmSubscriptionIdVariable) {
        this.rtmSubscriptionIdVariable = rtmSubscriptionIdVariable;
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Event event = (Event) o;
        return Objects.equals(type, event.type) &&
                Objects.equals(fields, event.fields) &&
                Objects.equals(rtmSubscriptionIdVariable, event.rtmSubscriptionIdVariable);
    }

    @Override
    public int hashCode() {
        return Objects.hash(type, fields, rtmSubscriptionIdVariable);
    }

    @Override
    public String toString() {
        return "Event{" +
                "type='" + type + '\'' +
                ", fields=" + fields +
                ", rtmSubscriptionIdVariable='" + rtmSubscriptionIdVariable + '\'' +
                '}';
    }
}
