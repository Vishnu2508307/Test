package com.smartsparrow.rtm.message.recv.ingestion;

import java.util.UUID;

import com.smartsparrow.rtm.message.MessageType;

public interface IngestionRootElementMessage extends MessageType {

    UUID getRootElementId();
    UUID getProjectId();
}
