package com.smartsparrow.rtm.message.recv.courseware.theme;

import java.util.Objects;
import java.util.UUID;

import com.smartsparrow.rtm.message.ReceivedMessage;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

@SuppressFBWarnings(value = "UWF_UNWRITTEN_FIELD", justification = "Hydrated via Jackson on message deserialization")
public class UpdateThemeVariantMessage extends ReceivedMessage implements ThemeMessage {

    private UUID themeId;
    private UUID variantId;
    private String variantName;
    private String config;

    @Override
    public UUID getThemeId() {
        return themeId;
    }

    public UUID getVariantId() {
        return variantId;
    }

    public String getVariantName() {
        return variantName;
    }

    public String getConfig() {
        return config;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        UpdateThemeVariantMessage that = (UpdateThemeVariantMessage) o;
        return Objects.equals(themeId, that.themeId) &&
                Objects.equals(variantId, that.variantId) &&
                Objects.equals(variantName, that.variantName) &&
                Objects.equals(config, that.config);
    }

    @Override
    public int hashCode() {
        return Objects.hash(themeId, variantId, variantName, config);
    }

    @Override
    public String toString() {
        return "UpdateThemeVariantMessage{" +
                "themeId=" + themeId +
                ", variantId=" + variantId +
                ", variantName='" + variantName + '\'' +
                ", config='" + config + '\'' +
                '}';
    }
}
