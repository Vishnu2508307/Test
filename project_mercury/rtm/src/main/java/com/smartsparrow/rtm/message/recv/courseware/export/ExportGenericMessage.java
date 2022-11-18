package com.smartsparrow.rtm.message.recv.courseware.export;

import java.util.Objects;
import java.util.UUID;

import com.smartsparrow.rtm.message.ReceivedMessage;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

@SuppressFBWarnings(value = "UWF_UNWRITTEN_FIELD", justification = "Hydrated via Jackson on message deserialization")
public class ExportGenericMessage extends ReceivedMessage {

    private UUID exportId;

    public UUID getExportId() {
        return exportId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ExportGenericMessage that = (ExportGenericMessage) o;
        return Objects.equals(exportId, that.exportId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(exportId);
    }

    @Override
    public String toString() {
        return "ExportGenericMessage{" +
                "exportId=" + exportId +
                "} " + super.toString();
    }
}
