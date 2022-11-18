package com.smartsparrow.workspace.data;

import java.util.List;
import java.util.Objects;
import java.util.UUID;

import com.smartsparrow.iam.service.PermissionLevel;

public class ThemePayload {
    private UUID id;
    private String name;
    private PermissionLevel permissionLevel;
    private List<ThemeVariant> themeVariants;
    private List<IconLibrary> iconLibraries;

    public UUID getId() {
        return id;
    }

    public ThemePayload setId(final UUID id) {
        this.id = id;
        return this;
    }

    public String getName() {
        return name;
    }

    public ThemePayload setName(final String name) {
        this.name = name;
        return this;
    }

    public List<ThemeVariant> getThemeVariants() {
        return themeVariants;
    }

    public ThemePayload setThemeVariants(final List<ThemeVariant> themeVariants) {
        this.themeVariants = themeVariants;
        return this;
    }

    public PermissionLevel getPermissionLevel() {
        return permissionLevel;
    }

    public ThemePayload setPermissionLevel(final PermissionLevel permissionLevel) {
        this.permissionLevel = permissionLevel;
        return this;
    }

    public List<IconLibrary> getIconLibraries() {
        return iconLibraries;
    }

    public ThemePayload setIconLibraries(final List<IconLibrary> iconLibraries) {
        this.iconLibraries = iconLibraries;
        return this;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ThemePayload that = (ThemePayload) o;
        return Objects.equals(id, that.id) &&
                Objects.equals(name, that.name) &&
                permissionLevel == that.permissionLevel &&
                Objects.equals(themeVariants, that.themeVariants) &&
                Objects.equals(iconLibraries, that.iconLibraries);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name, permissionLevel, themeVariants, iconLibraries);
    }

    @Override
    public String toString() {
        return "ThemePayload{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", permissionLevel=" + permissionLevel +
                ", themeVariants=" + themeVariants +
                ", iconLibraries=" + iconLibraries +
                '}';
    }

}
