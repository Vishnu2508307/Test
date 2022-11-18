package com.smartsparrow.rtm.wiring;

import static com.google.common.base.Preconditions.checkState;

import java.lang.annotation.Annotation;
import java.util.Map;

import com.google.common.collect.Maps;
import com.google.inject.Key;
import com.google.inject.OutOfScopeException;
import com.google.inject.Provider;
import com.google.inject.Scope;
import com.google.inject.Scopes;

/**
 * A Real time message (RTM) scope, encapsulates a scope to be used within the message handling.
 *
 * This is essentially a copy from Guice's documentation, https://github.com/google/guice/wiki/CustomScopes
 *
 * Objects using the scope should be Injected as a Provider&lt;T&gt;.
 */
public class RTMScope implements Scope {

    private static final Provider<Object> SEEDED_KEY_PROVIDER = () -> {
        throw new IllegalStateException("If you got here then it means that" //
                                                + " your code asked for scoped object which should have been"
                                                + " explicitly seeded in this scope by calling seed(), but was not.");
    };

    private final ThreadLocal<Map<Key<?>, Object>> values = new ThreadLocal<>();

    /**
     * Enter the scope.
     */
    public void enter() {
        checkState(values.get() == null, "A scoping block is already in progress");
        values.set(Maps.newHashMap());
    }

    /**
     * Exit the scope, clearing the seeded values.
     */
    public void exit() {
        checkState(values.get() != null, "No scoping block in progress");
        values.remove();
    }

    /**
     * Seed the value into the scope.
     *
     * @param key the key for the class
     * @param value the instance value
     * @param <T>
     */
    public <T> void seed(final Key<T> key, final T value) {
        Map<Key<?>, Object> scopedObjects = getScopedObjectMap(key);
        checkState(!scopedObjects.containsKey(key), //
                   "A value for the key %s was already seeded in this scope. Old value: %s New value: %s", //
                   key, scopedObjects.get(key), value);
        scopedObjects.put(key, value);
    }

    /**
     * Seed a value by class into the scope
     *
     * @param clazz the class type
     * @param value the instance value
     * @param <T>
     */
    public <T> void seed(final Class<T> clazz, final T value) {
        seed(Key.get(clazz), value);
    }

    /**
     * Seed a value by class and binding annotation into the scope
     *
     * @param clazz the class type
     * @param value the instance value
     * @param <T>
     */
    public <T> void seed(final Class<T> clazz, final Class<? extends Annotation> annotationType, final T value) {
        seed(Key.get(clazz, annotationType), value);
    }


    /**
     * Provides a scoped object
     *
     * @param key the object key
     * @param unscoped the unscoped provider
     * @param <T>
     * @return the Provider to the scoped object
     */
    public <T> Provider<T> scope(final Key<T> key, final Provider<T> unscoped) {
        return () -> {
            Map<Key<?>, Object> scopedObjects = getScopedObjectMap(key);

            @SuppressWarnings("unchecked")
            T current = (T) scopedObjects.get(key);
            if (current == null && !scopedObjects.containsKey(key)) {
                current = unscoped.get();

                // don't remember proxies; these exist only to serve circular dependencies
                if (Scopes.isCircularProxy(current)) {
                    return current;
                }

                scopedObjects.put(key, current);
            }
            return current;
        };
    }

    private <T> Map<Key<?>, Object> getScopedObjectMap(Key<T> key) {
        Map<Key<?>, Object> scopedObjects = values.get();
        if (scopedObjects == null) {
            throw new OutOfScopeException("Cannot access " + key + " outside of a scoping block");
        }
        return scopedObjects;
    }

    /**
     * Returns a provider that always throws exception complaining that the object
     * in question must be seeded before it can be injected.
     *
     * @return typed provider
     */
    @SuppressWarnings({ "unchecked" })
    public static <T> Provider<T> seededKeyProvider() {
        return (Provider<T>) SEEDED_KEY_PROVIDER;
    }
}
