package com.smartsparrow.iam.service;

/**
 * Define the sources of account provisioning.
 */
public enum AccountProvisionSource {

    // via a "Public" lesson activation
    ACTIVATION,

    // via some form of Collaboration
    COLLABORATION,

    // Enrolled by an instructor
    ENROLLMENT,

    // Enrolled on their own via self-enrolled class.
    ENROLLMENT_SELF,

    // Signup via the main signup flow
    SIGNUP,

    // via LTI
    LTI,

    // via AICC
    AICC,

    // via SSO OIDC
    OIDC,

    // via a Network such as BEST. Use a shadow attribute to record which
    NETWORK,

    // for when it can not be determined.
    LEGACY_UNKNOWN,

    //via web socket
    RTM

}
