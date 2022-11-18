package com.smartsparrow.rtm.message.authorization;

import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.smartsparrow.iam.service.AuthenticationContext;
import com.smartsparrow.iam.service.PermissionLevel;
import com.smartsparrow.rtm.message.AuthorizationPredicate;
import com.smartsparrow.rtm.message.recv.ingestion.IngestionRootElementMessage;

public abstract class ProjectIngestionRootElementAuthorizer implements AuthorizationPredicate<IngestionRootElementMessage> {
    private static final Logger log = LoggerFactory.getLogger(ProjectIngestionRootElementAuthorizer.class);

    private final ProjectIngestionRootElementAuthorizerService projectIngestionRootElementAuthorizerService;

    @Inject
    public ProjectIngestionRootElementAuthorizer(final ProjectIngestionRootElementAuthorizerService projectIngestionRootElementAuthorizerService) {
        this.projectIngestionRootElementAuthorizerService = projectIngestionRootElementAuthorizerService;
    }

    @Override
    public String getErrorMessage() {
        return "Unauthorized permission level";
    }


    public abstract PermissionLevel getAllowedPermissionLevel();

    @Override
    public boolean test(AuthenticationContext authenticationContext, IngestionRootElementMessage message) {
        return projectIngestionRootElementAuthorizerService.authorize(authenticationContext, message.getRootElementId(), message.getProjectId(), getAllowedPermissionLevel());
    }
}
