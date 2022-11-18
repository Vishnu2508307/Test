package com.smartsparrow.learner.data;

import java.util.Objects;
import java.util.UUID;

import com.smartsparrow.courseware.data.CoursewareElement;

public class DeploymentStepLog {

    private UUID id;
    private Deployment deployment;
    private DeploymentStepState state;
    private String message;
    private CoursewareElement element;

    public UUID getId() {
        return id;
    }

    public DeploymentStepLog setId(UUID id) {
        this.id = id;
        return this;
    }

    public Deployment getDeployment() {
        return deployment;
    }

    public DeploymentStepLog setDeployment(Deployment deployment) {
        this.deployment = deployment;
        return this;
    }

    public DeploymentStepState getState() {
        return state;
    }

    public DeploymentStepLog setState(DeploymentStepState state) {
        this.state = state;
        return this;
    }

    public String getMessage() {
        return message;
    }

    public DeploymentStepLog setMessage(String message) {
        this.message = message;
        return this;
    }

    public CoursewareElement getElement() {
        return element;
    }

    public DeploymentStepLog setElement(CoursewareElement element) {
        this.element = element;
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DeploymentStepLog that = (DeploymentStepLog) o;
        return Objects.equals(id, that.id) &&
                Objects.equals(deployment, that.deployment) &&
                state == that.state &&
                Objects.equals(message, that.message) &&
                Objects.equals(element, that.element);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, deployment, state, message, element);
    }

    @Override
    public String toString() {
        return "DeploymentStepLog{" +
                "id=" + id +
                ", deployment=" + deployment +
                ", state=" + state +
                ", message='" + message + '\'' +
                ", element=" + element +
                '}';
    }
}
