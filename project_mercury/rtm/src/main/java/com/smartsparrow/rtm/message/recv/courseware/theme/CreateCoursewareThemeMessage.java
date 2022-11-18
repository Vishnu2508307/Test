package com.smartsparrow.rtm.message.recv.courseware.theme;

import java.util.Objects;
import java.util.UUID;

import com.smartsparrow.rtm.message.ReceivedMessage;
import com.smartsparrow.rtm.message.recv.workspace.WorkspaceMessage;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

@SuppressFBWarnings(value = "UWF_UNWRITTEN_FIELD", justification = "Hydrated via Jackson on message deserialization")
public class CreateCoursewareThemeMessage extends ReceivedMessage implements WorkspaceMessage {

    private UUID workspaceId;
    private String name;

    @Override
    public UUID getWorkspaceId() {
        return workspaceId;
    }

    public String getName() {
        return name;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CreateCoursewareThemeMessage that = (CreateCoursewareThemeMessage) o;
        return Objects.equals(name, that.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name);
    }

    @Override
    public String toString() {
        return "CreateCoursewareThemeMessage{" +
                "  name='" + name + '\'' +
                '}';
    }
}
