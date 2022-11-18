package com.smartsparrow.workspace.cms;

import static com.smartsparrow.data.Headers.BRONTE_COURSE_ID_HEADER;
import static com.smartsparrow.data.Headers.BRONTE_ASSET_ID_HEADER;
import static com.smartsparrow.data.Headers.ALFRESCO_AZURE_AUTHORIZATION_HEADER;

import javax.ws.rs.HttpMethod;

import com.google.common.collect.ImmutableMap;
import com.smartsparrow.ext_http.service.Request;

/**
 * Builds an ext_http type Request object that conforms to Alfresco Push request format.
 */
public class AlfrescoAssetPushRequestBuilder {
    String uri;
    String myCloudToken;
    String remoteAssetUri;
    String fileName;
    String courseId;
    String assetId;
    boolean autoRename;
    String workURN;
    String altText;
    String longDesc;

    public Request build() {
        return new Request()
                .setUri(uri)
                .setMethod(HttpMethod.POST)  //
                .addField("headers", ImmutableMap.of( //
                        BRONTE_COURSE_ID_HEADER, courseId, //
                        BRONTE_ASSET_ID_HEADER, assetId, //
                        ALFRESCO_AZURE_AUTHORIZATION_HEADER, myCloudToken //
                )) //
                .addField("formData", ImmutableMap.builder()
                        .put("name", fileName)
                        .put("nodeType", "cm:content")
                        .put("autoRename", String.valueOf(autoRename))
                        .put("include", "path")
                        .put("filedata", "{}")
                        .put("cplg:altText", altText)
                        .put("cplg:longDescription", longDesc)
                        .put("cp:workURN", workURN)
                        .build()
                )
                .addField("remoteDataOpts", ImmutableMap.of( //
                        "uri", remoteAssetUri, //
                        "method", HttpMethod.GET, //
                        "formDataField", "filedata" //
                ));
    }

    public AlfrescoAssetPushRequestBuilder setUri(String uri) {
        this.uri = uri;
        return this;
    }

    public AlfrescoAssetPushRequestBuilder setMyCloudToken(String myCloudToken) {
        this.myCloudToken = myCloudToken;
        return this;
    }

    public AlfrescoAssetPushRequestBuilder setRemoteAssetUri(String remoteAssetUri) {
        this.remoteAssetUri = remoteAssetUri;
        return this;
    }

    public AlfrescoAssetPushRequestBuilder setFileName(String fileName) {
        this.fileName = fileName;
        return this;
    }

    public AlfrescoAssetPushRequestBuilder setCourseId(String courseId) {
        this.courseId = courseId;
        return this;
    }

    public AlfrescoAssetPushRequestBuilder setAssetId(String assetId) {
        this.assetId = assetId;
        return this;
    }

    public AlfrescoAssetPushRequestBuilder setAutoRename(boolean autoRename) {
        this.autoRename = autoRename;
        return this;
    }

    public AlfrescoAssetPushRequestBuilder setWorkURN(String workURN) {
        this.workURN = workURN;
        return this;
    }

    public AlfrescoAssetPushRequestBuilder setAltText(String altText) {
        this.altText = altText;
        return this;
    }

    public AlfrescoAssetPushRequestBuilder setLongDesc(String longDesc) {
        this.longDesc = longDesc;
        return this;
    }
}
