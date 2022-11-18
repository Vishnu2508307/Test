package com.smartsparrow.rtm.message.recv.courseware.theme;

import java.util.Objects;
import java.util.UUID;

import com.smartsparrow.courseware.service.ThemeState;
import com.smartsparrow.rtm.message.ReceivedMessage;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

@SuppressFBWarnings(value = "UWF_UNWRITTEN_FIELD", justification = "Hydrated via Jackson on message deserialization")
public class GetThemeVariantMessage extends ReceivedMessage implements ThemeMessage {

    private UUID themeId;
    private UUID variantId;

    @Override
    public UUID getThemeId() {
        return themeId;
    }

    public UUID getVariantId() {
        return variantId;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        GetThemeVariantMessage that = (GetThemeVariantMessage) o;
        return Objects.equals(themeId, that.themeId) &&
                Objects.equals(variantId, that.variantId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(themeId, variantId);
    }

    @Override
    public String toString() {
        return "GetThemeVariantMessage{" +
                "themeId=" + themeId +
                ", variantId=" + variantId +
                '}';
    }
}
