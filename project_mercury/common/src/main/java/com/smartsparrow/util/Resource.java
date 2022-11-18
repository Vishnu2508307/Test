package com.smartsparrow.util;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.net.URL;

import org.apache.commons.io.IOUtils;

import com.smartsparrow.exception.IllegalArgumentFault;
import com.smartsparrow.exception.IllegalStateFault;

public class Resource {

    /**
     * Load a resource file
     *
     * @param resourceName the resource file name to load
     * @return the input stream representing the resource content
     */
    public static InputStream load(final String resourceName) {
        return Resource.class.getClassLoader().getResourceAsStream(resourceName);
    }

    /**
     * Load a resource file as a string.
     *
     * @param resourceName the resource file name to load
     * @return a string representing the resource content
     * @throws IOException when failing to load the resource content
     */
    public static String loadAsString(final String resourceName) throws IOException {
        try (StringWriter writer = new StringWriter()) {
            IOUtils.copy(load(resourceName), writer);
            return writer.toString();
        }
    }

    /**
     * Get the absolute path of a resource
     *
     * @param resourceName the resource to get the path for
     * @return the resource absolute path
     * @throws IllegalArgumentFault when the resource cannot be found
     */
    public static String path(final String resourceName) {
        URL resource = Resource.class.getClassLoader().getResource(resourceName);
        if (resource != null) {
            return resource.getPath();
        }
        throw new IllegalStateFault("cannot find " + resourceName);
    }
}
