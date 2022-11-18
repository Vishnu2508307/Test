package com.smartsparrow.rtm.message.recv.courseware.theme;

import java.util.List;
import java.util.Objects;
import java.util.UUID;

import com.smartsparrow.rtm.message.ReceivedMessage;
import com.smartsparrow.workspace.data.IconLibrary;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

@SuppressFBWarnings(value = "UWF_UNWRITTEN_FIELD", justification = "Hydrated via Jackson on message deserialization")
public class AssociateThemeIconLibraryMessage extends ReceivedMessage implements ThemeMessage{

    private UUID themeId;
    private List<IconLibrary> iconLibraries;

    @Override
    public UUID getThemeId() {
        return themeId;
    }

    public List<IconLibrary> getIconLibraries() {
        return iconLibraries;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AssociateThemeIconLibraryMessage that = (AssociateThemeIconLibraryMessage) o;
        return Objects.equals(themeId, that.themeId) && Objects.equals(iconLibraries,
                                                                       that.iconLibraries);
    }

    @Override
    public int hashCode() {
        return Objects.hash(themeId, iconLibraries);
    }

    @Override
    public String toString() {
        return "CreateThemeIconLibraryMessage{" +
                "themeId=" + themeId +
                ", iconLibraries=" + iconLibraries +
                '}';
    }
}
