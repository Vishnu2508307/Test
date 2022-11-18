package com.smartsparrow.iam.service;

/**
 * Define the source of the shadow attribute.
 */
public enum AccountShadowAttributeSource {

    // information derived from a LTI Request
    LTI,

    // legacy attributes
    LEGACY,

    // information derived from a HTTP Request
    REQUEST,

    // information derived from system operations
    SYSTEM

}
