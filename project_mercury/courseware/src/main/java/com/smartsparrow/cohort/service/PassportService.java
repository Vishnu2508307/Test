package com.smartsparrow.cohort.service;

import static com.smartsparrow.dataevent.RouteUri.PASSPORT_ENTITLEMENT_CHECK;
import static com.smartsparrow.util.Warrants.affirmArgumentNotNullOrEmpty;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.apache.camel.component.reactive.streams.api.CamelReactiveStreamsService;

import com.newrelic.api.agent.Trace;
import com.smartsparrow.cohort.eventmessage.PassportEntitlementEventMessage;
import com.smartsparrow.iam.exception.PermissionFault;
import com.smartsparrow.util.log.MercuryLogger;
import com.smartsparrow.util.log.MercuryLoggerFactory;
import com.smartsparrow.util.monitoring.ReactiveTransaction;

import reactor.core.publisher.Mono;

@Singleton
public class PassportService {

    private static final MercuryLogger log = MercuryLoggerFactory.getLogger(PassportService.class);

    private final CamelReactiveStreamsService camelReactiveStreamsService;

    @Inject
    public PassportService(CamelReactiveStreamsService camelReactiveStreamsService) {
        this.camelReactiveStreamsService = camelReactiveStreamsService;
    }

    /**
     * Check that the user is entitled to access a product
     *
     * @param pearsonUid the user to check the entitlement for
     * @param productURN the product the user is trying to access
     * @return a Mono of <code>true</code> when the user is entitled to access the product
     * @throws PermissionFault when the user is not entitled to access the product
     */
    @Trace(async = true)
    public Mono<Boolean> checkEntitlement(final String pearsonUid, final String productURN) {
        affirmArgumentNotNullOrEmpty(pearsonUid, "pearsonUid is required");
        affirmArgumentNotNullOrEmpty(productURN, "productURN is required");
        return Mono.just(new PassportEntitlementEventMessage(pearsonUid, productURN)) //
                .doOnEach(log.reactiveInfoSignal("handling passport entitlement check"))
                .map(event -> camelReactiveStreamsService.toStream(PASSPORT_ENTITLEMENT_CHECK, event, PassportEntitlementEventMessage.class)) //
                .flatMap(Mono::from)
                .doOnEach(log.reactiveInfoSignal("passport entitlement check handled"))
                .doOnEach(ReactiveTransaction.linkOnNext())
                .map(passportEntitlementEventMessage -> {
                    Boolean isValid = passportEntitlementEventMessage.getHasAccess();
                    if (isValid) {
                        return true;
                    }
                    throw new PermissionFault("not entitled");
                });
    }
}
