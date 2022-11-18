package com.smartsparrow.rtm.message.recv.courseware;

import java.util.Objects;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JacksonInject;
import com.smartsparrow.config.service.ConfigurationService;
import com.smartsparrow.eval.wiring.EvaluationFeatureMode;
import com.smartsparrow.rtm.message.ReceivedMessage;
import com.smartsparrow.util.Enums;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

@SuppressFBWarnings(value = "UWF_UNWRITTEN_FIELD", justification = "Hydrated via Jackson on message deserialization")
public class LearnerEvaluateMessage extends ReceivedMessage {

    public UUID interactiveId;
    public UUID deploymentId;
    private UUID timeId;

    @JacksonInject("configurationService")
    private ConfigurationService configurationService;

    public UUID getInteractiveId() {
        return interactiveId;
    }

    public LearnerEvaluateMessage setInteractiveId(UUID interactiveId) {
        this.interactiveId = interactiveId;
        return this;
    }

    public UUID getDeploymentId() {
        return deploymentId;
    }

    public LearnerEvaluateMessage setDeploymentId(UUID deploymentId) {
        this.deploymentId = deploymentId;
        return this;
    }

    public UUID getTimeId() {
        return timeId;
    }

    public LearnerEvaluateMessage setTimeId(UUID timeId) {
        this.timeId = timeId;
        return this;
    }

    @Override
    public Mode getMode() {
        EvaluationFeatureMode mode = configurationService.get(EvaluationFeatureMode.class, "evaluation.mode");
        // make this safe, if no mode is found then use DEFAULT
        if (mode == null) {
            mode = new EvaluationFeatureMode().setProcessingMode("DEFAULT");
        }
        return Enums.of(Mode.class, mode.getProcessingMode());
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        LearnerEvaluateMessage that = (LearnerEvaluateMessage) o;
        return Objects.equals(interactiveId, that.interactiveId) &&
                Objects.equals(deploymentId, that.deploymentId) &&
                Objects.equals(configurationService, that.configurationService) &&
                Objects.equals(timeId, that.timeId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(interactiveId, deploymentId, configurationService, timeId);
    }

    @Override
    public String toString() {
        return "LearnerEvaluateMessage{" +
                "interactiveId=" + interactiveId +
                ", deploymentId=" + deploymentId +
                ", configurationService=" + configurationService +
                ", timeId=" + timeId +
                '}';
    }
}
