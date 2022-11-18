package com.smartsparrow.learner.data;

import java.util.Objects;
import java.util.UUID;

import javax.annotation.Nullable;

public class LearnerInteractivePayload extends LearnerInteractive implements LearnerWalkablePayload {

    private static final long serialVersionUID = -6405208608991922805L;

    public LearnerInteractivePayload(final LearnerInteractive interactive) {
        this.setId(interactive.getId());
        this.setChangeId(interactive.getChangeId());
        this.setDeploymentId(interactive.getDeploymentId());
        this.setConfig(interactive.getConfig());
        this.setEvaluationMode(interactive.getEvaluationMode());
        this.setPluginId(interactive.getPluginId());
        this.setPluginVersionExpr(interactive.getPluginVersionExpr());
        this.setStudentScopeURN(interactive.getStudentScopeURN());
    }

    @Nullable
    @Override
    public String getTheme() {
        return null;
    }

    @Nullable
    @Override
    public UUID getCreatorId() {
        return null;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        return super.equals(o);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode());
    }

    @Override
    public String toString() {
        return "LearnerInteractivePayload{" +
                super.toString() +
                "}";
    }
}
