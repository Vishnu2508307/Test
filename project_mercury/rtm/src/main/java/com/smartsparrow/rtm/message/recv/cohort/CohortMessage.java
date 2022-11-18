package com.smartsparrow.rtm.message.recv.cohort;

import java.util.UUID;

import com.smartsparrow.rtm.message.MessageType;

public interface CohortMessage extends MessageType {

    UUID getCohortId();
}
