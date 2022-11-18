package com.smartsparrow.workspace.service;

import java.io.UnsupportedEncodingException;
import java.util.UUID;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.pearson.autobahn.common.exception.AutobahnIdentityProviderException;
import com.smartsparrow.ext_http.service.ExternalHttpRequestService;
import com.smartsparrow.ext_http.service.RequestNotification;
import com.smartsparrow.ext_http.service.RequestPurpose;
import com.smartsparrow.iam.wiring.IesSystemToSystemIdentityProvider;
import com.smartsparrow.learner.outcome.PublishMetadataRequestBuilder;
import com.smartsparrow.util.UUIDs;
import com.smartsparrow.util.log.MercuryLogger;
import com.smartsparrow.util.log.MercuryLoggerFactory;
import com.smartsparrow.workspace.data.PublicationSettings;

import reactor.core.publisher.Mono;

@Singleton
public class PublishMetadataService {

    private static final MercuryLogger log = MercuryLoggerFactory.getLogger(PublishMetadataService.class);

    // todo create config for MX Lab Details service URL
    private final ExternalHttpRequestService httpRequestService;
    private final IesSystemToSystemIdentityProvider iesSystemToSystemIdentityProvider;

    @Inject
    public PublishMetadataService(final ExternalHttpRequestService httpRequestService,
                                  final IesSystemToSystemIdentityProvider iesSystemToSystemIdentityProvider) {
        this.httpRequestService = httpRequestService;
        this.iesSystemToSystemIdentityProvider = iesSystemToSystemIdentityProvider;
    }

    public Mono<RequestNotification> publish(String mxServiceUrl,
                                             PublicationSettings publicationSettings) {
        final UUID referenceId = UUIDs.timeBased();
        // prepare builder with common fields
        PublishMetadataRequestBuilder reqBuilder = new PublishMetadataRequestBuilder()
                .setUri(mxServiceUrl)
                .setPublicationSettings(publicationSettings);
        try {
            reqBuilder.setPiToken(iesSystemToSystemIdentityProvider.getPiToken()); // set IES system token
        } catch (UnsupportedEncodingException | AutobahnIdentityProviderException ex) {
            throw new RuntimeException("PublishMetadata Service - Unable to set IES system token");
        }
        return Mono
                // build request
                .just(reqBuilder.build())
                // send it
                .flatMap(request -> httpRequestService.submit(RequestPurpose.PUBLISH_METADATA, request, referenceId))
                .doOnEach(log.reactiveDebugSignal("sent Lab Metadata to MX service"));
    }

}
