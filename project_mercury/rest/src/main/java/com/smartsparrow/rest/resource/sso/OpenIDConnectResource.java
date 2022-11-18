package com.smartsparrow.rest.resource.sso;

import static com.smartsparrow.util.Warrants.affirmArgumentNotNullOrEmpty;
import static com.smartsparrow.util.Warrants.affirmNotNull;

import java.net.URI;
import java.util.UUID;

import javax.inject.Inject;
import javax.ws.rs.CookieParam;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.CacheControl;
import javax.ws.rs.core.Cookie;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.NewCookie;
import javax.ws.rs.core.Response;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.smartsparrow.iam.service.WebSessionToken;
import com.smartsparrow.rest.util.Cookies;
import com.smartsparrow.sso.service.OpenIDConnectService;

// See:
// https://developers.google.com/identity/protocols/OpenIDConnect
// https://connect2id.com/products/nimbus-oauth-openid-connect-sdk/examples/openid-connect/oidc-auth
// https://connect2id.com/products/nimbus-jose-jwt/examples/validating-jwt-access-tokens
//
@Path("/oidc")
public class OpenIDConnectResource {

    private static final Logger log = LoggerFactory.getLogger(OpenIDConnectResource.class);

    private final OpenIDConnectService openIDConnectService;

    //
    private final static CacheControl CACHE_POLICY = new CacheControl();

    static {
        // See: https://developers.google.com/web/fundamentals/performance/optimizing-content-efficiency/http-caching
        // Based on the decision tree in the above doc, only no-store needs to be set
        CACHE_POLICY.setNoStore(true);
    }

    @Inject
    public OpenIDConnectResource(OpenIDConnectService openIDConnectService) {
        this.openIDConnectService = openIDConnectService;
    }

    @GET
    @Path("/to")
    @Produces(MediaType.APPLICATION_JSON)
    public Response toHandler(@QueryParam("relying_party_id") final UUID paramId,
            @QueryParam("continue_to") final String paramContinueTo,
            @CookieParam("bearerToken") final Cookie bearerTokenCookie) {
        //
        affirmNotNull(paramId, "missing relying_party_id");
        affirmArgumentNotNullOrEmpty(paramContinueTo, "missing continue_to");

        // extract the bearer token to invalidate.
        String invalidateBearerToken = bearerTokenCookie == null ? null : bearerTokenCookie.getValue();

        // build the authentication request.
        URI authenticationRequest = openIDConnectService.buildAuthenticationRequest(paramId, paramContinueTo,
                                                                                    invalidateBearerToken);

        // Start the response.
        Response.ResponseBuilder responseBuilder = Response.temporaryRedirect(authenticationRequest) //
                // Don't allow this response to be cached.
                .cacheControl(CACHE_POLICY);

        // Clear a cookie if one was sent.
        if (bearerTokenCookie != null) {
            NewCookie invalidateCookie = Cookies.mapToRemove(bearerTokenCookie);
            responseBuilder.cookie(invalidateCookie);
        }

        return responseBuilder.build();
    }

    @GET
    @Path("/callback")
    @Produces(MediaType.APPLICATION_JSON)
    public Response callbackHandler(@QueryParam("code") final String paramCode,
            @QueryParam("state") final String paramState) {
        //
        affirmArgumentNotNullOrEmpty(paramCode, "missing parameter code");
        affirmArgumentNotNullOrEmpty(paramState, "missing parameter state");

        // authenticate the incoming user.
        WebSessionToken webSessionToken = openIDConnectService.processCallback(paramCode, paramState);
        NewCookie authCookie = Cookies.map(webSessionToken);

        // get the original redirect URL.
        String redirectUrl = openIDConnectService.getContinueTo(paramState);

        return Response.seeOther(URI.create(redirectUrl)) //
                .cacheControl(CACHE_POLICY) //
                .cookie(authCookie) //
                .build();
    }

    @GET
    @Path("/logout")
    @Produces(MediaType.APPLICATION_JSON)
    public Response logout(@CookieParam("bearerToken") final Cookie bearerTokenCookie,
                           @QueryParam("source") final String source) {
        //
        String bearerToken = bearerTokenCookie == null ? null : bearerTokenCookie.getValue();
        URI redirectLocation = openIDConnectService.logout(bearerToken, source);

        // Clear the cookie if one was sent.
        if (bearerToken != null) {
            NewCookie invalidateCookie = Cookies.mapToRemove(bearerTokenCookie);
            return Response.temporaryRedirect(redirectLocation)
                    .cacheControl(CACHE_POLICY)
                    .cookie(invalidateCookie)
                    .build();
        } else {
            //
            return Response.temporaryRedirect(redirectLocation)
                    .cacheControl(CACHE_POLICY)
                    .build();
        }
    }
}
