package com.smartsparrow.courseware.eventmessage;

import java.util.Objects;

import com.smartsparrow.courseware.data.CoursewareChangeLog;
import com.smartsparrow.dataevent.BroadcastMessage;

public class CoursewareChangeLogBroadcastMessage implements BroadcastMessage {

    private static final long serialVersionUID = -6553580768161981517L;

    private CoursewareChangeLog coursewareChangeLog;

    public CoursewareChangeLog getCoursewareChangeLog() {
        return coursewareChangeLog;
    }

    public CoursewareChangeLogBroadcastMessage setCoursewareChangeLog(CoursewareChangeLog coursewareChangeLog) {
        this.coursewareChangeLog = coursewareChangeLog;
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CoursewareChangeLogBroadcastMessage that = (CoursewareChangeLogBroadcastMessage) o;
        return Objects.equals(coursewareChangeLog, that.coursewareChangeLog);
    }

    @Override
    public int hashCode() {
        return Objects.hash(coursewareChangeLog);
    }

    @Override
    public String toString() {
        return "CoursewareChangeLogBroadcastMessage{" +
                "coursewareChangeLog=" + coursewareChangeLog +
                '}';
    }
}
