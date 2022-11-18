package com.smartsparrow.rtm.message.recv;


import com.smartsparrow.annotation.service.Motivation;
import com.smartsparrow.rtm.message.recv.courseware.CoursewareElementMessage;


public interface CoursewareElementMotivationMessage extends CoursewareElementMessage {

    Motivation getMotivation();
}
