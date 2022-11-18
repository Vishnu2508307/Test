package com.smartsparrow.learner.service;

import java.util.UUID;

import com.smartsparrow.learner.data.Deployment;

public class DeploymentDataStub {

    public static Deployment buildDeployment() {
        return new Deployment()
                .setId(UUID.randomUUID())
                .setCohortId(UUID.randomUUID())
                .setChangeId(UUID.randomUUID());
    }
}
