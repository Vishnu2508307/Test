package com.smartsparrow.rtm.message.authorization;

import javax.inject.Inject;

import com.smartsparrow.iam.service.PermissionLevel;
import com.smartsparrow.rtm.message.AuthorizationPredicate;
import com.smartsparrow.rtm.message.recv.ingestion.IngestionMessage;

public class AllowProjectIngestionContributorOrHigher extends ProjectIngestionAuthorizer
        implements AuthorizationPredicate<IngestionMessage> {

    @Inject
    public AllowProjectIngestionContributorOrHigher(final ProjectIngestionAuthorizerService projectIngestionAuthorizerService) {
        super(projectIngestionAuthorizerService);
    }
    
    @Override
    public PermissionLevel getAllowedPermissionLevel() {
        return PermissionLevel.CONTRIBUTOR;
    }
}
