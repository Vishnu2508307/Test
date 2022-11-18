package com.smartsparrow.courseware.eventmessage;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.UUID;

import org.junit.jupiter.api.Test;

import com.datastax.driver.core.utils.UUIDs;

class ActivityEventMessageTest {

    @Test
    void buildChannelName_test() {

        UUID activityId = UUIDs.timeBased();
        String name = new ActivityEventMessage(activityId).getName();
        assertEquals("author.activity/" + activityId.toString(), name);

    }

}
