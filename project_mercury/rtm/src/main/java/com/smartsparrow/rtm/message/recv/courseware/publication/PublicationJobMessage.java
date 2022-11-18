package com.smartsparrow.rtm.message.recv.courseware.publication;

import java.util.UUID;

import com.smartsparrow.rtm.message.MessageType;

public interface PublicationJobMessage extends MessageType {

    UUID getPublicationId();
}
