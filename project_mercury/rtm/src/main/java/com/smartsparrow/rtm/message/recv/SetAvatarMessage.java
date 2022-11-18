package com.smartsparrow.rtm.message.recv;

import java.util.Objects;

import com.smartsparrow.rtm.message.ReceivedMessage;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

@SuppressFBWarnings(value = "UWF_UNWRITTEN_FIELD", justification = "Hydrated via Jackson on message deserialization")
public class SetAvatarMessage extends ReceivedMessage {

    private String avatar;

    public String getAvatar() {
        return avatar;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SetAvatarMessage that = (SetAvatarMessage) o;
        return Objects.equals(avatar, that.avatar);
    }

    @Override
    public int hashCode() {

        return Objects.hash(avatar);
    }

    @Override
    public String toString() {
        return "SetAvatarMessage{" +
                "avatar='" + avatar + '\'' +
                "} " + super.toString();
    }
}
