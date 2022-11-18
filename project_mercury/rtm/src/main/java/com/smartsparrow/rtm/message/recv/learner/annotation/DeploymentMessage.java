package com.smartsparrow.rtm.message.recv.learner.annotation;

import com.smartsparrow.rtm.message.MessageType;

import java.util.UUID;


public interface DeploymentMessage extends MessageType {

    UUID getDeploymentId();

}
