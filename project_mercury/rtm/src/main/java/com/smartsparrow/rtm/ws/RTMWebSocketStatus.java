package com.smartsparrow.rtm.ws;

/**
 * Enumeration that provides some useful websocket statuses.
 *
 * Based on https://tools.ietf.org/html/rfc6455 specifications
 */
public enum RTMWebSocketStatus {

    NORMAL_CLOSURE(1000),
    GOING_AWAY(1001),
    PROTOCOL_ERROR(1002),
    UNACCEPTABLE_DATA(1003),
    POLICY_VIOLATION(1008);

    private int value;

    RTMWebSocketStatus(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }
}
