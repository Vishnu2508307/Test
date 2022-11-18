package com.smartsparrow.learner.data;

import java.util.Map;
import java.util.Objects;
import java.util.UUID;

public class EvaluationScopeData {

    private UUID studentScopeURN;
    private Map<UUID, String> studentScopeDataMap;

    public UUID getStudentScopeURN() {
        return studentScopeURN;
    }

    public EvaluationScopeData setStudentScopeURN(UUID studentScopeURN) {
        this.studentScopeURN = studentScopeURN;
        return this;
    }

    public Map<UUID, String> getStudentScopeDataMap() {
        return studentScopeDataMap;
    }

    public EvaluationScopeData setStudentScopeDataMap(Map<UUID, String> studentScopeDataMap) {
        this.studentScopeDataMap = studentScopeDataMap;
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        EvaluationScopeData that = (EvaluationScopeData) o;
        return Objects.equals(studentScopeURN, that.studentScopeURN) &&
                Objects.equals(studentScopeDataMap, that.studentScopeDataMap);
    }

    @Override
    public int hashCode() {
        return Objects.hash(studentScopeURN, studentScopeDataMap);
    }

    @Override
    public String toString() {
        return "EvaluationScopeData{" +
                "studentScopeURN=" + studentScopeURN +
                ", studentScopeDataMap=" + studentScopeDataMap +
                '}';
    }
}
