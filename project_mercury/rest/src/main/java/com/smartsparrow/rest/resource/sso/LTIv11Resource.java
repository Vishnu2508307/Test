package com.smartsparrow.rest.resource.sso;

import static com.smartsparrow.sso.service.LTIParam.OAUTH_CONSUMER_KEY;
import static com.smartsparrow.util.Warrants.affirmArgumentNotNullOrEmpty;
import static com.smartsparrow.util.Warrants.affirmNotNull;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import javax.annotation.Nonnull;
import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.CookieParam;
import javax.ws.rs.FormParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.CacheControl;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Cookie;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;

import org.apache.commons.collections4.map.HashedMap;
import org.apache.commons.lang.StringUtils;
import org.redisson.api.RBucketReactive;
import org.redisson.api.RedissonReactiveClient;

import com.google.inject.Provider;
import com.smartsparrow.cache.service.CacheService;
import com.smartsparrow.cohort.data.CohortSettings;
import com.smartsparrow.cohort.data.CohortSummary;
import com.smartsparrow.cohort.data.EnrollmentType;
import com.smartsparrow.cohort.service.CohortEnrollmentService;
import com.smartsparrow.cohort.service.CohortService;
import com.smartsparrow.cohort.wiring.LTIConfig;
import com.smartsparrow.exception.IllegalArgumentFault;
import com.smartsparrow.exception.IllegalStateFault;
import com.smartsparrow.iam.lang.UnauthorizedFault;
import com.smartsparrow.iam.service.AuthenticationService;
import com.smartsparrow.iam.service.MutableAuthenticationContext;
import com.smartsparrow.iam.service.WebTokenType;
import com.smartsparrow.learner.service.DeploymentService;
import com.smartsparrow.sso.data.ltiv11.LTI11LaunchSessionHash;
import com.smartsparrow.sso.data.ltiv11.LTIv11ConsumerKey;
import com.smartsparrow.sso.lang.PIUserIdNotFoundException;
import com.smartsparrow.sso.service.IESWebToken;
import com.smartsparrow.sso.service.LTI11ConsumerCredentials;
import com.smartsparrow.sso.service.LTIConsumerCredentials;
import com.smartsparrow.sso.service.LTILaunchRequestLogEvent;
import com.smartsparrow.sso.service.LTIMessage;
import com.smartsparrow.sso.service.LTIWebSession;
import com.smartsparrow.sso.service.LTIv11Service;
import com.smartsparrow.sso.service.SessionJsTemplateInitializer;
import com.smartsparrow.sso.wiring.LTIConsumerAuthentication;
import com.smartsparrow.util.UUIDs;
import com.smartsparrow.util.log.MercuryLogger;
import com.smartsparrow.util.log.MercuryLoggerFactory;

import io.reactivex.annotations.NonNull;
import io.reactivex.functions.Function3;
import reactor.core.publisher.Mono;

/**
 * A resource for LTI v1.1 POSTs. Overall, the handler is responsible for:
 * <p>
 * 1. Uses the supplied parameters and request to perform message validation.
 * 2. Performs optional account provisioning and authentication.
 * 3. Sets Authentication cookies.
 * 4. Issues a redirect back to the originally requested URL, i.e. not this handler.
 * <p>
 * LTI requests are first touched at the CDN edge. It uses these rules to determine if it is a LTI request:
 * https://www.imsglobal.org/wiki/step-1-lti-launch-request
 * <p>
 * If the request has been deemed a LTI launch request, the CDN rewrites the URL and passes it through to the services
 * in order to perform the necessary handling:
 * /sso/lti-1-1/launch-request?continue_to=https://learn.phx-spr.com/xxxx/yyyy
 * <p>
 * Note: This handler is not intended to be directly user facing, only routed to by other services.
 */
@Path("/lti-1-1")
public class LTIv11Resource {

    private static final MercuryLogger log = MercuryLoggerFactory.getLogger(LTIv11Resource.class);

