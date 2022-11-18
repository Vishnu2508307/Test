package com.smartsparrow.rtm.ws;

import java.util.Arrays;

public enum RTMWebSocketContext {

    /**
     * This is the websocket endpoint for the learner environment
     */
    LEARN("/learn"),

    /**
     * This is the websocket endpoint for the Workspace environment
     */
    SOCKET("/socket");

    private final String path;

    RTMWebSocketContext(final String path) {
        this.path = path;
    }

    public String getPath() {
        return path;
    }

    /**
     * Find the RTMWebSocketContext based on the supplied path string
     *
     * @param path the value to find the context for
     * @return the rtm web socket context enum value
     * @throws IllegalStateException when the registered websocket context path is not defined in this enum
     */
    public static RTMWebSocketContext from(final String path) {
        RTMWebSocketContext found = Arrays.stream(RTMWebSocketContext.values())
                .filter(value -> value.getPath().equals(path))
                .findFirst()
                .orElse(null);

        if (found == null) {
            throw new IllegalStateException(String.format("unknown socket path `%s`", path));
        }

        return found;
    }
}
