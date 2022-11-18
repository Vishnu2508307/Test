package com.smartsparrow.rtm.message.recv.learner;

import com.smartsparrow.rtm.message.MessageType;

import java.util.UUID;


public interface LearnerAnnotationMessage extends MessageType {

    UUID getAnnotationId();
}
