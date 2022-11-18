package com.smartsparrow.rtm.message.recv.team;

import java.util.UUID;

import com.smartsparrow.rtm.message.MessageType;

public interface TeamMessage extends MessageType {

    UUID getTeamId();
}
