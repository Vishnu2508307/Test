package com.smartsparrow.rtm.message.recv.courseware.theme;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

import java.util.Objects;
import java.util.UUID;

@SuppressFBWarnings(value = "UWF_UNWRITTEN_FIELD", justification = "Hydrated via Jackson on message deserialization")
public class ListThemeCollaboratorMessage extends GenericThemeMessage {

    private UUID themeId;
    private Integer limit;

    @Override
    public UUID getThemeId() {
        return themeId;
    }

    public Integer getLimit() {
        return limit;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        ListThemeCollaboratorMessage that = (ListThemeCollaboratorMessage) o;
        return Objects.equals(themeId, that.themeId) &&
                Objects.equals(limit, that.limit);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), themeId, limit);
    }

    @Override
    public String toString() {
        return "ListThemeCollaboratorMessage{" +
                "themeId=" + themeId +
                ", limit=" + limit +
                '}';
    }
}
