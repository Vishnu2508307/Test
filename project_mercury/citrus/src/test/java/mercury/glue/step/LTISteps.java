package mercury.glue.step;

import static mercury.common.CitrusAssert.assertContains;
import static mercury.common.Variables.interpolate;
import static mercury.common.Variables.nameFrom;
import static mercury.glue.wiring.CitrusConfiguration.MESSAGE_OPERATIONS;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.apache.http.Header;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.protocol.HttpCoreContext;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;

import com.consol.citrus.annotations.CitrusResource;
import com.consol.citrus.context.TestContext;
import com.consol.citrus.dsl.runner.TestRunner;
import com.google.common.base.Strings;
import com.smartsparrow.sso.lang.OAuthHandlerException;
import com.smartsparrow.sso.service.LTIMessageSignatures;
import com.smartsparrow.sso.service.LTIParam;
import com.smartsparrow.util.Hashing;
import com.smartsparrow.util.Tokens;

import cucumber.api.java.en.And;
import cucumber.api.java.en.Given;
import cucumber.api.java.en.Then;
import cucumber.api.java.en.When;
import mercury.common.MessageOperations;
import mercury.common.PayloadBuilder;
import net.oauth.OAuthMessage;

public class LTISteps {

    private static final Logger log = LoggerFactory.getLogger(LTISteps.class);

    @CitrusResource
    private TestRunner runner;

    @Autowired
    @Qualifier(MESSAGE_OPERATIONS)
    private MessageOperations messageOperations;

    @Value("${mercury.host}")
    private String backendHost;

    @Value("${mercury.port}")
    private int backendPort;

    // required for the lti launch + ies flow to work
    private static final String IESTokenMock = "eyJraWQiOiJrMzI4LjE1NjM5MTM0ODEiLCJhbGciOiJSUzUxMiJ9.eyJzdWIiOiJmZmZmZmZmZjVmMjBlMDlkMWQ0Y" +
            "jc0MDFkZmNjYmZiMiIsImhjYyI6IkFVIiwidHlwZSI6ImF0IiwiZXhwIjoxNTk3MTI0NjQxLCJpYXQiOjE1OTcxMjI4NDEsImNsaWVud" +
            "F9pZCI6Im9YNVZtNlNFRWVTaTVRQkVBVTAwODF0eDIwVUhFNDY5Iiwic2Vzc2lkIjoiNzY1ZmVjZDAtMWU5ZC00ZDkxLWEzNDktMDI2N" +
            "zRkNzRkNGI4In0.Bd89NMcbydGhzv_QkP-rCXUqNNrPhl9qwXQ0czz_cKgsI66Bqi9aAcaTGeSz2awbFlOzDfrYm9fkwrlbq0yaeowjo" +
            "SVw6BAhXct_vqv83_agcY3w5fhJmpl-gUL4wj3uZIg8uKHXBF8fhjaNLdIO9HmahwAocSpH71EtLOD62nnGv3EmsF9Hzw0abpPGMSF9g" +
            "DUqRS3rjXzrAkjRzX9CX1A_odYPkP65UYSgHNfVKP7jjJHS1x-v2um6GpX435RO-F38LPRy336mEeoGoZv6X9q6i5JA5H2dauhLna728" +
            "q3Fmg2kKsLlvGi148JM4wNb6-DzxBnq_LyES0e7Iwkg1w";

    //
    private HttpCoreContext requestContext;

    @Then("^mercury generates a valid key and secret$")
    public void mercuryGeneratesAValidKeyAndSecret() {
        String payload = "{" +
                "\"type\":\"iam.ltiConsumerKey.create.ok\"," +
                "\"response\":{" +
                "\"ltiConsumerKey\":{" +
                "\"id\":\"@notEmpty()@\"," +
                "\"key\":\"@notEmpty()@\"," +
                "\"secret\":\"@notEmpty()@\"," +
                " \"subscriptionId\":\"@notEmpty()@\"" +
                "}}," +
                "\"replyTo\":\"@notEmpty()@\"}";
        messageOperations.receiveJSON(runner, action -> action.payload(payload)
                .extractFromPayload("$.response.ltiConsumerKey.key", "validKey")
                .extractFromPayload("$.response.ltiConsumerKey.secret", "validSecret"));
    }

