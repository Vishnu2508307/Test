package com.smartsparrow.rtm.subscription.competency;

import java.util.Objects;
import java.util.UUID;

import javax.inject.Inject;

import com.google.inject.assistedinject.Assisted;
import com.smartsparrow.rtm.subscription.data.AbstractRTMSubscription;
import com.smartsparrow.rtm.subscription.data.RTMSubscription;

public class CompetencyDocumentEventRTMSubscription extends AbstractRTMSubscription {

    private static final long serialVersionUID = -4550883255567451713L;

    public interface CompetencyDocumentEventRTMSubscriptionFactory {
        /**
         * Create a new instance of CompetencyDocumentEventRTMSubscription with a given documentId
         *
         * @param documentId the documentId
         * @return the workspaceRTMSubscription created instance
         */
        CompetencyDocumentEventRTMSubscription create(final UUID documentId);
    }

    private UUID documentId;

    @Inject
    public CompetencyDocumentEventRTMSubscription(@Assisted UUID documentId) {
        this.documentId = documentId;
    }

    /**
     * Provides the name of the PluginPermissionRTMSubscription
     *
     * @param documentId the workspace id this subscription is for
     * @return the subscription name
     */
    public static String NAME(final UUID documentId) {
        return String.format("competency.document/%s", documentId);
    }

    public UUID getDocumentId() {
        return documentId;
    }


    @Override
    public Class<? extends RTMSubscription> getSubscriptionType() {
        return CompetencyDocumentEventRTMSubscription.class;
    }

    @Override
    public String getName() {
        return NAME(documentId);
    }

    @Override
    public String getBroadcastType() {
        return "workspace.competency.document.broadcast";
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CompetencyDocumentEventRTMSubscription that = (CompetencyDocumentEventRTMSubscription) o;
        return Objects.equals(documentId, that.documentId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(documentId);
    }

    @Override
    public String toString() {
        return "CompetencyDocumentEventRTMSubscription{" +
                "documentId=" + documentId +
                '}';
    }
}
