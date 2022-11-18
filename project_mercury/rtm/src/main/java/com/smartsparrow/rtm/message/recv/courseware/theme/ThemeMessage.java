package com.smartsparrow.rtm.message.recv.courseware.theme;

import java.util.UUID;

import com.smartsparrow.rtm.message.MessageType;

public interface ThemeMessage extends MessageType {

    UUID getThemeId();

}
