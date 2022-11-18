package com.smartsparrow.rtm.message.recv.ingestion;

import java.util.UUID;

import com.smartsparrow.rtm.message.MessageType;

public interface IngestionMessage extends MessageType {

    UUID getIngestionId();
}
