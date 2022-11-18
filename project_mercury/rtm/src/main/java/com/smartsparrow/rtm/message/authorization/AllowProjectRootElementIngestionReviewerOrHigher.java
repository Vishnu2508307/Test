package com.smartsparrow.rtm.message.authorization;

import javax.inject.Inject;

import com.smartsparrow.iam.service.PermissionLevel;
import com.smartsparrow.rtm.message.AuthorizationPredicate;
import com.smartsparrow.rtm.message.recv.ingestion.IngestionRootElementMessage;

public class AllowProjectRootElementIngestionReviewerOrHigher extends ProjectIngestionRootElementAuthorizer
        implements AuthorizationPredicate<IngestionRootElementMessage> {

    @Inject
    public AllowProjectRootElementIngestionReviewerOrHigher(final ProjectIngestionRootElementAuthorizerService projectIngestionRootElementAuthorizerService) {
        super(projectIngestionRootElementAuthorizerService);
    }


    @Override
    public PermissionLevel getAllowedPermissionLevel() {
        return PermissionLevel.REVIEWER;
    }
}
