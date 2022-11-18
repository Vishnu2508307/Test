package com.smartsparrow.workspace.cms;

import java.util.HashMap;
import java.util.Objects;
import static com.smartsparrow.data.Headers.ALFRESCO_AZURE_AUTHORIZATION_HEADER;
import static com.smartsparrow.data.Headers.BRONTE_ASSET_HEADER;

import javax.ws.rs.HttpMethod;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableMap;
import com.smartsparrow.exception.IllegalStateFault;
import com.smartsparrow.ext_http.service.Request;
import com.smartsparrow.util.log.MercuryLogger;
import com.smartsparrow.util.log.MercuryLoggerFactory;

public class AlfrescoAssetPullRequestBuilder {

    private static final MercuryLogger log = MercuryLoggerFactory.getLogger(AlfrescoAssetPullRequestBuilder.class);
    private static final ObjectMapper om = new ObjectMapper();


    private static final String QUERYSTRING_INCLUDE = "include=path";

    private String uri;
    private String myCloudToken;
    private AlfrescoAssetPullRequestMessage message;

    public Request build() {
        try {
            Request req = new Request();
            final String bronteAssetJson = om.writeValueAsString(message);
            log.jsonDebug("Alfresco asset sync", new HashMap<String, Object>() {
                {
                    put("referenceId", message.getReferenceId());
                }

                {
                    put("bronteAsset", bronteAssetJson);
                }
            });
            req.setUri(String.format("%s%s?%s", uri, message.getAlfrescoNodeId(), QUERYSTRING_INCLUDE));
            req.setMethod(HttpMethod.GET);
            req.addField("headers", ImmutableMap.of( //
                    BRONTE_ASSET_HEADER, bronteAssetJson,
                    ALFRESCO_AZURE_AUTHORIZATION_HEADER, myCloudToken
            ));
            return req;
        } catch (JsonProcessingException e) {
            log.jsonError("failed to build the alfresco asset pull request", new HashMap<String, Object>() {
                {
                    put("assetId", message.getAssetId().toString());
                }
            }, e);
            throw new IllegalStateFault("failed to build the alfresco asset pull request");
        }

    }

    public String getUri() {
        return uri;
    }

    public AlfrescoAssetPullRequestBuilder setUri(String uri) {
        this.uri = uri;
        return this;
    }

    public String getMyCloudToken() {
        return myCloudToken;
    }

    public AlfrescoAssetPullRequestBuilder setMyCloudToken(String myCloudToken) {
        this.myCloudToken = myCloudToken;
        return this;
    }

    public AlfrescoAssetPullRequestMessage getMessage() {
        return message;
    }

    public AlfrescoAssetPullRequestBuilder setMessage(AlfrescoAssetPullRequestMessage message) {
        this.message = message;
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AlfrescoAssetPullRequestBuilder that = (AlfrescoAssetPullRequestBuilder) o;
        return Objects.equals(uri, that.uri) &&
                Objects.equals(myCloudToken, that.myCloudToken) &&
                Objects.equals(message, that.message);
    }

    @Override
    public int hashCode() {
        return Objects.hash(uri, myCloudToken, message);
    }

    @Override
    public String toString() {
        return "AlfrescoAssetPullRequestBuilder{" +
                "uri='" + uri + '\'' +
                ", myCloudToken='" + myCloudToken + '\'' +
                ", message=" + message +
                '}';
    }
}
