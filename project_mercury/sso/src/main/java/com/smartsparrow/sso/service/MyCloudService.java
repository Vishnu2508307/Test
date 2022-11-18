package com.smartsparrow.sso.service;

import static com.smartsparrow.dataevent.RouteUri.MYCLOUD_PROFILE_GET;
import static com.smartsparrow.dataevent.RouteUri.MYCLOUD_TOKEN_VALIDATE;
import static com.smartsparrow.util.Warrants.affirmArgumentNotNullOrEmpty;
import static com.smartsparrow.util.Warrants.affirmNotNull;

import javax.annotation.Nonnull;
import javax.inject.Inject;
import javax.inject.Singleton;

import com.newrelic.api.agent.Trace;
import com.smartsparrow.exception.UnprocessableEntityException;
import com.smartsparrow.iam.data.MyCloudAccountTracking;
import com.smartsparrow.iam.data.MyCloudAccountTrackingGateway;
import com.smartsparrow.iam.service.AuthenticationType;
import com.smartsparrow.sso.lang.MyCloudServiceFault;
import com.smartsparrow.sso.wiring.MyCloudConfig;
import org.apache.camel.component.reactive.streams.api.CamelReactiveStreamsService;

import com.smartsparrow.exception.ConflictException;
import com.smartsparrow.exception.IllegalStateFault;
import com.smartsparrow.iam.lang.UnauthorizedFault;
import com.smartsparrow.iam.payload.AccountPayload;
import com.smartsparrow.iam.service.Account;
import com.smartsparrow.iam.service.AccountAdapter;
import com.smartsparrow.iam.service.AccountAvatar;
import com.smartsparrow.iam.service.AccountIdentityAttributes;
import com.smartsparrow.iam.service.AccountProvisionSource;
import com.smartsparrow.iam.service.AccountService;
import com.smartsparrow.iam.service.PermissionLevel;
import com.smartsparrow.iam.service.SubscriptionPermissionService;
import com.smartsparrow.sso.event.MyCloudProfileGetEventMessage;
import com.smartsparrow.sso.event.MyCloudTokenValidationEventMessage;
import com.smartsparrow.util.log.MercuryLogger;
import com.smartsparrow.util.log.MercuryLoggerFactory;
import com.smartsparrow.util.monitoring.ReactiveTransaction;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import reactor.core.publisher.Mono;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

@Singleton
public class MyCloudService {
    private static final MercuryLogger log = MercuryLoggerFactory.getLogger(MyCloudService.class);

    public static final String MYCLOUD_EMAIL_FORMAT = "%s@mycloud.internal";

    private final AccountService accountService;
    private final SubscriptionPermissionService subscriptionPermissionService;
    private final CamelReactiveStreamsService camelReactiveStreamsService;
    private final MyCloudAccountTrackingGateway myCloudAccountTrackingGateway;
    private final MyCloudConfig myCloudConfig;

    @Inject
    public MyCloudService(final AccountService accountService,
                          final SubscriptionPermissionService subscriptionPermissionService,
                          final CamelReactiveStreamsService camelReactiveStreamsService,
                          final MyCloudAccountTrackingGateway myCloudAccountTrackingGateway,
                          final MyCloudConfig myCloudConfig) {
        this.accountService = accountService;
        this.subscriptionPermissionService = subscriptionPermissionService;
        this.camelReactiveStreamsService = camelReactiveStreamsService;
        this.myCloudAccountTrackingGateway = myCloudAccountTrackingGateway;
        this.myCloudConfig = myCloudConfig;
    }

    /**
     * Send an event to the {@link com.smartsparrow.dataevent.RouteUri#MYCLOUD_TOKEN_VALIDATE} route which instructs camel
     * to perform an external http request to the myCloud service to validate the token.
     *
     * @param token the token to validate
     * @return Pearson UID when the token is valid
     * @throws UnauthorizedFault when the token is not valid
     */
    @Trace(async = true)
    public Mono<String> validateToken(final String token) {
        affirmArgumentNotNullOrEmpty(token, "token is required");
        return Mono.just(new MyCloudTokenValidationEventMessage(token)) //
                .doOnEach(log.reactiveInfo("handling mycloud authorization"))
                .doOnEach(ReactiveTransaction.linkOnNext())
                .map(event -> camelReactiveStreamsService.toStream(MYCLOUD_TOKEN_VALIDATE, event, MyCloudTokenValidationEventMessage.class)) //
                .flatMap(Mono::from)
                .map(mycloudTokenValidationEventMessage -> {
                    Boolean isValid = mycloudTokenValidationEventMessage.isValid();
                    Boolean hasError = mycloudTokenValidationEventMessage.hasError();
                    if (isValid) {
                        return mycloudTokenValidationEventMessage.getPearsonUid();
                    } else if (hasError) {
                        throw new MyCloudServiceFault("myCloud service error");
                    } else {
                        log.warn("Invalid token supplied: " + token);
                        throw new UnauthorizedFault("Invalid token supplied");
                    }
                });
    }

