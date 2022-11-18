package com.smartsparrow.cache.config;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.MoreObjects;
import com.google.common.base.Objects;

public class RouteConsumersInfraResponse {

    @JsonProperty("leanerEvaluateCompleteConsumers")
    private Integer leanerEvaluateCompleteConsumers = 50;

    @JsonProperty("learnerStudentScopeSetConsumers")
    private Integer learnerStudentScopeSetConsumers = 10;

    @JsonProperty("leanerProgressBroadcastConsumers")
    private Integer leanerProgressBroadcastConsumers = 10;

    @JsonProperty("competencyDocumentUpdateConsumers")
    private Integer competencyDocumentUpdateConsumers = 2;

    public Integer getLeanerEvaluateCompleteConsumers() {
        return leanerEvaluateCompleteConsumers;
    }

    public RouteConsumersInfraResponse setLeanerEvaluateCompleteConsumers(Integer leanerEvaluateCompleteConsumers) {
        this.leanerEvaluateCompleteConsumers = leanerEvaluateCompleteConsumers;
        return this;
    }

    public Integer getLearnerStudentScopeSetConsumers() {
        return learnerStudentScopeSetConsumers;
    }

    public RouteConsumersInfraResponse setLearnerStudentScopeSetConsumers(Integer learnerStudentScopeSetConsumers) {
        this.learnerStudentScopeSetConsumers = learnerStudentScopeSetConsumers;
        return this;
    }

    public Integer getLeanerProgressBroadcastConsumers() {
        return leanerProgressBroadcastConsumers;
    }

    public RouteConsumersInfraResponse setLeanerProgressBroadcastConsumers(Integer leanerProgressBroadcastConsumers) {
        this.leanerProgressBroadcastConsumers = leanerProgressBroadcastConsumers;
        return this;
    }

    public Integer getCompetencyDocumentUpdateConsumers() {
        return competencyDocumentUpdateConsumers;
    }

    public RouteConsumersInfraResponse setCompetencyDocumentUpdateConsumers(Integer competencyDocumentUpdateConsumers) {
        this.competencyDocumentUpdateConsumers = competencyDocumentUpdateConsumers;
        return this;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("leanerEvaluateCompleteConsumers", leanerEvaluateCompleteConsumers)
                .add("learnerStudentScopeSetConsumers", learnerStudentScopeSetConsumers)
                .add("leanerProgressBroadcastConsumers", leanerProgressBroadcastConsumers)
                .add("competencyDocumentUpdateConsumers", competencyDocumentUpdateConsumers).toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        RouteConsumersInfraResponse that = (RouteConsumersInfraResponse) o;
        return Objects.equal(leanerEvaluateCompleteConsumers, that.leanerEvaluateCompleteConsumers) && Objects
                .equal(learnerStudentScopeSetConsumers, that.learnerStudentScopeSetConsumers) && Objects
                .equal(leanerProgressBroadcastConsumers, that.leanerProgressBroadcastConsumers) && Objects
                .equal(competencyDocumentUpdateConsumers, that.competencyDocumentUpdateConsumers);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(leanerEvaluateCompleteConsumers, learnerStudentScopeSetConsumers,
                leanerProgressBroadcastConsumers, competencyDocumentUpdateConsumers);
    }
}
