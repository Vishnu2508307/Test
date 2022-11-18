package com.smartsparrow.rtm.message.recv.asset;

import com.smartsparrow.rtm.message.recv.courseware.activity.ActivityGenericMessage;
import com.smartsparrow.workspace.service.AlfrescoAssetSyncType;

import java.util.Objects;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

@SuppressFBWarnings(value = "UWF_UNWRITTEN_FIELD", justification = "Hydrated via Jackson on message deserialization")
public class AlfrescoAssetsProgressMessage extends ActivityGenericMessage {

    private AlfrescoAssetSyncType syncType;

    public AlfrescoAssetSyncType getSyncType() {
        return syncType;
    }

    public AlfrescoAssetsProgressMessage setSyncType(AlfrescoAssetSyncType syncType) {
        this.syncType = syncType;
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        AlfrescoAssetsProgressMessage that = (AlfrescoAssetsProgressMessage) o;
        return syncType == that.syncType;
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), syncType);
    }

    @Override
    public String toString() {
        return "AlfrescoAssetsProgressMessage{" +
                "syncType=" + syncType +
                "} " + super.toString();
    }
}
