package com.smartsparrow.util.log.data;

import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.smartsparrow.util.Json;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class ThreadLog {

    private String name;
    private long id;

    public String getName() {
        return name;
    }

    public ThreadLog setName(String name) {
        this.name = name;
        return this;
    }

    public long getId() {
        return id;
    }

    public ThreadLog setId(long id) {
        this.id = id;
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ThreadLog threadLog = (ThreadLog) o;
        return id == threadLog.id &&
                Objects.equals(name, threadLog.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, id);
    }

    @Override
    public String toString() {
        return Json.stringify(this);
    }
}


