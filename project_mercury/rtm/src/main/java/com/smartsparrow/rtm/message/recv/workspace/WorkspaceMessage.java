package com.smartsparrow.rtm.message.recv.workspace;

import java.util.UUID;

import com.smartsparrow.rtm.message.MessageType;

public interface WorkspaceMessage extends MessageType {

    UUID getWorkspaceId();

}
