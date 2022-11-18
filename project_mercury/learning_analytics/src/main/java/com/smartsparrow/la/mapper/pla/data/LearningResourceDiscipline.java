package com.smartsparrow.la.mapper.pla.data;

import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.smartsparrow.util.Json;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class LearningResourceDiscipline {
    private String disciplineId;
    private String disciplineName;

    @JsonProperty("disciplineId")
    public String getDisciplineId() {
        return disciplineId;
    }

    public LearningResourceDiscipline setDisciplineId(String disciplineId) {
        this.disciplineId = disciplineId;
        return this;
    }

    @JsonProperty("disciplineName")
    public String getDisciplineName() {
        return disciplineName;
    }

    public LearningResourceDiscipline setDisciplineName(String disciplineName) {
        this.disciplineName = disciplineName;
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        LearningResourceDiscipline that = (LearningResourceDiscipline) o;
        return Objects.equals(disciplineId, that.disciplineId) &&
                Objects.equals(disciplineName, that.disciplineName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(disciplineId, disciplineName);
    }

    @Override
    public String toString() {
        return Json.stringify(this);
    }
}
