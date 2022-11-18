package com.smartsparrow.rtm.message.recv.learner.annotation;

import java.util.UUID;

import com.smartsparrow.rtm.message.recv.learner.annotation.DeploymentMessage;

public interface CreatorMessage extends DeploymentMessage {

    UUID getCreatorAccountId();
}
