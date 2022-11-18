package com.smartsparrow.sso.service;

import static com.google.common.base.Strings.isNullOrEmpty;
import static com.smartsparrow.sso.service.LTILaunchRequestLogEvent.Action.ERROR;
import static com.smartsparrow.sso.service.LTILaunchRequestLogEvent.Action.RECEIVED;
import static com.smartsparrow.sso.service.LTIParam.LTI_MESSAGE_TYPE;
import static com.smartsparrow.sso.service.LTIParam.LTI_VERSION;
import static com.smartsparrow.sso.service.LTIParam.OAUTH_CONSUMER_KEY;
import static com.smartsparrow.sso.service.LTIParam.RESOURCE_LINK_ID;
import static com.smartsparrow.sso.service.LTIParam.USER_ID;
import static com.smartsparrow.util.Warrants.affirmArgument;
import static com.smartsparrow.util.Warrants.affirmArgumentNotNullOrEmpty;
import static com.smartsparrow.util.Warrants.affirmNotNull;

import java.util.HashMap;
import java.util.UUID;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.collections4.map.HashedMap;

import com.smartsparrow.exception.IllegalArgumentFault;
import com.smartsparrow.iam.data.IESAccountTracking;
import com.smartsparrow.iam.lang.AuthenticationNotSupportedFault;
import com.smartsparrow.iam.service.AccountProvisionSource;
import com.smartsparrow.iam.service.AuthenticationService;
import com.smartsparrow.iam.service.AuthenticationType;
import com.smartsparrow.iam.service.CredentialService;
import com.smartsparrow.sso.data.ltiv11.LTIv11ConsumerConfiguration;
import com.smartsparrow.sso.lang.PIUserIdNotFoundException;
import com.smartsparrow.util.JWT;
import com.smartsparrow.util.log.MercuryLogger;
import com.smartsparrow.util.log.MercuryLoggerFactory;

import reactor.core.Exceptions;
import reactor.core.publisher.Mono;

@Singleton
public class LTI11AuthenticationService implements AuthenticationService<LTI11ConsumerCredentials, LTIWebSession> {

    private static final MercuryLogger log = MercuryLoggerFactory.getLogger(LTI11AuthenticationService.class);

    private final CredentialService credentialService;
    private final LTIv11Service ltIv11Service;
    private final IESService iesService;

    @Inject
    public LTI11AuthenticationService(final CredentialService credentialService,
                                      final LTIv11Service ltIv11Service,
                                      final IESService iesService) {
        this.credentialService = credentialService;
        this.ltIv11Service = ltIv11Service;
        this.iesService = iesService;
    }