    private final CohortService cohortService;
    private final AuthenticationService<LTIConsumerCredentials, LTIWebSession> authenticationService;
    private final LTIv11Service ltIv11Service;
    private final SessionJsTemplateInitializer sessionJsTemplateInitializer;
    private final CohortEnrollmentService cohortEnrollmentService;
    private final Provider<MutableAuthenticationContext> mutableAuthenticationContextProvider;
    private final DeploymentService deploymentService;
    private final LTIConfig ltiConfig;
    private final RedissonReactiveClient redissonReactiveClient;
    //
    private final static CacheControl CACHE_POLICY = new CacheControl();

    static {
        // See: https://developers.google.com/web/fundamentals/performance/optimizing-content-efficiency/http-caching
        // Based on the decision tree in the above doc, only no-store needs to be set
        CACHE_POLICY.setNoStore(true);
    }

    @Inject
    public LTIv11Resource(final CohortService cohortService,
                          @LTIConsumerAuthentication AuthenticationService<LTIConsumerCredentials, LTIWebSession> authenticationService,
                          final LTIv11Service ltIv11Service,
                          final SessionJsTemplateInitializer sessionJsTemplateInitializer,
                          final CohortEnrollmentService cohortEnrollmentService,
                          Provider<MutableAuthenticationContext> mutableAuthenticationContextProvider,
                          final DeploymentService deploymentService,
                          final LTIConfig ltiConfig,
                          final RedissonReactiveClient redissonReactiveClient) {
        this.cohortService = cohortService;
        this.authenticationService = authenticationService;
        this.ltIv11Service = ltIv11Service;
        this.sessionJsTemplateInitializer = sessionJsTemplateInitializer;
        this.cohortEnrollmentService = cohortEnrollmentService;
        this.mutableAuthenticationContextProvider = mutableAuthenticationContextProvider;
        this.deploymentService = deploymentService;
        this.ltiConfig = ltiConfig;
        this.redissonReactiveClient = redissonReactiveClient;
    }

