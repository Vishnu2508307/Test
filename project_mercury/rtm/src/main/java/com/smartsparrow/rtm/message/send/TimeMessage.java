package com.smartsparrow.rtm.message.send;

import java.util.HashMap;
import java.util.Map;

import com.smartsparrow.rtm.message.MessageClassification;
import com.smartsparrow.rtm.message.ResponseMessageType;

public class TimeMessage implements ResponseMessageType {

    private String typeBase = "time";
    private String type = typeBase;
    //
    private String replyTo;

    private Long epochMilli;
    private String rfc1123;

    public TimeMessage(MessageClassification classification) {
        type = typeBase + "." + classification;
    }

    @Override
    public String getType() {
        return type;
    }

    @Override
    public String getReplyTo() {
        return replyTo;
    }

    public TimeMessage setReplyTo(String replyTo) {
        this.replyTo = replyTo;
        return this;
    }

    public Map<String, Object> getResponse() {
        Map<String, Object> response = new HashMap<>();
        response.put("epochMilli", epochMilli);
        response.put("rfc1123", rfc1123);
        return response;
    }

    public TimeMessage setEpochMilli(Long epochMilli) {
        this.epochMilli = epochMilli;
        return this;
    }

    public TimeMessage setRfc1123(String rfc1123) {
        this.rfc1123 = rfc1123;
        return this;
    }
}
