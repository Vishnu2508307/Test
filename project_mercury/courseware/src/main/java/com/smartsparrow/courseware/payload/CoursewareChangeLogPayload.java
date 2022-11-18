package com.smartsparrow.courseware.payload;

import com.smartsparrow.courseware.data.CoursewareChangeLog;

public interface CoursewareChangeLogPayload extends CoursewareChangeLog {

    String getCreatedAt();

    String getGivenName();

    String getFamilyName();

     String getPrimaryEmail();

     String getAvatarSmall();
}
