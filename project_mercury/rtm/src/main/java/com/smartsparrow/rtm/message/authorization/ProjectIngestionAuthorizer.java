package com.smartsparrow.rtm.message.authorization;

import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.smartsparrow.iam.service.AuthenticationContext;
import com.smartsparrow.iam.service.PermissionLevel;
import com.smartsparrow.rtm.message.AuthorizationPredicate;
import com.smartsparrow.rtm.message.recv.ingestion.IngestionMessage;

public abstract class ProjectIngestionAuthorizer implements AuthorizationPredicate<IngestionMessage> {

    private static final Logger log = LoggerFactory.getLogger(ProjectIngestionAuthorizer.class);

    private final ProjectIngestionAuthorizerService projectIngestionAuthorizerService;

    @Inject
    public ProjectIngestionAuthorizer(final ProjectIngestionAuthorizerService projectIngestionAuthorizerService) {
        this.projectIngestionAuthorizerService = projectIngestionAuthorizerService;
    }

    @Override
    public String getErrorMessage() {
        return "Unauthorized permission level";
    }

    public abstract PermissionLevel getAllowedPermissionLevel();

    @Override
    public boolean test(AuthenticationContext authenticationContext, IngestionMessage message) {
        return projectIngestionAuthorizerService.authorize(authenticationContext, message.getIngestionId(), getAllowedPermissionLevel());
    }

}
