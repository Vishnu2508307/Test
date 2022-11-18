package com.smartsparrow.rest.resource.to;

import static com.smartsparrow.learner.redirect.LearnerRedirectType.PRODUCT;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

import java.net.URI;

import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MultivaluedHashMap;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import org.apache.http.HttpStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.google.common.collect.ImmutableMap;
import com.smartsparrow.exception.IllegalArgumentFault;
import com.smartsparrow.learner.redirect.LearnerRedirect;
import com.smartsparrow.learner.service.LearnerRedirectService;

import reactor.core.publisher.Mono;

class LearnerRedirectResourceTest {

    @InjectMocks
    private LearnerRedirectResource learnerRedirectResource;

    @Mock
    private LearnerRedirectService learnerRedirectService;

    @Mock
    private UriInfo uriInfo;

    @Mock
    private HttpHeaders httpHeaders;

    private static final String requestPath = "ABCD1234";

    @BeforeEach
    void setUp() {
        MockitoAnnotations.initMocks(this);

        when(learnerRedirectService.fetch(PRODUCT, requestPath))
                .thenReturn(Mono.just(new LearnerRedirect()
                                              .setDestinationPath("/redirect/path")));

        when(httpHeaders.getHeaderString("Forwarded")).thenReturn("by=1.1.1.1;host=fwded.pearson.com;proto=https");
    }

    @Test
    void handle_noForwardedHeader() {
        when(httpHeaders.getHeaderString("Forwarded")).thenReturn(null);

        assertThrows(IllegalArgumentFault.class, () -> //
                learnerRedirectResource.handleTypeKey(uriInfo, httpHeaders, PRODUCT.name(), requestPath));
    }

    // https://fwded.pearson.com/to/invalid/key
    @Test
    void handleTypeKey_invalidType() {
        assertThrows(IllegalArgumentFault.class, () -> //
                learnerRedirectResource.handleTypeKey(uriInfo, httpHeaders, "invalid", "key"));
    }

    // https://fwded.pearson.com/to/product/not-found
    @Test
    void handleTypeKey_redirectNotFound() {
        when(learnerRedirectService.fetch(PRODUCT, "not-found"))
                .thenReturn(Mono.empty());

        assertThrows(IllegalArgumentFault.class, () -> //
                learnerRedirectResource.handleTypeKey(uriInfo, httpHeaders, PRODUCT.name(), "not-found"));
    }

    // https://fwded.pearson.com/to/product/ABCD123
    @Test
    void handleTypeKey() {
        Response response = learnerRedirectResource.handleTypeKey(uriInfo, httpHeaders, PRODUCT.name(), requestPath);
        URI redirect = response.getLocation();

        assertEquals(HttpStatus.SC_SEE_OTHER, response.getStatus());
        assertEquals("https://fwded.pearson.com/redirect/path", redirect.toString());
    }

    // https://fwded.pearson.com/to/product/ABCD123?foo=bar
    @Test
    void handleTypeKey_queryParams() {
        MultivaluedMap<String, String> queryParams = new MultivaluedHashMap<>(ImmutableMap.of("foo", "bar"));
        when(uriInfo.getQueryParameters()).thenReturn(queryParams);

        Response response = learnerRedirectResource.handleTypeKey(uriInfo, httpHeaders, PRODUCT.name(), requestPath);

        URI redirect = response.getLocation();

        assertEquals(HttpStatus.SC_SEE_OTHER, response.getStatus());
        assertEquals("https://fwded.pearson.com/redirect/path?foo=bar", redirect.toString());
    }

    // https://fwded.pearson.com/to/invalid/key/extra
    @Test
    void handleTypeKeyExtra_invalid() {
        assertThrows(IllegalArgumentFault.class, () -> //
                learnerRedirectResource.handleTypeKeyExtra(uriInfo, httpHeaders, "invalid", "key", "extra"));
    }

    // https://fwded.pearson.com/to/product/ABCD123/extra
    @Test
    void handleTypeKeyExtra() {
        Response response = learnerRedirectResource.handleTypeKeyExtra(uriInfo,
                                                                       httpHeaders,
                                                                       PRODUCT.name(),
                                                                       requestPath,
                                                                       "extra");

        URI redirect = response.getLocation();

        assertEquals(HttpStatus.SC_SEE_OTHER, response.getStatus());
        assertEquals("https://fwded.pearson.com/redirect/path/extra", redirect.toString());
    }

    // https://fwded.pearson.com/to/product/ABCD123/extra/
    @Test
    void handleTypeKeyExtra_trailingSlash() {
        Response response = learnerRedirectResource
                .handleTypeKeyExtra(uriInfo, httpHeaders, PRODUCT.name(), requestPath, "extra/");

        URI redirect = response.getLocation();

        assertEquals(HttpStatus.SC_SEE_OTHER, response.getStatus());
        assertEquals("https://fwded.pearson.com/redirect/path/extra/", redirect.toString());
    }

    // https://fwded.pearson.com/to/product/ABCD123/extra/path/?foo=bar
    @Test
    void handleTypeKeyExtra_queryParams() {
        MultivaluedMap<String, String> queryParams = new MultivaluedHashMap<>(ImmutableMap.of("foo", "bar"));
        when(uriInfo.getQueryParameters()).thenReturn(queryParams);

        Response response = learnerRedirectResource
                .handleTypeKeyExtra(uriInfo, httpHeaders, PRODUCT.name(), requestPath, "extra/path/");

        URI redirect = response.getLocation();

        assertEquals(HttpStatus.SC_SEE_OTHER, response.getStatus());
        assertEquals("https://fwded.pearson.com/redirect/path/extra/path/?foo=bar", redirect.toString());
    }

    // http://an.evil.host/to/product/ABCD123
    @Test
    void handle_forwarded_badDomain() {
        when(httpHeaders.getHeaderString("Forwarded")).thenReturn("by=0.0.0.0;host=an.evil.host;proto=http");

        assertThrows(IllegalArgumentFault.class, () -> //
                learnerRedirectResource.handleTypeKey(uriInfo, httpHeaders, PRODUCT.name(), requestPath));
    }
}