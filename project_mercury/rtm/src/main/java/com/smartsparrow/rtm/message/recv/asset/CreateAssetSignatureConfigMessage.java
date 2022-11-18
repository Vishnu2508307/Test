package com.smartsparrow.rtm.message.recv.asset;

import java.util.Objects;

import com.smartsparrow.asset.data.AssetSignatureStrategyType;
import com.smartsparrow.rtm.message.ReceivedMessage;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

@SuppressFBWarnings(value = "UWF_UNWRITTEN_FIELD", justification = "Hydrated via Jackson on message deserialization")
public class CreateAssetSignatureConfigMessage extends ReceivedMessage {

    private String host;
    private String path;
    private String config;
    private AssetSignatureStrategyType strategyType;

    public String getHost() {
        return host;
    }

    public String getPath() {
        return path;
    }

    public String getConfig() {
        return config;
    }

    public AssetSignatureStrategyType getStrategyType() {
        return strategyType;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CreateAssetSignatureConfigMessage that = (CreateAssetSignatureConfigMessage) o;
        return Objects.equals(host, that.host) &&
                Objects.equals(path, that.path) &&
                Objects.equals(config, that.config) &&
                strategyType == that.strategyType;
    }

    @Override
    public int hashCode() {
        return Objects.hash(host, path, config, strategyType);
    }

    @Override
    public String toString() {
        return "CreateAssetSignatureConfigMessage{" +
                "host='" + host + '\'' +
                ", path='" + path + '\'' +
                ", config='" + config + '\'' +
                ", strategyType=" + strategyType +
                '}';
    }
}
