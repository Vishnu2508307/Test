package com.smartsparrow.sso.event;

import java.util.List;
import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonProperty;

public class IESBatchProfileGetParams {

    @JsonProperty("piIds")
    private final List<String> piIds;

    public IESBatchProfileGetParams(List<String> ids) {
        this.piIds = ids;
    }

    public List<String> getPiIds() {
        return piIds;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        IESBatchProfileGetParams that = (IESBatchProfileGetParams) o;
        return Objects.equals(piIds, that.piIds);
    }

    @Override
    public int hashCode() {
        return Objects.hash(piIds);
    }

    @Override
    public String toString() {
        return "IESBatchProfileGetParams{" +
                "piIds=" + piIds +
                '}';
    }
}
