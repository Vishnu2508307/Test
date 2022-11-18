package com.smartsparrow.rtm.message.recv.plugin;

import java.util.Objects;
import java.util.UUID;

import com.smartsparrow.courseware.data.CoursewareElementType;
import com.smartsparrow.plugin.data.PluginLogLevel;
import com.smartsparrow.rtm.message.ReceivedMessage;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

@SuppressFBWarnings(value = "UWF_UNWRITTEN_FIELD", justification = "Hydrated via Jackson on message deserialization")
public class WorkspacePluginLogMessage extends ReceivedMessage implements PluginLogMessage {

    private UUID pluginId;
    private String version;
    private PluginLogLevel level;
    private String message;
    private String args;
    private String pluginContext;
    private UUID elementId;
    private CoursewareElementType elementType;
    private UUID projectId;
    private UUID eventId;
    private UUID transactionId;
    private String transactionName;
    private UUID segmentId;
    private String segmentName;
    private String transactionSequence;

    @Override
    public UUID getPluginId() {
        return pluginId;
    }

    @Override
    public String getVersion() {
        return version;
    }

    @Override
    public PluginLogLevel getLevel() {
        return level;
    }

    @Override
    public String getMessage() {
        return message;
    }

    @Override
    public String getArgs() {
        return args;
    }

    @Override
    public String getPluginContext() {
        return pluginContext;
    }

    public UUID getElementId() {
        return elementId;
    }

    public CoursewareElementType getElementType() {
        return elementType;
    }

    public UUID getProjectId() {
        return projectId;
    }

    @Override
    public UUID getEventId() {
        return eventId;
    }

    @Override
    public UUID getTransactionId() {
        return transactionId;
    }

    @Override
    public String getTransactionName() {
        return transactionName;
    }

    @Override
    public UUID getSegmentId() {
        return segmentId;
    }

    @Override
    public String getSegmentName() {
        return segmentName;
    }

    @Override
    public String getTransactionSequence() {
        return transactionSequence;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        WorkspacePluginLogMessage that = (WorkspacePluginLogMessage) o;
        return Objects.equals(pluginId, that.pluginId) && Objects.equals(version,
                                                                         that.version) && level == that.level && Objects.equals(
                message,
                that.message) && Objects.equals(args, that.args) && Objects.equals(pluginContext,
                                                                                   that.pluginContext) && Objects.equals(
                elementId,
                that.elementId) && elementType == that.elementType && Objects.equals(projectId,
                                                                                     that.projectId) && Objects.equals(
                eventId,
                that.eventId) && Objects.equals(transactionId, that.transactionId) && Objects.equals(
                transactionName,
                that.transactionName) && Objects.equals(segmentId, that.segmentId) && Objects.equals(
                segmentName,
                that.segmentName) && Objects.equals(transactionSequence, that.transactionSequence);
    }

    @Override
    public int hashCode() {
        return Objects.hash(pluginId,
                            version,
                            level,
                            message,
                            args,
                            pluginContext,
                            elementId,
                            elementType,
                            projectId,
                            eventId,
                            transactionId,
                            transactionName,
                            segmentId,
                            segmentName,
                            transactionSequence);
    }

    @Override
    public String toString() {
        return "WorkspacePluginLogMessage{" +
                "pluginId=" + pluginId +
                ", version='" + version + '\'' +
                ", level=" + level +
                ", message='" + message + '\'' +
                ", args='" + args + '\'' +
                ", pluginContext='" + pluginContext + '\'' +
                ", elementId=" + elementId +
                ", elementType=" + elementType +
                ", projectId=" + projectId +
                ", eventId=" + eventId +
                ", transactionId=" + transactionId +
                ", transactionName='" + transactionName + '\'' +
                ", segmentId=" + segmentId +
                ", segmentName='" + segmentName + '\'' +
                ", transactionSequence='" + transactionSequence + '\'' +
                "} " + super.toString();
    }
}
