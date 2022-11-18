package com.smartsparrow.rtm.util;

public enum NewRelicTransactionAttributes {
    ELEMENT_ID("elementId"),
    COURSE_ID("courseId"),
    EXPORT_ID("exportId"),
    ACCOUNT_ID("accountId");

    public final String id;
    NewRelicTransactionAttributes(String id) {
        this.id = id;
    }

    public String getValue(){
        return id;
    }
}