    /**
     * Find the Bronte account by pearsionUid.
     *
     * @param pearsonUid the mycloud user id to find the account for
     * @return a mono with the associated account or an empty mono when not found
     */
    public Mono<Account> findAccount(@Nonnull final String pearsonUid) {
        // find the corresponding account id
        return myCloudAccountTrackingGateway.findAccountId(pearsonUid)
                // find the account by id
                .flatMap(myCloudAccountTracking -> accountService.findById(myCloudAccountTracking.getAccountId())
                        .singleOrEmpty());
    }

    /**
     * Provision an account for a Pearson internal user. The first time a Pearson internal user is going through the
     * Bronte authorization flow the Bronte account is provisioned with a {@link com.smartsparrow.iam.service.AccountRole#INSTRUCTOR}
     * role.
     *
     * @param pearsonUid the pearson id to provision the account for
     * @return a mono with the provisioned account
     * @throws IllegalStateFault when the account email is already taken by another account
     */
    public Mono<Account> provisionAccount(@Nonnull final String pearsonUid) {
        affirmArgumentNotNullOrEmpty(pearsonUid, "pearsonUid is required");

        try {
            AccountAdapter accountAdapter = accountService.provision(AccountProvisionSource.OIDC,
                    null, null, null,
                    null, String.format(MYCLOUD_EMAIL_FORMAT, pearsonUid), null,
                    // the account is provision with an instructor role
                    null, null, true, AuthenticationType.MYCLOUD);

            Account acct = accountAdapter.getAccount();

            return subscriptionPermissionService
                    .saveAccountPermission(acct.getId(), acct.getSubscriptionId(), PermissionLevel.OWNER)
                    .singleOrEmpty()
                    // persist the accountId -> pearsonUid tracking
                    .then(myCloudAccountTrackingGateway.persist(new MyCloudAccountTracking()
                            .setAccountId(acct.getId())
                            .setMyCloudUserId(pearsonUid))
                            .singleOrEmpty())
                    .thenReturn(acct);
        } catch (ConflictException e) {
            // rethrow as a fault, we should never get here in the IES authorization flow
            // if we are here, the data is in an inconsistent state
            log.error(e.getMessage(), e);
            throw new IllegalStateFault(e.getMessage());
        }
    }

    /**
     * Send an event to the {@link com.smartsparrow.dataevent.RouteUri#IES_PROFILE_GET} route which instructs camel
     * to perform an external http request to the IES service to fetch the user identity profile.
     *
     * @param pearsonUid the userId to find the identity profile for
     * @param accessToken a valid access token
     * @return a mono with the identity profile when found
     * @throws UnauthorizedFault when the the request failed for any reason
     */
    @Trace(async = true)
    public Mono<IdentityProfile> getProfile(@Nonnull final String pearsonUid,
                                            @Nonnull final String accessToken) {
        affirmArgumentNotNullOrEmpty(pearsonUid, "pearsonUid is required");
        affirmArgumentNotNullOrEmpty(accessToken, "accessToken is required");
        return Mono.just(new MyCloudProfileGetEventMessage(pearsonUid, accessToken)) //
                .doOnEach(ReactiveTransaction.linkOnNext())
                .doOnEach(log.reactiveInfo("handling mycloud profile get"))
                .doOnEach(log.reactiveErrorThrowable("error while fetching myCloud user identity profile"))
                .map(event -> camelReactiveStreamsService.toStream(MYCLOUD_PROFILE_GET, event, MyCloudProfileGetEventMessage.class)) //
                .flatMap(Mono::from)
                .map(mycloudProfileGetEventMessage -> {
                    final IdentityProfile identity = mycloudProfileGetEventMessage.getIdentityProfile();
                    if (identity != null) {
                        return identity;
                    }
                    throw new UnauthorizedFault("Invalid token supplied");
                });
    }

    /**
     * Get account payload for Pearson Identity user
     *
     * @param pearsonUid pearson user id
     * @param pearsonToken pearson user access token
     * @param account user account metadata
     * @return a mono with the account payload object
     */
    public Mono<AccountPayload> getAccountPayload(@Nonnull final String pearsonUid,
                                                  @Nonnull final String pearsonToken,
                                                  @Nonnull Account account) {
        affirmNotNull(account, "Account should not be null");
        affirmArgumentNotNullOrEmpty(pearsonUid, "pearsonUid is required");
        affirmArgumentNotNullOrEmpty(pearsonToken, "pearsonToken is required");

        return getProfile(pearsonUid, pearsonToken)
                .map(identityProfile -> AccountPayload.from(account, new AccountIdentityAttributes()
                        .setAccountId(account.getId())
                        .setPrimaryEmail(identityProfile.getPrimaryEmail())
                        .setGivenName(identityProfile.getGivenName())
                        .setFamilyName(identityProfile.getFamilyName()),
                        new AccountAvatar(), AuthenticationType.MYCLOUD));
    }

