package com.smartsparrow.rtm.subscription.export;

import java.util.Objects;
import java.util.UUID;

import javax.inject.Inject;

import com.google.inject.assistedinject.Assisted;
import com.smartsparrow.rtm.subscription.data.AbstractRTMSubscription;
import com.smartsparrow.rtm.subscription.data.RTMSubscription;

public class ExportEventRTMSubscription extends AbstractRTMSubscription {

    private static final long serialVersionUID = 7582603163221747972L;

    public interface ExportEventRTMSubscriptionFactory {
        /**
         * Create a new instance of ExportEventRTMSubscription with a given exportId
         *
         * @param exportId the exportId
         * @return the ExportEventRTMSubscription created instance
         */
        ExportEventRTMSubscription create(final UUID exportId);
    }

    /**
     * Provides the name of the ExportEventRTMSubscription
     *
     * @param exportId the export id
     * @return the subscription name
     */
    public static String NAME(final UUID exportId) {
        return String.format("author.activity.export/%s", exportId);
    }

    private UUID exportId;

    @Inject
    public ExportEventRTMSubscription(@Assisted final UUID exportId) {
        this.exportId = exportId;
    }

    @Override
    public Class<? extends RTMSubscription> getSubscriptionType() {
        return ExportEventRTMSubscription.class;
    }

    public UUID getExportId() {
        return exportId;
    }

    @Override
    public String getName() {
        return NAME(exportId);
    }

    @Override
    public String getBroadcastType() {
        return "author.export.broadcast";
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ExportEventRTMSubscription that = (ExportEventRTMSubscription) o;
        return Objects.equals(exportId, that.exportId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(exportId);
    }

    @Override
    public String toString() {
        return "ExportEventRTMSubscription{" +
                "exportId=" + exportId +
                '}';
    }
}
