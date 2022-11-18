package com.smartsparrow.rtm.message.recv.courseware.pathway;

import java.util.UUID;

import com.smartsparrow.rtm.message.MessageType;

/**
 * This class is intended to provide pathwayId for pathway authorizers.
 */
public interface PathwayMessage extends MessageType {

    UUID getPathwayId();
}
