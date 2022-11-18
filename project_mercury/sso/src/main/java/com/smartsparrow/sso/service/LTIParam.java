package com.smartsparrow.sso.service;

public enum LTIParam {
    CONTEXT_ID("context_id"),
    CONTEXT_LABEL("context_label"),
    LAUNCH_PRESENTATION_DOCUMENT_TARGET("launch_presentation_document_target"),
    LAUNCH_PRESENTATION_RETURN_URL("launch_presentation_return_url"),
    USER_ID("user_id"),
    USER_GIVEN_NAME("lis_person_name_given"),
    USER_FAMILY_NAME("lis_person_name_family"),
    USER_FULL_NAME("lis_person_name_full"),
    USER_EMAIL("lis_person_contact_email_primary"),
    LTI_VERSION("lti_version"),
    LTI_MESSAGE_TYPE("lti_message_type"),
    LTI_CUSTOM_PARAM_PREFIX("custom_"),
    RESOURCE_LINK_ID("resource_link_id"),
    OAUTH_CONSUMER_KEY("oauth_consumer_key"),
    OAUTH_NONCE("oauth_nonce"),
    OAUTH_SIGNATURE("oauth_signature"),
    OAUTH_SIGNATURE_METHOD("oauth_signature_method"),
    OAUTH_TIMESTAMP("oauth_timestamp"),
    OAUTH_VERSION("oauth_version"),
    USER_ROLES("roles");

    private String value;

    LTIParam(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}
