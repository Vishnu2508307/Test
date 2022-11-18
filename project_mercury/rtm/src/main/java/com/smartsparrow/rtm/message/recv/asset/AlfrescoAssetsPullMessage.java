package com.smartsparrow.rtm.message.recv.asset;

import java.util.Objects;

import com.smartsparrow.rtm.message.recv.courseware.activity.ActivityGenericMessage;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

@SuppressFBWarnings(value = "UWF_UNWRITTEN_FIELD", justification = "Hydrated via Jackson on message deserialization")
public class AlfrescoAssetsPullMessage extends ActivityGenericMessage {

    @Deprecated
    private String token;

    private boolean forceAssetSync = false;

    @Deprecated
    public String getToken() {
        return token;
    }

    @Deprecated
    public AlfrescoAssetsPullMessage setToken(String token) {
        this.token = token;
        return this;
    }

    public boolean isForceAssetSync() {
        return forceAssetSync;
    }

    public AlfrescoAssetsPullMessage setForceAssetSync(boolean forceAssetSync) {
        this.forceAssetSync = forceAssetSync;
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        AlfrescoAssetsPullMessage that = (AlfrescoAssetsPullMessage) o;
        return forceAssetSync == that.forceAssetSync &&
                Objects.equals(token, that.token);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), token, forceAssetSync);
    }

    @Override
    public String toString() {
        return "SyncAlfrescoAssetsMessage{" +
                "token='" + token + '\'' +
                ", forceAssetSync=" + forceAssetSync +
                '}';
    }
}