    @Override
    public Mono<LTIWebSession> authenticate(LTI11ConsumerCredentials credentials) {
        // log a debug statement
        log.jsonDebug("received LTI launch request", new HashMap<String, Object>(){
            {put("url", credentials.getUrl());}
        });

        // first, invalidate any previously supplied tokens.
        if (!isNullOrEmpty(credentials.getInvalidateBearerToken())) {
            credentialService.invalidate(credentials.getInvalidateBearerToken());
        }

        affirmArgumentNotNullOrEmpty(credentials.getUrl(), "missing launch url");
        // make sure the message is valid
        affirmNotNull(credentials.getLtiMessage(), "missing launch request message");
        affirmArgument(!credentials.getLtiMessage().getParams().isEmpty(), "missing launch request parameters");
        assertRequiredLTIParams(credentials.getLtiMessage());

        // record the launch request
        Mono<UUID> launchRequestRecordMono = ltIv11Service.recordLaunchRequest(credentials);
        // lookup the LTI consumer configuration
        Mono<LTIv11ConsumerConfiguration> consumerConfigurationMono = ltIv11Service
                .findLTIConsumerConfiguration(credentials.getWorkspaceId());

        return Mono.zip(launchRequestRecordMono, consumerConfigurationMono)
                // update the launch status to received
                .flatMap(tuple2 -> ltIv11Service.updateLaunchRequestStatus(tuple2.getT1(), RECEIVED, "received")
                        .then(Mono.just(tuple2)))
                .flatMap(tuple2 -> {
                    final UUID launchRequestId = tuple2.getT1();
                    final LTIv11ConsumerConfiguration consumerConfiguration = tuple2.getT2();
                    // make sure the message signature is valid before processing
                    ltIv11Service.assertValidSignature(credentials);
                    // lookup the bronte account for this credentials via consumer configuration id and the lti user_id
                    return ltIv11Service.findAccountByLTIConfiguration(consumerConfiguration.getId(), credentials.getLtiMessage().get(USER_ID))
                            .switchIfEmpty(Mono.defer(() -> {
                                // when the account is not found, try provisioning a new account using the piUserId
                                final String piUserId = extractUserId(credentials.getPiToken());
                                if (piUserId == null) {
                                    // there is no pi user id, there is nothing we can do, throw an exception
                                    throw new PIUserIdNotFoundException("could not provision the account, pi user id not found")
                                            .setUserId(credentials.getLtiMessage().get(USER_ID))
                                            .setLaunchRequestId(launchRequestId);
                                }

                                // we have a piUserId, try looking up the account by that
                                return iesService.findAccount(piUserId)
                                        .switchIfEmpty(Mono.defer(() -> {
                                            // the account was not found so provision one
                                            return iesService.provisionAccount(piUserId, AccountProvisionSource.LTI,
                                                                               AuthenticationType.LTI);
                                        }))
                                        // we have an account, make sure to associate the provisioned account to this
                                        // lti consumer configuration and lti user_id so next time is easy to lookup
                                        .flatMap(account -> ltIv11Service.associateAccountIdToLTI(consumerConfiguration.getId(),
                                                credentials.getLtiMessage().get(USER_ID),
                                                account.getId())
                                                .then(Mono.just(account)));
                            }))
                            // An account was found, try creating an LTIWebSession with an ies token
                            .flatMap(account -> iesService.findIESId(account.getId())
                                    // find the ies user id for the found account
                                    .map(IESAccountTracking::getIesUserId)
                                    // if the ies user id was not found then we cannot proceed, throw
                                    .switchIfEmpty(Mono.error(new PIUserIdNotFoundException("could not authenticate the account, pi user id not found")
                                            .setUserId(credentials.getLtiMessage().get(USER_ID))
                                            .setLaunchRequestId(launchRequestId)))
                                    // create the web session token
                                    .flatMap(userId -> credentialService.createWebSessionToken(account.getId(), account.getSubscriptionId(), null)
                                            // map the websession token to an ies token
                                            .map(webSessionToken -> new LTIWebSession(account)
                                                    .setWebToken(new IESWebToken(credentials.getPiToken())
                                                            .setPearsonUid(userId)
                                                            .setValidUntilTs(webSessionToken.getValidUntilTs())))))
                            // create a log statement if anything goes wrong
                            .doOnEach(log.reactiveErrorThrowable("error authenticating LTI request", throwable -> new HashMap<String, Object>(){
                                {put("launchRequestId", launchRequestId);}
                            }))
                            // update the launch request status if any error happens
                            .doOnError(throwable -> {
                                log.jsonInfo("launchRequestHandler - authenticate failed ", new HashedMap<String, Object>() {
                                    {
                                        put("error", throwable.getMessage());
                                    }
                                });
                                ltIv11Service.updateLaunchRequestStatus(launchRequestId, ERROR, throwable.getMessage())
                                        .subscribe();
                                throw Exceptions.propagate(throwable);
                            });
                });
    }

    @Override
    public Mono<LTIWebSession> authenticate(final String token) {
        throw new AuthenticationNotSupportedFault("LTI 1.1 consumer bearer token authentication not supported");
    }

    @Override
    public void authenticate(LTI11ConsumerCredentials credentials, HttpServletRequest req, HttpServletResponse res) {
        throw new AuthenticationNotSupportedFault("LTI 1.1 consumer servlet authentication not supported");
    }

    /**
     * Assert that a required parameter exists in the supplied LTI launch parameters
     *
     * @param message the lti message to check the params for
     * @param param the param to check
     * @throws IllegalArgumentFault when the param is missing from the message
     */
    private void assertRequiredParam(LTIMessage message, LTIParam param) {
        final String requiredParam = message.get(param);

        if (requiredParam == null || requiredParam.isEmpty()) {
            throw new IllegalArgumentFault(String.format("missing required field %s", param.getValue()));
        }
    }

    /**
     * Assert that the lti message contains the required parameters
     *
     * @param message the message to check the required params for
     * @throws IllegalArgumentFault when any of the required param is missing
     */
    private void assertRequiredLTIParams(final LTIMessage message) {
        assertRequiredParam(message, OAUTH_CONSUMER_KEY);
        assertRequiredParam(message, LTI_VERSION);
        assertRequiredParam(message, LTI_MESSAGE_TYPE);
        assertRequiredParam(message, RESOURCE_LINK_ID);
        assertRequiredParam(message, USER_ID);
    }

    /**
     * Extract the pi user id from a pi session cookie
     *
     * @param piToken the piToken to extract the pi user id from
     * @return the pi user id or null
     */
    private String extractUserId(final String piToken) {
        try {
            return JWT.getUserId(piToken);
        } catch (Exception e) {
            return null;
        }
    }
}
