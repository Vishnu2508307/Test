package com.smartsparrow.workspace.data;

import java.util.Objects;

public class IconLibrary {

    private String name;
    private IconLibraryState status;

    public String getName() {
        return name;
    }

    public IconLibrary setName(final String name) {
        this.name = name;
        return this;
    }

    public IconLibraryState getStatus() {
        return status;
    }

    public IconLibrary setStatus(final IconLibraryState status) {
        this.status = status;
        return this;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        IconLibrary that = (IconLibrary) o;
        return Objects.equals(name, that.name) && status == that.status;
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, status);
    }

    @Override
    public String toString() {
        return "IconLibrary{" +
                "name='" + name + '\'' +
                ", status='" + status + '\'' +
                '}';
    }
}
