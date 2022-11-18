package com.smartsparrow.util;

import java.util.EnumMap;
import java.util.EnumSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Utility class to assist with String to Enum mapping
 */
public class Enums {

    /**
     * Returns the enum constant of the specified enum type with the specified name to uppercase.
     * The supplied name will be upper cased before lookup.
     *
     * @param enumType the {@code Class} object of the enum type from which to return a constant
     * @param name the name of the enum.
     * @param <T> The enum type whose constant is to be returned
     * @return the enum constant of the specified enum type with the specified name
     */
    public static <T extends Enum<T>> T ofToUpperCase(Class<T> enumType, String name) {
        return Enums.of(enumType, name.toUpperCase());
    }

    /**
     * Returns the enum constant of the specified enum type with the specified name.
     *
     * @param enumType the {@code Class} object of the enum type from which to return a constant
     * @param name the name of the enum.
     * @param <T> The enum type whose constant is to be returned
     * @return the enum constant of the specified enum type with the specified name
     */
    public static <T extends Enum<T>> T of(Class<T> enumType, String name) {
        return Enum.valueOf(enumType, name);
    }

    /**
     * Returns a set of enum constants of the specified enum type of the specified set of names.
     * The names will be upper cased before lookup.
     *
     * @param enumType the {@code Class} object of the enum type from which to return a constant
     * @param names the names of the enums
     * @param <T> The enum type whose constant is to be returned
     * @return an {@code EnumSet} containing enum constants of the specified enum type with the specified name
     */
    public static <T extends Enum<T>> Set<T> of(Class<T> enumType, Set<String> names) {
        return names.stream()
                // map to enum vals
                .map(val -> of(enumType, val))
                // collect it, starting with an empty EnumSet.
                .collect(Collectors.toCollection(() -> EnumSet.noneOf(enumType)));
    }

    /**
     * Returns a new map of enum constant of the specified enum type with the specified name to value.
     * The name value will be upper cased before lookup.
     *
     * @param enumType the {@code Class} object of the enum type from which to return a constant
     * @param valueNames a map containing String values to convert
     * @param <T> The enum type whose constant is to be returned
     * @return a new {@code EnumMap} of enum constants of the specified enum type with the specified name to values
     */
    public static <T extends Enum<T>> Map<T, ?> mapKeys(Class<T> enumType, Map<String, ?> valueNames) {
        return valueNames.entrySet().stream()
                //
                .collect(Collectors.toMap(entry -> of(enumType, entry.getKey()),
                                          //
                                          entry -> entry.getValue(),
                                          //
                                          (l, r) -> {
                                              // this is impossible.
                                              throw new IllegalArgumentException(
                                                      "Duplicate keys " + l + "and " + r + ".");
                                          },
                                          // collect it as an EnumMap, for efficency.
                                          () -> new EnumMap<>(enumType)));
    }

    /**
     * Returns a new map of key to enum constant of the specified enum type with the
     * specified name value. The name value will be upper cased before lookup.
     *
     * @param enumType the {@code Class} object of the enum type from which to return a constant
     * @param valueNames a map containing String values to convert
     * @param <T> The enum type whose constant is to be returned
     * @return a new map of keys mapped to enum constants of the specified enum type with the specified name
     */
    public static <T extends Enum<T>> Map<?, T> mapValues(Class<T> enumType, Map<?, String> valueNames) {
        return valueNames.entrySet().stream()
                //
                .collect(Collectors.toMap(entry -> entry.getKey(),
                                          //
                                          entry -> of(enumType, entry.getValue())));
    }

    /**
     * Returns the name() of the supplied enum constant.
     *
     * @param e The enum constant
     * @param <T> The enum type whose constant is to be returned
     * @return the name() for the supplied enum constant
     */
    public static <T extends Enum<T>> String asString(T e) {
        return e.name();
    }

    /**
     * Returns a set of enum names for the specified enum set of constants.
     *
     * @param values The enum constants
     * @param <T> The enum type whose constant is to be returned
     * @return a set of the name() for the supplied enum constants
     */
    public static <T extends Enum<T>> Set<String> asString(Set<T> values) {
        return values.stream() //
                .map(e -> asString(e)) //
                .collect(Collectors.toSet());
    }

    /**
     * Returns a new map of enum constant names to the specified values.
     *
     * @param values a map of enum constants to values
     * @param <T> The enum type whose constant is to be returned
     * @return the name() for the supplied enum constant
     * @return a new {@code Map} of enum constant names to the specified values
     */
    public static <T extends Enum<T>> Map<String, ?> asStringKeys(Map<T, ?> values) {
        return values.entrySet().stream() //
                .collect(Collectors.toMap(entry -> asString(entry.getKey()), //
                                          entry -> entry.getValue()));
    }

    /**
     * Returns a new map of specified keys to enum constant names.
     *
     * @param values a map of keys to enum constants
     * @param <T> The enum type whose constant is to be returned
     * @return a new {@code Map} of specified keys to enum constant names
     */
    public static <T extends Enum<T>> Map<?, String> asStringValues(Map<?, T> values) {
        return values.entrySet().stream() //
                .collect(Collectors.toMap(entry -> entry.getKey(), //
                                          entry -> asString(entry.getValue())));
    }

    /**
     * Turns an enum into a different enum type given the desired return enum has the name value defined
     *
     * @param t the input enum to transform
     * @param enumClass the desired return type class
     * @param <T> the input enum type
     * @param <R> the return enum type
     * @return the transformed enum
     * @throws IllegalArgumentException when <T> has no constant with the specified name
     * @throws NullPointerException when <R> ot its name is null
     */
    public static <T extends Enum<T>, R extends Enum<R>> R from(final T t, final Class<R> enumClass) {
        return of(enumClass, t.name());
    }
}