    @Given("an LTI Launch Request to {string} for cohort {string} and activity {string} with {string} and {string} with parameters")
    public void anLTILaunchRequestToForCohortAndActivityWithAndWithParameters(String url, String cohortName, String activityName,
                                                                              String keyName, String secretName,
                                                                              final Map<String, String> suppliedParameters) throws OAuthHandlerException {

        final TestContext testContext = messageOperations.getTestContext(runner);
        //
        // Create the signed parameters and store launch request info.
        //

        final String key = testContext.getVariable(nameFrom(keyName, "ltiKey"));
        final String secret = testContext.getVariable(nameFrom(secretName, "ltiSecret"));

        final String fullUrl = String.format("%s/%s/%s", url,
                testContext.getVariable(nameFrom(cohortName, "id")),
                testContext.getVariable(nameFrom(activityName, "id"))
        );

        Map<String, String> msgParams = new HashMap<>();
        // add in the consumer key
        msgParams.put(LTIParam.OAUTH_CONSUMER_KEY.getValue(), key);
        suppliedParameters.entrySet().forEach(entry -> {
            // a bit of a hack..
            String value = entry.getValue();
            if (value.contains("${__generate}")) {
                value = value.replace("${__generate}", Tokens.generate(12));
                runner.createVariable(nameFrom(entry.getKey(), "one"), value);
            }
            if (value.equals("previous_user")) {
                value = messageOperations.getTestContext(runner).getVariable("user_id_one");
            }
            msgParams.put(entry.getKey(), value);
        });

        Map<String, String> signedParameters = LTIMessageSignatures.sign(new OAuthMessage("POST",
                        fullUrl,
                        msgParams.entrySet()),
                key,
                secret);
        runner.createVariable("launchRequestUrl", fullUrl);

        // save the signed launch parameters to a variable.
        runner.createVariable("signedLaunchParameters", PayloadBuilder.getUrlEncodedForm(signedParameters));
    }

    @And("a {string} as {string}")
    public void an(String name, String value) {
        runner.variable(name, value);
    }

    @Then("the response status code is {string}")
    public void theResponseStatusCodeIs(String expectedStatus) {
        assertEquals(HttpStatus.valueOf(expectedStatus).value(), //
                     requestContext.getResponse().getStatusLine().getStatusCode());
    }

    @And("the response has a header {string} of {string}")
    public void theResponseHasAHeaderAs(String headerName, String expectedHeaderValue) {
        String actualHeaderValue = requestContext.getResponse().getFirstHeader(headerName).getValue();
        assertEquals(expectedHeaderValue, actualHeaderValue);
    }

    @And("the response has a header {string} starting with {string}")
    public void theResponseHasAHeaderStartingWith(String headerName, String prefixValue) {
        String actualHeaderValue = requestContext.getResponse().getFirstHeader(headerName).getValue();
        assertTrue(actualHeaderValue.startsWith(prefixValue));
    }

    @And("the response body contains {string}")
    public void theResponseBodyContains(String expectedBodyContains) throws IOException {
        String actualBody = runner.variable("responseBody", interpolate("responseBody"));
        log.info("expectedBody contains: {}", expectedBodyContains);
        log.info("actualBody: {}", actualBody);
        assertContains(expectedBodyContains, actualBody);
    }

    @And("the response redirects to url {string} with")
    public void theResponseRedirectsToUrlWith(String url, List<String> parts) {

        final TestContext testContext = messageOperations.getTestContext(runner);
        final String actualLocation = requestContext.getResponse()
                .getFirstHeader("location")
                .getValue();

        final String expectedLocation = String.format("%s/%s", url, parts.stream()
                .reduce((accumulator, value) -> String.format("%s/%s",
                        testContext.getVariable(nameFrom(accumulator, "id")),
                        testContext.getVariable(nameFrom(value, "id"))
                ))
                .orElse(""));

        assertEquals(expectedLocation, actualLocation);
    }

