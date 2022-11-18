package com.smartsparrow.ext_http.service;

/**
 * Provides a well-defined type for the type of the request.
 */
public enum RequestPurpose {
    GENERAL,
    CSG_INDEX,
    CSG_DELETE,
    ALFRESCO_ASSET_PUSH,
    ALFRESCO_ASSET_PULL,
    GRADE_PASSBACK,
    PUBLISH_METADATA
}