    /**
     * General handler as described in the class javadoc above.
     *
     * @param httpHeaders               the Request http headers
     * @param bodyParams                the parameters from the POST body
     * @param originalContinueTo        the original requested URL which the user performed the POST to
     * @param existingBearerTokenCookie an existing bearer token
     * @return a redirect to paramContinueTo
     */
    @POST
    @Path("/launch-request")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    public Response launchRequestHandler(@Context HttpHeaders httpHeaders,
                                         MultivaluedMap<String, String> bodyParams,
                                         @QueryParam("continue_to") final String originalContinueTo,
                                         @CookieParam("bearerToken") final Cookie existingBearerTokenCookie,
                                         @CookieParam("PiAuthSession") final Cookie piAuthSession) {
        // validate continue_to param
        affirmArgumentNotNullOrEmpty(originalContinueTo, "missing parameter");
        URI continueTo;

        try {
            continueTo = new URI(originalContinueTo);

            // raise an exception if it does not end with .pearson.com
            if (!continueTo.getHost().endsWith(".pearson.com")) {
                throw new URISyntaxException("ignored", "ignored");
            }
        } catch (URISyntaxException e) {
            throw new IllegalArgumentFault("Invalid continue_to url parameter");
        }

        // a LTI message does not have "multi" valued parameters, convert to a std map.
        Map<String, String> ltiParams = bodyParams.entrySet().stream()
                .collect(Collectors.toMap(Map.Entry::getKey, //
                        entry -> entry.getValue().get(0), //
                        (left, right) -> {
                            throw new IllegalArgumentFault("duplicate parameter " + left);
                        }));

        // todo temporary logging during integration, remove after May 2022
        HashMap<String, Object> logLtiParams = new HashMap<>();
        logLtiParams.putAll(ltiParams);
        log.jsonInfo("launchRequestHandler - LTI params", logLtiParams);

        // build a LTI message.
        LTIMessage ltiMessage = new LTIMessage().putAll(ltiParams);

        String paramContinueTo = parseContinueTo(originalContinueTo, ltiParams);
        try {
            continueTo = new URI(paramContinueTo);

            // raise an exception if it does not end with .pearson.com
            if (!continueTo.getHost().endsWith(".pearson.com")) {
                throw new URISyntaxException("ignored", "ignored");
            }
        } catch (URISyntaxException e) {
            throw new IllegalArgumentFault("Invalid computed_continue_to url parameter");
        }

        // extract the previous bearer token to invalidate.
        // Note: an error handler should include cookie invalidation.
        String invalidateBearerToken = existingBearerTokenCookie == null ? null : existingBearerTokenCookie.getValue();

        // parse cohort id from target learnspace url
        UUID cohortId = parseCohortId(paramContinueTo);
        affirmNotNull(cohortId, "invalid cohort id");
        // get the workspaceId and add it to the credentials
        final CohortSummary cohort = cohortService.fetchCohortSummary(cohortId)
                .block();
        affirmNotNull(cohort, "invalid cohort id, cohort not found");
        // find the LTI credentials by cohort and consumer key
        String oauthConsumerKey = ltiMessage.get(OAUTH_CONSUMER_KEY);
        affirmArgumentNotNullOrEmpty(oauthConsumerKey, "missing oauth consumer key");
        final LTIv11ConsumerKey consumerKey = ltIv11Service.findConsumerKey(cohort.getWorkspaceId(), cohortId, oauthConsumerKey)
                .block();
//        LtiConsumerCredentialDetail consumerCredential = cohortService.fetchLtiConsumerCredential(cohortId, oauthConsumerKey).block();
        affirmNotNull(consumerKey, "signature_invalid, no credential found for consumer key provided and cohort id " + cohortId);
        String piTokenCookie = piAuthSession != null ? piAuthSession.getValue() : null;
        final String piToken =  piTokenCookie;
        // build the LTI consumer credentials for version 11
        MultivaluedMap<String, String> requestHeaders =httpHeaders != null ? httpHeaders.getRequestHeaders() : null;
        LTI11ConsumerCredentials credentials = new LTI11ConsumerCredentials(piToken)
                .setLtiMessage(ltiMessage)
                .setUrl(originalContinueTo)
                .setCohortId(cohortId)
                .setWorkspaceId(cohort.getWorkspaceId())
                .setKey(consumerKey.getOauthConsumerKey())
                .setSecret(consumerKey.getOauthConsumerSecret())
                .setLogDebug(consumerKey.isLogDebug())
                .setHttpHeaders(requestHeaders)
                .setInvalidateBearerToken(invalidateBearerToken);

        try {
            // try initialising the session, if it fails, kick the alternate flow of serving a page to init session.js
            // exchange this LTI credentials for an LTI WebSession And enroll user if not exists when valid piUserId
            URI finalContinueTo = continueTo;
            return authenticationService.authenticate(credentials)
                    .map(ltiWebSession -> {
                        mutableAuthenticationContextProvider.get().setAccount(ltiWebSession.getAccount());
                        return ltiWebSession;
                    })
                    .map(ltiWebSession -> {
                        String labId = paramContinueTo;
                        String deploymentId = labId.substring(labId.lastIndexOf('/') + 1);
                        //store lti values in cache using account ID
                        // store time 1 hrs
                        String cacheName = String.format("ltiParams:account:/%s:deploymentId:/%s", ltiWebSession.getAccount().getId(), deploymentId);

                        RBucketReactive<Map<String, String>> bucket = redissonReactiveClient.getBucket(cacheName);
                        bucket.set(ltiParams, 1l, TimeUnit.DAYS).block();
                        return ltiWebSession;
                    })
                    .flatMap(ltiWebSession -> {
                        // FIXME: this is a read before write which is an anti-pattern in cassandra
                        // the data model should be refactored so that it allows for always inserting an enrollment without checking
                        // currently this is required to keep the enrollment date accurate
                        return cohortEnrollmentService.findCohortEnrollment(ltiWebSession.getAccount().getId(), cohortId)
                                // if the cohort enrollment is not found, then enroll
                                .switchIfEmpty(Mono.defer(() -> {
                                    if (!ltiWebSession.getWebToken().getWebTokenType().equals(WebTokenType.IES)) {
                                        final String error = String.format("IES webtoken expected, `%s` found", ltiWebSession.getWebToken().getWebTokenType());
                                        IllegalStateFault fault = new IllegalStateFault(error);
                                        log.jsonError(error, new HashMap<String, Object>(){
                                            {put("accountId", ltiWebSession.getAccount().getId());}
                                        }, fault);
                                        throw fault;
                                    }
                                    // this is a safe cast since it will always be a
                                    IESWebToken token = (IESWebToken) ltiWebSession.getWebToken();

                                    return cohortEnrollmentService.enrollAccount(ltiWebSession.getAccount().getId(), cohortId, EnrollmentType.LTI, token.getPearsonUid())
                                            .doOnEach(log.reactiveErrorThrowable("launchRequestHandler - enrollment error", throwable -> new HashMap<String, Object>() {
                                                {
                                                    put("error", throwable.getMessage());
                                                }
                                            }));
                                }))
                                // return the web token
                                .then(Mono.just(ltiWebSession.getWebToken()))
                                .doOnEach(log.reactiveInfoSignal("return lti web session",
                                        ignored -> new HashedMap<String, Object>() {
                                            {
                                                put("ltiWebSession", ltiWebSession);
                                            }
                                        })
                                );
                    })
                    .flatMap(webToken -> Mono.just(Response.seeOther(finalContinueTo)
                                                           .cacheControl(CACHE_POLICY)
                                                           .build()))
                    .block();

        } catch (PIUserIdNotFoundException ex) {
            // the account could not be provisioned because we don't have a piToken yet
            // initialise session js so we can later provision the account

            // start by recording the session js init with an hash
            LTI11LaunchSessionHash sessionHash = ltIv11Service.createSessionHash(consumerKey.getConsumerConfigurationId(), cohortId, paramContinueTo,
                    ex.getUserId(), ex.getLaunchRequestId())
                    .block();

            log.jsonInfo("launchRequestHandler - exception ", new HashedMap<String, Object>() {
                {
                    put("sessionHash", sessionHash);
                    put("consumerConfigurationId", consumerKey.getConsumerConfigurationId());
                    put("cohortId", cohortId);
                    put("paramContinueTo", paramContinueTo);
                    put("userId", ex.getUserId());
                    put("launchRequestId", ex.getLaunchRequestId());
                }
            });

            affirmNotNull(sessionHash, "a session hash is required");

            //store lti parms under hash
            String cacheName = String.format("ltiParams:sessionHash:/%s", sessionHash.getHash());

            RBucketReactive<Map<String, String>> bucket = redissonReactiveClient.getBucket(cacheName);
            bucket.set(ltiParams, 1l, TimeUnit.DAYS).block();

            // respond by rendering a page that initialises session.js
            return Response.ok()
                    .entity(sessionJsTemplateInitializer.get(sessionHash.getHash(), sessionHash.getLaunchRequestId()))
                    .type("text/html")
                    .build();
        }
    }

