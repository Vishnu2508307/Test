package com.smartsparrow.sso.service;

import com.smartsparrow.iam.lang.UnauthorizedFault;
import com.smartsparrow.sso.event.RegistrarSectionRoleGetEventMessage;
import com.smartsparrow.util.log.MercuryLogger;
import com.smartsparrow.util.log.MercuryLoggerFactory;
import org.apache.camel.component.reactive.streams.api.CamelReactiveStreamsService;
import reactor.core.publisher.Mono;

import javax.annotation.Nonnull;
import javax.inject.Inject;
import javax.inject.Singleton;

import static com.smartsparrow.dataevent.RouteUri.REGISTRAR_SECTION_ROLE_GET;
import static com.smartsparrow.util.Warrants.affirmArgumentNotNullOrEmpty;

@Singleton
public class RegistrarService {
    private static final MercuryLogger log = MercuryLoggerFactory.getLogger(RegistrarService.class);

    private final CamelReactiveStreamsService camelReactiveStreamsService;

    @Inject
    public RegistrarService(final CamelReactiveStreamsService camelReactiveStreamsService) {
        this.camelReactiveStreamsService = camelReactiveStreamsService;
    }

    /**
     * Send an event to the {@link com.smartsparrow.dataevent.RouteUri#REGISTRAR_SECTION_ROLE_GET} route which instructs
     * camel to perform an external http request to the Registrar service to fetch the user role for this section.
     *
     * @param pearsonUid the userId to find the role for
     * @param pearsonSectionId the sectionId to find the role for
     * @param accessToken a valid access token
     * @return a mono with the identity profile when found
     * @throws UnauthorizedFault when the the request failed for any reason
     */
    public Mono<SectionRole> getSectionRole(@Nonnull final String pearsonUid,
                                            @Nonnull final String pearsonSectionId,
                                            @Nonnull final String accessToken) {
        affirmArgumentNotNullOrEmpty(pearsonUid, "pearsonUid is required");
        affirmArgumentNotNullOrEmpty(pearsonUid, "pearsonSectionId is required");
        affirmArgumentNotNullOrEmpty(accessToken, "accessToken is required");
        return Mono.just(new RegistrarSectionRoleGetEventMessage(pearsonUid, pearsonSectionId, accessToken)) //
                .doOnEach(log.reactiveInfo("handling registrar section role get"))
                .doOnEach(log.reactiveErrorThrowable("error while fetching registrar user section role"))
                .map(event -> camelReactiveStreamsService.toStream(REGISTRAR_SECTION_ROLE_GET, event, RegistrarSectionRoleGetEventMessage.class)) //
                .flatMap(Mono::from)
                .map(RegistrarSectionRoleGetEventMessage -> {
                    final SectionRole role = RegistrarSectionRoleGetEventMessage.getSectionRole();
                    if (role != null) {
                        return role;
                    }
                    throw new UnauthorizedFault("Invalid token supplied");
                });
    }
}
