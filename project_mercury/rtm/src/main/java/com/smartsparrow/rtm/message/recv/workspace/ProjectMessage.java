package com.smartsparrow.rtm.message.recv.workspace;

import java.util.UUID;

import com.smartsparrow.rtm.message.MessageType;

public interface ProjectMessage extends MessageType {

    UUID getProjectId();
}
