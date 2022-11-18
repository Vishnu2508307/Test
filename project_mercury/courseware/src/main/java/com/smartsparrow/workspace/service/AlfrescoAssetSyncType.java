package com.smartsparrow.workspace.service;

public enum AlfrescoAssetSyncType {
    // push new, unlinked Bronte asset to Alfresco.
    PUSH,

    // pull latest changes from linked Alfresco asset to Bronte.
    PULL;
}
