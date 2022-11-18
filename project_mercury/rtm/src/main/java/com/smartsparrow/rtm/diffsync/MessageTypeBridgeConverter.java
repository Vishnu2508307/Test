package com.smartsparrow.rtm.diffsync;

import static com.smartsparrow.util.Warrants.affirmArgument;

import com.google.inject.Singleton;
import com.smartsparrow.exception.UnsupportedOperationFault;
import com.smartsparrow.rtm.message.ReceivedMessage;

import data.Exchangeable;

/**
 * This class is responsible for converting a {@link data.Exchangeable.Type}
 * into a message type string value that is used by the RTM layer for defining messages
 * see {@link ReceivedMessage#getType()}
 */
@Singleton
public class MessageTypeBridgeConverter {

    public static final String DIFF_SYNC_START = "diffSync.start";
    public static final String DIFF_SYNC_END = "diffSync.end";
    public static final String DIFF_SYNC_PATCH = "diffSync.patch";
    public static final String DIFF_SYNC_ACK = "diffSync.ack";

    public String from(final Exchangeable.Type type) {
        affirmArgument(type != null, "exchangeable type is required");

        switch (type) {
            case ACK:
                return DIFF_SYNC_ACK;
            case PATCH:
                return DIFF_SYNC_PATCH;
            case START:
                return DIFF_SYNC_START;
            case END:
                return DIFF_SYNC_END;
            default:
                throw new UnsupportedOperationFault(String.format("unknown exchangeable type '%s'", type));
        }
    }
}