    @POST
    @Path("/launch-request-continue")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    public Response launchRequestContinueHandler(@FormParam("hash") final String hash,
                                                 @FormParam("launchRequestId") final UUID launchRequestId,
                                                 @FormParam("piUserId") final String piUserId) {
        affirmNotNull(hash, "hash cannot be null");
        affirmNotNull(launchRequestId, "hash cannot be null");
        affirmNotNull(piUserId, new UnauthorizedFault("pi session failed to initialize"));

        // now that we have a piUserId, provision the account
        LTI11LaunchSessionHash sessionHash = ltIv11Service.provisionAccount(hash, launchRequestId, piUserId, enrollAccountFunction())
                .block();

        affirmNotNull(sessionHash, "error provisioning the LTI account");


        // finally redirect the account to the launch url
        try {
            final URI uri = new URI(sessionHash.getContinueTo());
            // update the status to completed
            ltIv11Service.updateLaunchRequestStatus(sessionHash.getLaunchRequestId(), LTILaunchRequestLogEvent.Action.COMPLETED, null)
                    .doOnEach(log.reactiveErrorThrowable("error provisioning the LTI account after a session init",
                            throwable -> new HashMap<String, Object>(){
                        {put("launchRequestId", launchRequestId);}
                        {put("hash", hash);}
                    }))
                    .block();
            return Response.seeOther(uri) //
                    .cacheControl(CACHE_POLICY) //
                    .build();
        } catch (URISyntaxException e) {
            log.jsonError("failed to complete the LTI flow after session init", new HashMap<String, Object>(){
                {put("launchRequestId", sessionHash.getLaunchRequestId());}
                {put("hash", sessionHash.getHash());}
            }, e);
            // update the status to failed
            ltIv11Service.updateLaunchRequestStatus(sessionHash.getLaunchRequestId(), LTILaunchRequestLogEvent.Action.ERROR,
                    e.getMessage())
                    .block();
            throw new IllegalStateFault("invalid url launch");
        }
    }

