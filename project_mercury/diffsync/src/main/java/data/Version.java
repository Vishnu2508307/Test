package data;

import java.io.Serializable;
import java.util.Objects;


/**
 * Describes the object Version used in this Differential Synchronization implementation
 */
public class Version implements Comparable<Version> , Serializable {

    private static final long serialVersionUID = 2897179687824124010L;

    private Long value;

    public synchronized Long getValue() {
        return value;
    }

    /**
     * Sets the value of the version.
     * This method must be synchronized to avoid concurrency issues
     *
     * @param value the value to set
     * @return the object
     */
    public synchronized Version setValue(Long value) {
        this.value = value;
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Version version = (Version) o;
        return Objects.equals(value, version.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(value);
    }

    @Override
    public String toString() {
        return "Version{" +
                "value=" + value +
                '}';
    }

    @Override
    public int compareTo(Version other) {
        return getValue().compareTo(other.getValue());
    }

}
