package com.smartsparrow.util.log.data;

import java.util.Objects;

import com.smartsparrow.util.Json;

public class ServerContext {

    private String hostName;

    public String getHostName() {
        return hostName;
    }

    public ServerContext setHostName(String hostName) {
        this.hostName = hostName;
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ServerContext that = (ServerContext) o;
        return Objects.equals(hostName, that.hostName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(hostName);
    }

    @Override
    public String toString() {
        return Json.stringify(this);
    }
}
