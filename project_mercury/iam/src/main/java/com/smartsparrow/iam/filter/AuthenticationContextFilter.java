package com.smartsparrow.iam.filter;

import static com.google.common.base.Strings.isNullOrEmpty;
import static com.smartsparrow.util.log.JsonLayout.REQUEST_CONTEXT;
import static com.smartsparrow.util.log.JsonLayout.TRACE_ID;

import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.HttpMethod;
import javax.ws.rs.core.MediaType;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.net.HttpHeaders;
import com.google.inject.Provider;
import com.smartsparrow.exception.NotAuthorizedException;
import com.smartsparrow.iam.service.Account;
import com.smartsparrow.iam.service.AccountService;
import com.smartsparrow.iam.service.AuthenticationType;
import com.smartsparrow.iam.service.BronteWebToken;
import com.smartsparrow.iam.service.CredentialService;
import com.smartsparrow.iam.service.MutableAuthenticationContext;
import com.smartsparrow.iam.wiring.MutableAuthenticationContextProvider;
import com.smartsparrow.util.Tokens;
import com.smartsparrow.util.UUIDs;
import com.smartsparrow.util.log.data.Request;
import com.smartsparrow.util.log.data.RequestContext;

/**
 * "Inject" the user, only if properly authenticated, into the @RequestScope.
 */
@Singleton
public class AuthenticationContextFilter implements Filter {

    private static final Logger log = LoggerFactory.getLogger(AuthenticationContextFilter.class);

    private static final Pattern BEARER_PATTERN = Pattern.compile("^Bearer +([^ ]+) *$", Pattern.CASE_INSENSITIVE);
    private final static String BEARER = "Bearer";
    private static final String PARAMETER_TOKEN = "access_token";

    protected final Provider<MutableAuthenticationContext> mutableAuthenticationContextProvider;
    protected final AccountService accountService;
    protected final CredentialService credentialService;

    @Inject
    public AuthenticationContextFilter(Provider<MutableAuthenticationContext> mutableAuthenticationContextProvider,
            AccountService accountService,
            CredentialService credentialService) {
        this.mutableAuthenticationContextProvider = mutableAuthenticationContextProvider;
        this.accountService = accountService;
        this.credentialService = credentialService;
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        // assume this is safe cast.
        HttpServletRequest httpServletRequest = (HttpServletRequest) request;

        if(httpServletRequest.getMethod().equals(HttpMethod.GET) || httpServletRequest.getMethod().equals(HttpMethod.POST)) {

            // Parse all aspects of request looking for the token
            String token = findToken(httpServletRequest);

            try {

                if (isNullOrEmpty(token)) {
                    throw new NotAuthorizedException("Bearer token not supplied");
                }

                // find the account matching token
                Account account = accountService.findByBearerTokenBlocking(token);
                if (account == null) {
                    // the account was not found. Call the next filter in the chain
                    chain.doFilter(httpServletRequest, response);
                    return;
                }

                // store it into the RequestContext.
                MutableAuthenticationContext context = mutableAuthenticationContextProvider.get();
                context.setAccount(account);
                context.setAuthenticationType(AuthenticationType.BRONTE);
                context.setWebSessionToken(credentialService.findWebSessionToken(token));
                context.setWebToken(new BronteWebToken(token));

                RequestContext requestContext = new RequestContext()
                        .setAccountId(account.getId().toString())
                        .setRequest(new Request()
                                .setMethod(httpServletRequest.getMethod())
                                .setType(Request.Type.REST)
                                .setUri(((HttpServletRequest) request).getRequestURI())
                                .setRequestId(Tokens.generate()));

                MDC.put(REQUEST_CONTEXT, requestContext.toString());
                if(((HttpServletRequest) request).getHeader(TRACE_ID) == null) {
                    String traceId = UUIDs.timeBased().toString();
                    MDC.put(TRACE_ID, traceId);
                    ((HttpServletResponse)response).setHeader(TRACE_ID, traceId);
                }else {
                    MDC.put(TRACE_ID, ((HttpServletRequest) request).getHeader(TRACE_ID));
                }

                chain.doFilter(httpServletRequest, response);

            } catch (NotAuthorizedException e) {
                handleAuthFailure(response, token, e);
            } finally {
                MutableAuthenticationContextProvider.cleanup();
            }
        } else {
            chain.doFilter(httpServletRequest, response);
        }

    }

    /**
     *
     * Parse the request looking for the authorization token. The order of search is:
     *
     * - authorization header
     *    Authorization: Bearer mF_9.B5f-4.1JqM
     *
     * - url query string:
     *    https://domain.tld/some/service?access_token=mF_9.B5f-4.1JqM
     *
     * @param request
     * @return
     */
    protected String findToken(HttpServletRequest request) {

        // look for it in Authorization Header
        String authHeader = extractBearerFrom(request);
        if (!isNullOrEmpty(authHeader)) {
            return authHeader;
        }
        // look for it in query
        String authParam = request.getParameter(PARAMETER_TOKEN);
        if (!isNullOrEmpty(authParam)) {
            return authParam;
        }

        return null;
    }

    /**
     * Extract the bearer token from the incoming request. Reads the {@link HttpHeaders#AUTHORIZATION} header and
     * tries to extract the bearer token.
     *
     * @param request the incoming {@link HttpServletRequest}
     * @return the token {@link String} value or <code>null</code> if the Authorization header is not sent or does not
     * contains the token.
     */
    public static String extractBearerFrom(HttpServletRequest request) {
        String authHeader = request.getHeader(HttpHeaders.AUTHORIZATION);
        if (authHeader != null && authHeader.contains(BEARER)) {
            Matcher matcher = BEARER_PATTERN.matcher(authHeader);
            if (matcher.matches()) {
                return matcher.group(1);
            }
        }
        return null;
    }

    /**
     * Work around the fact that this filter is not in JAX-RS Jersey context, so the nice error mapping is not hooked at this
     * stage. To keep error messages consistent with the REST code, output the same exception json output pattern.
     * Example:
     *
     * {
     *   "status": 401,
     *   "type": "NOT_AUTHORIZED",
     *   "message": "Missing authentication token"
     * }
     *
     * @return
     */
    protected void handleAuthFailure(ServletResponse response, String token, NotAuthorizedException e)
            throws IOException {
        log.info("Authentication failure at rest authentication filter for token: " + token, e);
        HttpServletResponse httpResponse = (HttpServletResponse) response;
        int status = HttpServletResponse.SC_UNAUTHORIZED;
        httpResponse.setStatus(status);
        httpResponse.setHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON);
        httpResponse.getWriter().write(toJson(status, e.getType(), e.getMessage()));
    }

    protected String toJson(int status, String type, String message) throws IOException {
        JsonError error = new JsonError(status, type, message);
        ObjectMapper mapper = new ObjectMapper();
        return mapper.writeValueAsString(error);
    }

    private static class JsonError {
        int status;
        String type;
        String message;

        JsonError(int status, String type, String message) {
            this.status = status;
            this.type = type;
            this.message = message;
        }
        public int getStatus() {
            return status;
        }
        public String getType() {
            return type;
        }
        public String getMessage() {
            return message;
        }
    }

}
