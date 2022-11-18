package com.smartsparrow.rtm.message.recv.courseware.theme;

import java.util.Objects;
import java.util.UUID;

import com.smartsparrow.courseware.service.ThemeState;
import com.smartsparrow.rtm.message.ReceivedMessage;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

@SuppressFBWarnings(value = "UWF_UNWRITTEN_FIELD", justification = "Hydrated via Jackson on message deserialization")
public class CreateThemeVariantMessage extends ReceivedMessage implements ThemeMessage {

    private UUID themeId;
    private String variantName;
    private String config;
    private ThemeState state;

    public UUID getThemeId() {
        return themeId;
    }

    public String getVariantName() {
        return variantName;
    }

    public String getConfig() {
        return config;
    }

    public ThemeState getState() {
        return state;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CreateThemeVariantMessage that = (CreateThemeVariantMessage) o;
        return Objects.equals(themeId, that.themeId) &&
                Objects.equals(variantName, that.variantName) &&
                Objects.equals(config, that.config) &&
                Objects.equals(state, that.state);
    }

    @Override
    public int hashCode() {
        return Objects.hash(themeId, variantName, config, state);
    }

    @Override
    public String toString() {
        return "CreateThemeVariantMessage{" +
                "themeId=" + themeId +
                ", variantName='" + variantName + '\'' +
                ", config='" + config + '\'' +
                ", state='" + state + '\'' +
                '}';
    }
}
