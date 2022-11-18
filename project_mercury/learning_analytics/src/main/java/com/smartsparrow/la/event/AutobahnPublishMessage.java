package com.smartsparrow.la.event;

import java.util.Map;
import java.util.Objects;
import java.util.UUID;

import com.smartsparrow.dataevent.BroadcastMessage;

public class AutobahnPublishMessage implements BroadcastMessage {

    private static final long serialVersionUID = -6474066519060006700L;
    private String payload;
    private String namespace;
    private String version;
    private String messageTypeCode;
    private String correlationId;
    private Map<String, String> tags;
    private String streamType;
    private String createType;
    private UUID trackingId;

    public String getPayload() {
        return payload;
    }

    public AutobahnPublishMessage setPayload(String payload) {
        this.payload = payload;
        return this;
    }

    public String getNamespace() {
        return namespace;
    }

    public AutobahnPublishMessage setNamespace(String namespace) {
        this.namespace = namespace;
        return this;
    }

    public String getVersion() {
        return version;
    }

    public AutobahnPublishMessage setVersion(String version) {
        this.version = version;
        return this;
    }

    public String getMessageTypeCode() {
        return messageTypeCode;
    }

    public AutobahnPublishMessage setMessageTypeCode(String messageTypeCode) {
        this.messageTypeCode = messageTypeCode;
        return this;
    }

    public String getCorrelationId() {
        return correlationId;
    }

    public AutobahnPublishMessage setCorrelationId(String correlationId) {
        this.correlationId = correlationId;
        return this;
    }

    public Map<String, String> getTags() {
        return tags;
    }

    public AutobahnPublishMessage setTags(Map<String, String> tags) {
        this.tags = tags;
        return this;
    }

    public String getStreamType() {
        return streamType;
    }

    public AutobahnPublishMessage setStreamType(String streamType) {
        this.streamType = streamType;
        return this;
    }

    public String getCreateType() {
        return createType;
    }

    public AutobahnPublishMessage setCreateType(String createType) {
        this.createType = createType;
        return this;
    }

    public UUID getTrackingId() {
        return trackingId;
    }

    public AutobahnPublishMessage setTrackingId(UUID trackingId) {
        this.trackingId = trackingId;
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AutobahnPublishMessage that = (AutobahnPublishMessage) o;
        return Objects.equals(payload, that.payload) &&
                Objects.equals(namespace, that.namespace) &&
                Objects.equals(version, that.version) &&
                Objects.equals(messageTypeCode, that.messageTypeCode) &&
                Objects.equals(correlationId, that.correlationId) &&
                Objects.equals(tags, that.tags) &&
                Objects.equals(streamType, that.streamType) &&
                Objects.equals(createType, that.createType) &&
                Objects.equals(trackingId, that.trackingId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(payload, namespace, version, messageTypeCode, correlationId, tags, streamType, createType, trackingId);
    }

    @Override
    public String toString() {
        return "AutobahnPublishMessage{" +
                "payload='" + payload + '\'' +
                ", namespace='" + namespace + '\'' +
                ", version='" + version + '\'' +
                ", messageTypeCode='" + messageTypeCode + '\'' +
                ", correlationId='" + correlationId + '\'' +
                ", tags=" + tags +
                ", streamType='" + streamType + '\'' +
                ", createType='" + createType + '\'' +
                ", trackingId=" + trackingId +
                '}';
    }
}