    @NonNull
    private Function3<UUID, UUID, String, Mono<UUID>> enrollAccountFunction() {
        return new Function3<UUID, UUID, String, Mono<UUID>>() {
            @Nonnull
            @Override
            public Mono<UUID> apply(@Nonnull UUID accountId, @Nonnull UUID cohortId, @Nonnull String pearsonUid) {
                // FIXME: this is an anti-pattern and it needs to be refactored
                // currently required to keep the enrolment timestamp accurate
                return cohortEnrollmentService.findCohortEnrollment(accountId, cohortId)
                        .switchIfEmpty(Mono.defer(() -> cohortEnrollmentService.enrollAccount(accountId, cohortId, EnrollmentType.LTI, pearsonUid)))
                        .then(Mono.just(accountId));
            }
        };
    }

    UUID parseCohortId(String urlString) {
        try {
            URL url = new URL(urlString);
            String pathstr = url.getPath();
            if (pathstr == null || pathstr.length() <= 1) {
                throw new MalformedURLException("error occurred parsing cohort id from learnspace url: " + urlString);
            }

            int stridx = -1;
            if (pathstr.charAt(0) == '/') {
                pathstr = pathstr.substring(1);
            }
            if ((stridx = pathstr.indexOf('/')) < 0) {
                throw new MalformedURLException("error occurred parsing cohort id from learnspace url: " + urlString);
            }

            return UUID.fromString(pathstr.substring(0, stridx));
        } catch (MalformedURLException ex) {
            // nothing we can do at this stage, throw a fault
            log.error(ex.getMessage());
            throw new IllegalArgumentFault(ex.getMessage());
        }
    }

    String parseContinueTo(String urlString, Map<String, String> ltiParams) {
        try {
            URL url = new URL(urlString);
            String pathstr = url.getPath();
            if (pathstr == null || pathstr.length() <= 1) {
                throw new MalformedURLException("error occurred parsing cohort id from learnspace url: " + urlString);
            }

            int stridx = -1;
            if (pathstr.charAt(0) == '/') {
                pathstr = pathstr.substring(1);
            }
            if (pathstr.indexOf('/') < 0) {
                throw new MalformedURLException("error occurred parsing cohort id from learnspace url: " + urlString);
            }

            // is this an on-demand product URL?
            if (pathstr.indexOf("ondemand/product/") != -1) {
                if ((stridx = pathstr.lastIndexOf('/')) == (pathstr.length() - 1)) {
                    throw new MalformedURLException("error occurred parsing product id from learnspace url: " + urlString);
                }

                String productId = pathstr.substring(stridx + 1);
                UUID deploymentId = deploymentService.findProductDeploymentId(productId).block();
                if (deploymentId == null) {
                    throw new MalformedURLException("product deployment id not found: " + urlString);
                }
                UUID cohortId = fetchOnDemandCohortId(productId, deploymentId, ltiParams);

                URL newUrl = new URL(url.getProtocol(), url.getHost(), "/" + cohortId + "/" + deploymentId);
                return newUrl.toString();
            }

            // this is a <cohortId>/<deploymentId> URL
            return urlString;
        } catch (MalformedURLException ex) {
            // nothing we can do at this stage, throw a fault
            log.error(ex.getMessage());
            throw new IllegalArgumentFault(ex.getMessage());
        }
    }

