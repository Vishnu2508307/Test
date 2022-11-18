package com.smartsparrow.rtm.message.authorization;

import static com.smartsparrow.util.Warrants.affirmArgument;

import java.util.UUID;

import javax.inject.Inject;

import com.smartsparrow.courseware.data.CoursewareElementType;
import com.smartsparrow.courseware.service.CoursewareService;
import com.smartsparrow.iam.service.Account;
import com.smartsparrow.iam.service.AuthenticationContext;
import com.smartsparrow.iam.service.PermissionLevel;
import com.smartsparrow.rtm.message.AuthorizationPredicate;
import com.smartsparrow.rtm.message.recv.courseware.pathway.PathwayMessage;
import com.smartsparrow.util.log.MercuryLogger;
import com.smartsparrow.util.log.MercuryLoggerFactory;
import com.smartsparrow.workspace.service.ProjectPermissionService;
import com.smartsparrow.workspace.service.WorkspaceService;

public class AllowPathwayContributorOrHigher implements AuthorizationPredicate<PathwayMessage> {

    private static final MercuryLogger log = MercuryLoggerFactory.getLogger(AllowPathwayContributorOrHigher.class);

    private final WorkspaceService workspaceService;
    private final CoursewareService coursewareService;
    private final ProjectPermissionService projectPermissionService;

    @Inject
    public AllowPathwayContributorOrHigher(final WorkspaceService workspaceService,
                                           final CoursewareService coursewareService,
                                           final ProjectPermissionService projectPermissionService) {
        this.workspaceService = workspaceService;
        this.coursewareService = coursewareService;
        this.projectPermissionService = projectPermissionService;
    }

    @Override
    public String getErrorMessage() {
        return "User does not have required permissions on the pathway";
    }

    @Override
    public boolean test(AuthenticationContext authenticationContext, PathwayMessage message) {
        Account account = authenticationContext.getAccount();

        affirmArgument(account != null, "User should be authenticated");

        try {
            UUID projectId = coursewareService.getProjectId(message.getPathwayId(),
                                                            CoursewareElementType.PATHWAY).block();
                if (projectId != null && account != null) {
                    PermissionLevel permission = projectPermissionService.findHighestPermissionLevel(account.getId(),
                                                                                                     projectId).block();
                    return permission != null
                            && permission.isEqualOrHigherThan(PermissionLevel.CONTRIBUTOR);
                }

            log.debug("Could not verify permission level, `projectId` or `accountId` can not be defined: " + message);
            return false;

        } catch (Exception ex) {
            log.debug("Exception while checking permissions for pathway", ex.getStackTrace());
            return false;
        }
    }
}
