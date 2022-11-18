package com.smartsparrow.rest.resource.to;

import static com.google.common.base.Strings.isNullOrEmpty;
import static com.smartsparrow.util.Warrants.affirmArgument;
import static com.smartsparrow.util.Warrants.affirmArgumentNotNullOrEmpty;
import static com.smartsparrow.util.Warrants.affirmNotNull;

import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.CacheControl;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import org.apache.http.NameValuePair;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.message.BasicNameValuePair;

import com.smartsparrow.exception.IllegalArgumentFault;
import com.smartsparrow.learner.redirect.LearnerRedirect;
import com.smartsparrow.learner.redirect.LearnerRedirectType;
import com.smartsparrow.learner.service.LearnerRedirectService;
import com.smartsparrow.util.Enums;

@Path("/")
public class LearnerRedirectResource {

    private final LearnerRedirectService learnerRedirectService;

    private final static CacheControl CACHE_POLICY = new CacheControl();
    private static final String MSG_INVALID_REQUEST = "invalid request";

    static {
        CACHE_POLICY.setNoStore(true);
    }

    @Inject
    public LearnerRedirectResource(final LearnerRedirectService learnerRedirectService) {
        this.learnerRedirectService = learnerRedirectService;
    }

    @GET
    @Path("/{type}/{key}")
    public Response handleTypeKey(@Context final UriInfo uriInfo,
                                  @Context final HttpHeaders httpHeaders,
                                  @PathParam("type") final String paramType,
                                  @PathParam("key") final String key) {
        //
        try {
            LearnerRedirectType type = Enums.ofToUpperCase(LearnerRedirectType.class, paramType);
            return handler(uriInfo, httpHeaders, type, key, null);
        } catch (IllegalArgumentException iae) {
            throw new IllegalArgumentFault(MSG_INVALID_REQUEST);
        }
    }

    @GET
    @Path("/{type}/{key}/{extra:.*}")
    public Response handleTypeKeyExtra(@Context final UriInfo uriInfo,
                                       @Context final HttpHeaders httpHeaders,
                                       @PathParam("type") final String paramType,
                                       @PathParam("key") final String key,
                                       @PathParam("extra") final String extra) {
        //
        try {
            LearnerRedirectType type = Enums.ofToUpperCase(LearnerRedirectType.class, paramType);
            return handler(uriInfo, httpHeaders, type, key, extra);
        } catch (IllegalArgumentException iae) {
            throw new IllegalArgumentFault(MSG_INVALID_REQUEST);
        }
    }


    /**
     * Redirects the request to the proper destination path defined by the {@link LearnerRedirect} record.
     * <p>
     * Will issue a relative Location '/foo/bar.html' if no trusted Forwarded header is provided.
     *
     * @param uriInfo uri info
     * @param headers request headers
     * @param type redirect type
     * @param key the redirect key
     * @param extraPath url extra path
     * @return a redirect response to the learnerRedirect destination path
     * @throws IllegalArgumentFault on error cases.
     */
    private Response handler(@Context final UriInfo uriInfo,
                             @Context final HttpHeaders headers,
                             final LearnerRedirectType type,
                             final String key,
                             final String extraPath) {
        //
        // This service should only be exposed through our public facing endpoints (not the ALB)
        // So, it means that the incoming request MUST have a Forwarded header!
        String headerForwarded = headers.getHeaderString(com.google.common.net.HttpHeaders.FORWARDED);
        affirmArgumentNotNullOrEmpty(headerForwarded, MSG_INVALID_REQUEST);

        //
        // Find the redirect; error if not found.
        LearnerRedirect learnerRedirect = learnerRedirectService.fetch(type, key).block();
        affirmNotNull(learnerRedirect, MSG_INVALID_REQUEST);

        //
        URIBuilder uriBuilder = new URIBuilder();

        //
        // Parse the Forwarded header.
        //   See: https://developer.mozilla.org/en-US/docs/Web/HTTP/Headers/Forwarded
        //   Example header looks like:
        //   > Forwarded: by=180.150.64.250;host=learn-bronte-dev.pearson.com;proto=https
        //

        // first split by ';'
        Map<String, String> forwardedPairs = Arrays.stream(headerForwarded.split(";"))
                .map(String::strip)
                // then map entries and split by '='
                .map(s -> s.split("="))
                // drop invalid supplied pairs.
                .filter(s -> s.length == 2)
                // key/value it into a map.
                .collect(Collectors.toMap(e -> e[0], e -> e[1]));

        // extract the host.
        String host = forwardedPairs.get("host");
        affirmArgumentNotNullOrEmpty(host, MSG_INVALID_REQUEST);
        uriBuilder = uriBuilder.setHost(host);

        // extract the protocol.
        String proto = forwardedPairs.get("proto");
        if (!isNullOrEmpty(proto)) {
            // set the scheme.
            uriBuilder = uriBuilder.setScheme(proto);
        }

        //
        // Construct the new path.
        //
        StringBuilder newPath = new StringBuilder(learnerRedirect.getDestinationPath());
        // append the extra path if supplied.
        if (!isNullOrEmpty(extraPath)) {
            newPath.append("/").append(extraPath);
        }
        uriBuilder = uriBuilder.setPath(newPath.toString());

        //
        // Carry over query params to the redirect
        //
        MultivaluedMap<String, String> queryParameters = uriInfo.getQueryParameters();
        if (queryParameters != null && !queryParameters.isEmpty()) {
            List<NameValuePair> p = queryParameters.entrySet().stream() //
                    // for each query param entry
                    .flatMap(entry -> {
                        return entry.getValue().stream() //
                                .map(val -> new BasicNameValuePair(entry.getKey(), val));
                    })
                    .collect(Collectors.toList());
            uriBuilder = uriBuilder.addParameters(p);
        }

        // Validate the host ends in pearson.com to prevent open redirects.
        String uriHost = uriBuilder.getHost();
        affirmArgument(uriHost.endsWith(".pearson.com"), MSG_INVALID_REQUEST);

        // write the response.
        try {
            return Response.seeOther(uriBuilder.build())
                    .cacheControl(CACHE_POLICY) //
                    .build();
        } catch (URISyntaxException e) {
            throw new IllegalArgumentFault(MSG_INVALID_REQUEST);
        }
    }
}