    UUID fetchOnDemandCohortId(String productId, UUID deploymentId, Map<String, String> ltiParams) {
        String customSmsCourseId = ltiParams.get("custom_sms_course_id");
        String customProductDiscipline = ltiParams.get("custom_product_discipline");
        affirmArgumentNotNullOrEmpty(customSmsCourseId, "missing or empty custom_sms_course_id");
        affirmArgumentNotNullOrEmpty(customProductDiscipline, "missing or empty custom_product_discipline");
        UUID tempCohortId = cohortService.findIdByProduct(productId).block();

        // Bronte cohort id is mapped to MX 'course' id ('custom_sms_course_id' is not unique across disciplines in MX, so we include discipline in the id)
        String lmsCourseId = customSmsCourseId + ':' + customProductDiscipline + ':' + tempCohortId; // needed to grab cohortId
        UUID cohortId = cohortService.findIdByLmsCourse(lmsCourseId).block();
        if (cohortId != null) {
            // we already have a cohort id mapped to this lms class
            return cohortId;
        } else {
            // if no lms class to bronte cohort exists, create new cohort from cohort template (created during publishing)
            // fetch template cohort data
            if (tempCohortId == null) {
                throw new IllegalArgumentFault("cohort id not found for product: " + productId);
            }
            CohortSummary tempCohortSummary = cohortService.fetchCohortSummary(tempCohortId).block();
            if (tempCohortSummary == null) {
                throw new IllegalArgumentFault("cohort summary not found for cohort:" + tempCohortId);
            }
            CohortSettings tempCohortSettings = cohortService.fetchCohortSettings(tempCohortId).block();
            if (tempCohortSettings == null) {
                throw new IllegalArgumentFault("cohort settings not found for cohort:" + tempCohortId);
            }

            // create new cohort
            UUID newCohortId = UUIDs.timeBased();
            CohortSummary newCohortSummary = cohortService.createCohort(newCohortId, tempCohortSummary.getCreatorId(), tempCohortSummary.getWorkspaceId(),
                                                                        tempCohortSummary.getName().replace("On-Demand Cohort Template",ltiParams.get("context_title") + ": On-Demand Cohort Instance"), EnrollmentType.LTI,
                                       tempCohortSummary.getStartDate(), tempCohortSummary.getEndDate(),
                                       tempCohortSummary.getSubscriptionId()).block();
            affirmNotNull(newCohortSummary, "error creating dynamic cohort");

            cohortService.createSettings(newCohortId,
                                         tempCohortSettings.getBannerPattern(), tempCohortSettings.getColor(), tempCohortSettings.getBannerImage(),
                                         productId).block();

            // save LTI credentials for new cohort
            // todo this only works while we're using global LTI credentials, else we'll need to lookup the keys of the template cohort
            String ltiCredentialKey = ltiConfig.getKey();
            String ltiCredentialSecret = ltiConfig.getSecret();
            affirmArgumentNotNullOrEmpty(ltiCredentialKey, "missing lti config key");
            affirmArgumentNotNullOrEmpty(ltiCredentialSecret, "missing lti config secret");
            cohortService.saveLTIConsumerKey(newCohortSummary, ltiCredentialKey, ltiCredentialSecret).block();

            // map new cohort to deployment
            deploymentService.saveDeploymentCohortId(newCohortId, deploymentId).block();
            // map new instance cohort id to template cohort id
            cohortService.saveCohortInstanceId(tempCohortId, newCohortId).block();
            // map new cohort to lms course id
            cohortService.saveLmsCourseCohortId(lmsCourseId, newCohortId).block();

            return newCohortId;
        }
    }
}
