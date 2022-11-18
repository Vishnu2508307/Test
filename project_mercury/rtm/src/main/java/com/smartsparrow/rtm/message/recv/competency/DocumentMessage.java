package com.smartsparrow.rtm.message.recv.competency;

import java.util.UUID;

import com.smartsparrow.rtm.message.MessageType;

public interface DocumentMessage extends MessageType {

    UUID getDocumentId();
}
