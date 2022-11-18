package com.smartsparrow.rtm.message.recv.diffsync;

import java.util.UUID;

import com.smartsparrow.rtm.message.MessageType;
import data.EntityType;

public interface DiffSyncMessage extends MessageType {

    EntityType getEntityType();

    UUID getEntityId();

}
