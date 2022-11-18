package com.smartsparrow.rtm.message.recv.iam;

import java.util.UUID;

import com.smartsparrow.rtm.message.MessageType;

public interface SubscriptionMessage extends MessageType {

    UUID getSubscriptionId();
}
