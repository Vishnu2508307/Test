package com.smartsparrow.rtm.message.recv.iam;

import java.util.Objects;

import com.smartsparrow.rtm.message.ReceivedMessage;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

@SuppressFBWarnings(value = "UWF_UNWRITTEN_FIELD", justification = "Hydrated via Jackson on message deserialization")
public class SetPasswordMessage extends ReceivedMessage {

    private String oldPassword;
    private String newPassword;
    private String confirmNew;

    public String getOldPassword() {
        return oldPassword;
    }

    public String getNewPassword() {
        return newPassword;
    }

    public String getConfirmNew() {
        return confirmNew;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SetPasswordMessage that = (SetPasswordMessage) o;
        return Objects.equals(oldPassword, that.oldPassword) &&
                Objects.equals(newPassword, that.newPassword) &&
                Objects.equals(confirmNew, that.confirmNew);
    }

    @Override
    public int hashCode() {
        return Objects.hash(oldPassword, newPassword, confirmNew);
    }

    @Override
    public String toString() {
        return "SetPasswordMessage{" +
                "oldPassword='" + oldPassword + '\'' +
                ", newPassword='" + newPassword + '\'' +
                ", confirmNew='" + confirmNew + '\'' +
                "} " + super.toString();
    }
}