    /**
     * Perform http request to the myCloud service to validate the token with debug info.
     *
     * @param myCloudToken the token to validate
     * @return Pearson UID when the token is valid
     * @throws UnauthorizedFault when the token is not valid
     */
    public Mono<String> debugValidateToken(String myCloudToken) {
        return Mono.defer(() -> {
            String uid = "";

            HttpURLConnection conn = null;
            try {
                log.info("validating myCloud token: " + myCloudToken);

                URL url = new URL(myCloudConfig.getBaseUrl() + "/auth/json/pearson/sessions/" + myCloudToken + "?_action=validate");
                conn = (HttpURLConnection) url.openConnection();
                conn.setUseCaches(false);

                // set request headers
                conn.setRequestMethod("POST");
                setCommonHeaders(conn);

                conn.connect();

                // check response code
                int responseCode = conn.getResponseCode();
                if (responseCode < 200 || responseCode > 299) {
                    String data = readResponse(conn);
                    log.error(String.format("Unexpected response code %s, data: %s", responseCode, data));
                    return Mono.error(new MyCloudServiceFault("Unexpected response code " + responseCode));
                }

                // parse user id
                String data = readResponse(conn);
                JSONObject json = new JSONObject(data);
                if (!json.getBoolean("valid")) {
                    return Mono.error(new UnauthorizedFault("Invalid token supplied"));
                }
                uid = json.getString("uid");
            } catch (MalformedURLException ex) {
                log.error(ex.getMessage());
                return Mono.error(ex);
            } catch (IOException ex) {
                log.error(ex.getMessage());
                return Mono.error(ex);
            } catch (JSONException ex) {
                log.error(ex.getMessage());
                return Mono.error(new UnprocessableEntityException("Unexpected object received from myCloud service"));
            } finally {
                if (conn != null) {
                    conn.disconnect();
                }
            }

            return Mono.just(uid);
        });
    }

    /**
     * Perform http request to the myCloud service to get user attributes with debug info.
     *
     * @param pearsonUid the userId to find the identity profile for
     * @param myCloudToken a valid access token
     * @return a mono with the identity profile when found
     * @throws UnauthorizedFault when the the request failed for any reason
     */
    public Mono<IdentityProfile> debugGetProfile(@Nonnull final String pearsonUid,
                                                 @Nonnull final String myCloudToken) {
        return Mono.defer(() -> {
            IdentityProfile identityProfile = new IdentityProfile();

            HttpURLConnection conn = null;
            try {
                log.info(String.format("fetching '%s' profile using myCloud token: %s", pearsonUid, myCloudToken));

                URL url = new URL(myCloudConfig.getBaseUrl() + "/auth/json/pearson/users/" + pearsonUid);
                conn = (HttpURLConnection) url.openConnection();
                conn.setUseCaches(false);

                // set request headers
                conn.setRequestMethod("GET");
                setCommonHeaders(conn);
                conn.setRequestProperty("PearsonSSOSession", myCloudToken);

                // check response code
                int responseCode = conn.getResponseCode();
                if (responseCode < 200 || responseCode > 299) {
                    String data = readResponse(conn);
                    log.error(String.format("Unexpected response code %s, data: %s", responseCode, data));
                    return Mono.error(new MyCloudServiceFault("Unexpected response code " + responseCode));
                }

                // parse identity profile
                String data = readResponse(conn);
                JSONObject json = new JSONObject(data);
                JSONArray givenNames = json.getJSONArray("givenName");
                JSONArray surnames = json.getJSONArray("sn");
                JSONArray emails = json.getJSONArray("mail");
                identityProfile.setId(json.getString("_id"))
                        .setGivenName(givenNames.getString(0))
                        .setFamilyName(surnames.getString(0))
                        .setPrimaryEmail(emails.getString(0));
            } catch (MalformedURLException ex) {
                log.error(ex.getMessage());
                return Mono.error(ex);
            } catch (IOException ex) {
                log.error(ex.getMessage());
                return Mono.error(ex);
            } catch (JSONException ex) {
                log.error(ex.getMessage());
                return Mono.error(new UnprocessableEntityException("Unexpected object received from myCloud service"));
            } finally {
                if (conn != null) {
                    conn.disconnect();
                }
            }

            return Mono.just(identityProfile);
        });
    }

    private void setCommonHeaders(HttpURLConnection conn) {
        conn.setRequestProperty("User-Agent", "HttpURLConnection/11.0 Java");
        conn.setRequestProperty("Content-Type", "application/json");
        conn.setRequestProperty("Accept", "application/json");
        conn.setRequestProperty("Connection", "close");
    }

    private String readResponse(HttpURLConnection conn) throws IOException {
        InputStream is = null;
        try {
            is = conn.getInputStream();
        } catch (IOException ex) {
            is = conn.getErrorStream();
            if (is == null) {
                log.error("No error stream available");
                throw ex;
            }
        }

        try (BufferedReader br = new BufferedReader(new InputStreamReader(is, "utf-8"))) {
            StringBuilder response = new StringBuilder();
            String responseLine = null;
            while ((responseLine = br.readLine()) != null) {
                response.append(responseLine.trim());
            }

            return response.toString();
        }
    }
}
