package com.smartsparrow.rtm.message.recv.team;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.smartsparrow.iam.service.PermissionLevel;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

@SuppressFBWarnings(value = "UWF_UNWRITTEN_FIELD", justification = "Hydrated via Jackson on message deserialization")
public class RevokeTeamPermissionMessage extends TeamPermissionMessage {

    @JsonIgnore
    @Override
    public PermissionLevel getPermissionLevel() {
        return null;
    }
}
