package com.smartsparrow.workspace.data;

import java.util.Objects;
import java.util.UUID;

public class IconLibraryByTheme {

    private UUID themeId;
    private String iconLibrary;
    private IconLibraryState status;

    public UUID getThemeId() {
        return themeId;
    }

    public IconLibraryByTheme setThemeId(final UUID themeId) {
        this.themeId = themeId;
        return this;
    }

    public String getIconLibrary() {
        return iconLibrary;
    }

    public IconLibraryByTheme setIconLibrary(final String iconLibrary) {
        this.iconLibrary = iconLibrary;
        return this;
    }

    public IconLibraryState getStatus() {
        return status;
    }

    public IconLibraryByTheme setStatus(final IconLibraryState status) {
        this.status = status;
        return this;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        IconLibraryByTheme that = (IconLibraryByTheme) o;
        return Objects.equals(themeId, that.themeId) && Objects.equals(iconLibrary,
                                                                       that.iconLibrary) && status == that.status;
    }

    @Override
    public int hashCode() {
        return Objects.hash(themeId, iconLibrary, status);
    }

    @Override
    public String toString() {
        return "IconLibraryByTheme{" +
                "themeId=" + themeId +
                ", iconLibrary='" + iconLibrary + '\'' +
                ", status='" + status + '\'' +
                '}';
    }
}
