package com.smartsparrow.learner.outcome;

import static com.smartsparrow.data.Headers.PI_AUTHORIZATION_HEADER;

import java.util.HashMap;

import javax.ws.rs.HttpMethod;

import com.google.common.collect.ImmutableMap;
import com.smartsparrow.ext_http.service.Request;
import com.smartsparrow.util.log.MercuryLogger;
import com.smartsparrow.util.log.MercuryLoggerFactory;
import com.smartsparrow.workspace.data.PublicationSettings;

public class PublishMetadataRequestBuilder {

    private static final MercuryLogger log = MercuryLoggerFactory.getLogger(PublishMetadataRequestBuilder.class);

    String uri;
    String piToken;
    PublicationSettings publicationSettings;

    public Request build() {
        String requestBody = publicationSettings.toString();

        //log info the requestBody
        log.jsonInfo("Publish Lab meta data request to MX", new HashMap<String, Object>() {
            {
                put("requestBody", requestBody);
            }
        });


        return new Request()
                .setUri(uri)
                .setMethod(HttpMethod.POST)  //
                .addField("headers", ImmutableMap.of( //
                                                      PI_AUTHORIZATION_HEADER, piToken, //
                                                      "Content-Type", "application/json" //
                )) //
                .addField("body", requestBody);
    }

    public PublishMetadataRequestBuilder setUri(final String uri) {
        this.uri = uri;
        return this;
    }

    public PublishMetadataRequestBuilder setPiToken(final String piToken) {
        this.piToken = piToken;
        return this;
    }

    public PublishMetadataRequestBuilder setPublicationSettings(final PublicationSettings publicationSettings) {
        this.publicationSettings = publicationSettings;
        return this;
    }


}
