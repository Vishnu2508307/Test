package com.smartsparrow.iam.collaborator;

import java.util.UUID;

public interface TeamCollaborator extends Collaborator {

    UUID getTeamId();
}
