package com.smartsparrow.rtm.message.recv.courseware;

import java.util.UUID;

import com.smartsparrow.courseware.data.CoursewareElementType;
import com.smartsparrow.rtm.message.MessageType;

/**
 * This interface is aimed to provide elementId and elementType to {@link com.smartsparrow.rtm.message.authorization.CoursewareElementAuthorizer}
 */
public interface CoursewareElementMessage extends MessageType {

    UUID getElementId();

    CoursewareElementType getElementType();

}
