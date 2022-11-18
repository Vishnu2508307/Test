package com.smartsparrow.iam.collaborator;

import java.util.UUID;

public interface AccountCollaborator extends Collaborator {

    UUID getAccountId();
}
