package com.smartsparrow.iam.collaborator;

import com.smartsparrow.iam.service.PermissionLevel;

public interface Collaborator {

    PermissionLevel getPermissionLevel();
}
