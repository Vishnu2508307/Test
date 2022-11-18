package com.smartsparrow.cohort.route;

import static com.smartsparrow.data.Headers.PI_AUTHORIZATION_HEADER;
import static com.smartsparrow.dataevent.RouteUri.DIRECT;
import static com.smartsparrow.dataevent.RouteUri.FIREHOSE;
import static com.smartsparrow.dataevent.RouteUri.PASSPORT_ENTITLEMENT_CHECK;
import static com.smartsparrow.dataevent.RouteUri.RS;

import javax.inject.Inject;

import org.apache.camel.Exchange;
import org.apache.camel.Message;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.http.common.HttpOperationFailedException;
import org.json.JSONObject;

import com.smartsparrow.cohort.eventmessage.PassportEntitlementEventMessage;
import com.smartsparrow.cohort.wiring.PassportConfig;
import com.smartsparrow.dataevent.data.HttpOperationFailedProcessor;
import com.smartsparrow.iam.wiring.IesSystemToSystemIdentityProvider;

public class CohortRoute extends RouteBuilder {

    public static final String PASSPORT_ENTITLEMENT_CHECK_EVENT_MESSAGE = "passport.entitlement.check.event.message";

    private final PassportConfig passportConfig;
    private final IesSystemToSystemIdentityProvider identityProvider;

    @Inject
    public CohortRoute(PassportConfig passportConfig,
                       IesSystemToSystemIdentityProvider identityProvider) {
        this.passportConfig = passportConfig;
        this.identityProvider = identityProvider;
    }

    @Override
    public void configure() {
        // Passport entitlement check
        from(RS + PASSPORT_ENTITLEMENT_CHECK)
                // set the route id
                .routeId(PASSPORT_ENTITLEMENT_CHECK_EVENT_MESSAGE)
                // set the event message property
                .setProperty(PASSPORT_ENTITLEMENT_CHECK_EVENT_MESSAGE, body())
                // add the token from the body to the header
                .process(exchange -> {
                    PassportEntitlementEventMessage message = exchange.getProperty(PASSPORT_ENTITLEMENT_CHECK_EVENT_MESSAGE, PassportEntitlementEventMessage.class);
                    Message out = exchange.getOut();
                    out.setHeader("userURN", String.format("x-urn:pi:%s", message.getPearsonUid()));
                    out.setHeader("productURN",  message.getProductURN());
                    out.setHeader("token", identityProvider.getPiToken());
                    exchange.setOut(out);
                })
                // set the token to the authorization header
                .setHeader(PI_AUTHORIZATION_HEADER, simple("${in.header.token}"))
                // set the request url
                .setHeader(Exchange.HTTP_URI, constant(passportConfig.getBaseUrl()))
                .setHeader(Exchange.HTTP_PATH, simple("/product-permissions/${in.header.userURN},${in.header.productURN}"))
                .wireTap(DIRECT + FIREHOSE)
                // handle the http failure via http failed processor
                .onException(HttpOperationFailedException.class)
                    .process(new HttpOperationFailedProcessor(exchange -> exchange.getProperty(PASSPORT_ENTITLEMENT_CHECK_EVENT_MESSAGE, PassportEntitlementEventMessage.class)))
                    .handled(true)
                    .stop()
                .end()
                // perform the http request
                .to("https:passportEntitlementCheck")
                // mark the event message as valid if the response is successful
                .process(exchange -> {
                    // TODO needs to be verified with an integration test, requires product id creation
                    PassportEntitlementEventMessage message = exchange.getProperty(PASSPORT_ENTITLEMENT_CHECK_EVENT_MESSAGE, PassportEntitlementEventMessage.class);
                    JSONObject json = new JSONObject(exchange.getIn().getBody(String.class));
                    if (json.has("access") && Boolean.TRUE.equals(json.getBoolean("access"))) {
                        // mark the message as valid
                        message.grantAccess();
                        exchange.getOut().setBody(message);
                    }
                    // simply return the message
                    exchange.getOut().setBody(message);
                });
    }
}
