package com.smartsparrow.la.data;

import java.util.Map;
import java.util.Objects;
import java.util.UUID;

public class EventSummary {
    private UUID id;
    private String namespace;
    private String version;
    private String messageTypeCode;
    private String streamType;
    private String createType;
    private String correlationId;
    private String payload;
    private Map<String, String> tags;

    public UUID getId() {
        return id;
    }

    public EventSummary setId(UUID id) {
        this.id = id;
        return this;
    }

    public String getNamespace() {
        return namespace;
    }

    public EventSummary setNamespace(String namespace) {
        this.namespace = namespace;
        return this;
    }

    public String getVersion() {
        return version;
    }

    public EventSummary setVersion(String version) {
        this.version = version;
        return this;
    }

    public String getMessageTypeCode() {
        return messageTypeCode;
    }

    public EventSummary setMessageTypeCode(String messageTypeCode) {
        this.messageTypeCode = messageTypeCode;
        return this;
    }

    public String getStreamType() {
        return streamType;
    }

    public EventSummary setStreamType(String streamType) {
        this.streamType = streamType;
        return this;
    }

    public String getCreateType() {
        return createType;
    }

    public EventSummary setCreateType(String createType) {
        this.createType = createType;
        return this;
    }

    public String getCorrelationId() {
        return correlationId;
    }

    public EventSummary setCorrelationId(String correlationId) {
        this.correlationId = correlationId;
        return this;
    }

    public String getPayload() {
        return payload;
    }

    public EventSummary setPayload(String payload) {
        this.payload = payload;
        return this;
    }

    public Map<String, String> getTags() {
        return tags;
    }

    public EventSummary setTags(Map<String, String> tags) {
        this.tags = tags;
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        EventSummary that = (EventSummary) o;
        return Objects.equals(id, that.id) &&
                Objects.equals(namespace, that.namespace) &&
                Objects.equals(version, that.version) &&
                Objects.equals(messageTypeCode, that.messageTypeCode) &&
                Objects.equals(streamType, that.streamType) &&
                Objects.equals(createType, that.createType) &&
                Objects.equals(correlationId, that.correlationId) &&
                Objects.equals(payload, that.payload) &&
                Objects.equals(tags, that.tags);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, namespace, version, messageTypeCode, streamType, createType, correlationId, payload, tags);
    }

    @Override
    public String toString() {
        return "EventSummary{" +
                "id=" + id +
                ", namespace='" + namespace + '\'' +
                ", version='" + version + '\'' +
                ", messageTypeCode='" + messageTypeCode + '\'' +
                ", streamType='" + streamType + '\'' +
                ", createType='" + createType + '\'' +
                ", correlationId='" + correlationId + '\'' +
                ", payload='" + payload + '\'' +
                ", tags=" + tags +
                '}';
    }
}
