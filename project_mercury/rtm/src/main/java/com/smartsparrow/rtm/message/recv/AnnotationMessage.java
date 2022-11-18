package com.smartsparrow.rtm.message.recv;

import com.smartsparrow.rtm.message.recv.courseware.CoursewareElementMessage;

import java.util.UUID;


public interface AnnotationMessage extends CoursewareElementMessage {

    UUID getAnnotationId();
}
