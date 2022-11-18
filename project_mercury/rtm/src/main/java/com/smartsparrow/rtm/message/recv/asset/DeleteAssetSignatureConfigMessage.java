package com.smartsparrow.rtm.message.recv.asset;

import java.util.Objects;

import com.smartsparrow.rtm.message.ReceivedMessage;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

@SuppressFBWarnings(value = "UWF_UNWRITTEN_FIELD", justification = "Hydrated via Jackson on message deserialization")
public class DeleteAssetSignatureConfigMessage extends ReceivedMessage {

    private String host;
    private String path;

    public String getHost() {
        return host;
    }

    public String getPath() {
        return path;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DeleteAssetSignatureConfigMessage that = (DeleteAssetSignatureConfigMessage) o;
        return Objects.equals(host, that.host) &&
                Objects.equals(path, that.path);
    }

    @Override
    public int hashCode() {
        return Objects.hash(host, path);
    }

    @Override
    public String toString() {
        return "DeleteAssetSignatureConfigMessage{" +
                "host='" + host + '\'' +
                ", path='" + path + '\'' +
                '}';
    }
}
