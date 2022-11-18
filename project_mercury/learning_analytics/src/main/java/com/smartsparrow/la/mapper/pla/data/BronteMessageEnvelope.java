package com.smartsparrow.la.mapper.pla.data;

public abstract class BronteMessageEnvelope {
    private static final String ORIGINATING_SYSTEM_CODE = "Bronte";
    private static final String NAMESPACE = "Common";

    abstract String getMessageTypeCode();

    public String getOriginatingSystemCode() {
        return ORIGINATING_SYSTEM_CODE;
    }

    public String getNamespaceCode() {
        return NAMESPACE;
    }

    abstract String getMessageVersion();

    abstract String getEnvironmentCode();

    abstract String getTransactionTypeCode();

    abstract String getTransactionDt();

    abstract String getMessageId();

}
