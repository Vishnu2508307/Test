package com.smartsparrow.iam.service;

/**
 * Define the names of the attributes.
 */
public enum AccountShadowAttributeName {

    /*
     * alpha order please :)
     */

    // This value is not used anymore. Once https://jira.smartsparrow.com/browse/PLT-5503 is completed all the records
    // in the db with this shadow attribute name should be removed. After that this value should be deleted
    @Deprecated
    AERO_ACCESS,

    // A two-character code representing the continent associated with the IP address.
    // Possible codes are: AF - Africa, AS - Asia, EU - Europe, NA - North America, OC - Oceania, SA - South America, AN - Antartica.
    GEO_CONTINENT_CODE,

    // A two-character ISO 3166-1 country code for the country associated with the IP address.
    GEO_COUNTRY_CODE,

    // the organization or university they attend
    INSTITUTION,

    // IDs in other systems
    ID_ZOHO,

    // Legacy attributes that aren't directly mapped to IAM structures.
    LEGACY_CREATED_TS_EPOCH_MS,
    LEGACY_DELETED_TS_EPOCH_MS,
    LEGACY_DISCIPLINE,
    LEGACY_EMAIL_VALIDATED_TS_EPOCH_MS,
    LEGACY_INSTITUTION_TYPE,
    LEGACY_JOB_ROLE,
    LEGACY_SUBJECT,

    // the LMS server's DNS (tool_consumer_instance_guid)
    LMS_HOSTNAME,

    //
    PROVISION_SOURCE,
    PROVISION_URL,

    // Signup Goal, Offering, etc. fields.
    SIGNUP_GOAL,
    SIGNUP_NETWORK,
    SIGNUP_OFFERING,
    SIGNUP_SUBJECT_AREA,

    // the university course code, CIS101 or BIO101
    UNIVERSITY_COURSE_CODE,

    // the university course name, Introduction to Biology
    UNIVERSITY_COURSE_NAME,

    // This entry describes whether the account has access to the new improved evaluation service
    // see LearnerWalkableService.evaluate(...) method
    REACTIVE_EVALUATION
}