    @When("the LTI Launch Request is submitted with a {string} pi session")
    public void theLTILaunchRequestIsSubmittedWithAPiSession(String piSessionState) throws IOException {

        // The Citrus runner http acts like a real browser in some cases; in particular, it follows
        // SEE_OTHER or 303 status codes. We really only want to assert that a 303 was sent, not follow the
        // Location sent. So, we are managing our own HTTP client.

        CloseableHttpClient instance = HttpClientBuilder.create().disableRedirectHandling().build();

        // in PROD, the request for the launch url will hit Fastly; this will be rewritten to:
        //  .../sso/lti-1-1/launch-request?continue_to=<url>
        String launchRequestUrl = runner.variable("launchRequestUrl", interpolate("launchRequestUrl"));
        String postUrl = String.format("http://%s:%s/sso/lti-1-1/launch-request?continue_to=%s", //
                backendHost, backendPort, //
                URLEncoder.encode(launchRequestUrl, "UTF-8"));
        HttpPost httpPost = new HttpPost(postUrl);

        // build the body
        String _signedParameters = runner.variable("signedLaunchParameters",
                interpolate("signedLaunchParameters"));
        StringEntity entity = new StringEntity(_signedParameters,
                ContentType.create(URLEncodedUtils.CONTENT_TYPE, (Charset) null));
        httpPost.setEntity(entity);
        if (piSessionState.equals("valid")) {
            // mock the ies cookie to allow the request to succeed on the ies flow
            httpPost.setHeader("Cookie", String.format("PiAuthSession=%s", IESTokenMock));
        }

        // execute it
        requestContext = new HttpCoreContext();
        //HttpResponse response = instance.execute(httpPost, requestContext);
        instance.execute(httpPost, requestContext);

        // add some spiffy logging.
        log.info("* Trying {}", requestContext.getTargetHost().toHostString());
        log.info("> {}", requestContext.getRequest().getRequestLine());
        Header[] headers = requestContext.getRequest().getAllHeaders();
        for (Header header : headers) {
            log.info("> {}", header.toString());
        }
        log.info(">");
        //
        log.info("< " + requestContext.getResponse().getStatusLine());
        Arrays.stream(requestContext.getResponse().getAllHeaders()) //
                .map(Object::toString) //
                .forEach(entry -> log.info("< {}", entry));
        log.info("<");
        // the response comes back in a Stream, so log & preserve it.
        String responseBody = Strings.nullToEmpty(EntityUtils.toString(requestContext.getResponse().getEntity()));
        runner.createVariable("responseBody", responseBody);
        log.info("{}", responseBody);
    }

    @And("the response body contains")
    public void theResponseBodyContains(final Map<String, String> expectedVals) {
        String body = messageOperations.getTestContext(runner).getVariable("responseBody");

        for (Map.Entry<String, String> entry : expectedVals.entrySet()) {
            assertTrue(body.contains(entry.getKey()));
            String value = extractValue(body, entry.getKey());
            runner.variable(entry.getValue(), value);
        }
    }

    private String extractValue(String body, String key) {
        final String fullKey = key + "\" value=";
        int index = body.indexOf(fullKey) + fullKey.length() - 1;

        final String QUOTE = "\"";

        StringBuilder extracted = new StringBuilder();
        boolean started = false;

        for (int i = index; i < body.length() - 1; i++) {
            char character = body.charAt(i);
            if (QUOTE.indexOf(character) != -1 && started) {
                return extracted.toString();
            }
            if (QUOTE.indexOf(character) != -1) {
                started = true;
                continue;
            }
            if (started) {
                extracted.append(character);
            }
        }
        return null;
    }

    @When("a pi session is initialised for LTI hash {string} and launch {string}")
    public void aPiSessionIsInitialisedForLTI(final String hash, final String launch) throws IOException {
        TestContext testContext = messageOperations.getTestContext(runner);

        Map<String, String> params = new HashMap<>();
        // add a random userId and token
        params.put("piUserId", Hashing.string(UUID.randomUUID().toString()));
        params.put("piToken", Hashing.string(UUID.randomUUID().toString()));
        params.put("hash", testContext.getVariable(hash));
        params.put("launchRequestId", testContext.getVariable(launch));

        String payload = PayloadBuilder.getUrlEncodedForm(params);

        CloseableHttpClient instance = HttpClientBuilder.create().disableRedirectHandling().build();

        // in PROD, the request for the launch url will hit Fastly; this will be rewritten to:
        //  .../sso/lti-1-1/launch-request?continue_to=<url>
        String postUrl = String.format("http://%s:%s/sso/lti-1-1/launch-request-continue", //
                backendHost, backendPort);
        HttpPost httpPost = new HttpPost(postUrl);

        // build the body
        StringEntity entity = new StringEntity(payload,
                ContentType.create(URLEncodedUtils.CONTENT_TYPE, (Charset) null));
        httpPost.setEntity(entity);


        // execute it
        requestContext = new HttpCoreContext();
        //HttpResponse response = instance.execute(httpPost, requestContext);
        instance.execute(httpPost, requestContext);

        // add some spiffy logging.
        log.info("* Trying {}", requestContext.getTargetHost().toHostString());
        log.info("> {}", requestContext.getRequest().getRequestLine());
        Header[] headers = requestContext.getRequest().getAllHeaders();
        for (Header header : headers) {
            log.info("> {}", header.toString());
        }
        log.info(">");
        //
        log.info("< " + requestContext.getResponse().getStatusLine());
        Arrays.stream(requestContext.getResponse().getAllHeaders()) //
                .map(Object::toString) //
                .forEach(entry -> log.info("< {}", entry));
        log.info("<");
        // the response comes back in a Stream, so log & preserve it.
        String responseBody = Strings.nullToEmpty(EntityUtils.toString(requestContext.getResponse().getEntity()));
        runner.createVariable("responseBody", responseBody);
        log.info("{}", responseBody);

    }
}
