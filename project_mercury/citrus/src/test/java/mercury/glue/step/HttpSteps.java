package mercury.glue.step;

import static mercury.common.Variables.interpolate;
import static mercury.common.Variables.nameFrom;
import static mercury.glue.wiring.CitrusConfiguration.HTTP_CLIENT;
import static mercury.glue.wiring.CitrusConfiguration.REST_CLIENT;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;

import com.consol.citrus.annotations.CitrusResource;
import com.consol.citrus.dsl.runner.TestRunner;
import com.consol.citrus.exceptions.CitrusRuntimeException;
import com.consol.citrus.http.client.HttpClient;
import com.google.common.net.HttpHeaders;

import cucumber.api.java.en.Then;
import cucumber.api.java.en.When;
import mercury.common.CitrusAssert;

public class HttpSteps {

    @CitrusResource
    private TestRunner runner;

    @Autowired
    @Qualifier(HTTP_CLIENT)
    private HttpClient httpClient;

    @Autowired
    @Qualifier(REST_CLIENT)
    private HttpClient restClient;

    @When("^\"([^\"]*)\" requests via http \"([^\"]*)\" with query parameter named \"([^\"]*)\" with value \"([^\"]*)\"$")
    public void namedUserRequestsResourceWithQueryString(String accountName,
            String url,
            String param,
            String paramValue) throws UnsupportedEncodingException {
        final String encodedParam = URLEncoder.encode(paramValue, "UTF-8");
        runner.http(action -> action.client(httpClient).send().get(url).queryParam(param, encodedParam));

        // parse out the bearer token.
        String bearerToken = "";
        try {
            bearerToken = runner.variable(accountName + "_bearerToken", interpolate(accountName + "_bearerToken"));
        } catch (CitrusRuntimeException ex) {
            if (!ex.getMessage().contains("Unknown variable")) {
                throw ex;
            }
        }

        String finalBearerToken = bearerToken;
        runner.http(builder -> builder.client(httpClient) //
                .send() //
                .get(url) //
                .queryParam(param, encodedParam) //
                .header("Authorization", "Bearer " + finalBearerToken));
    }

    @Then("^the server should respond with a response body of '(.*)'$")
    public void userReceivesResponseSingleQuoted(String response) {
        runner.http(action -> action.client(httpClient).receive().response(HttpStatus.OK).payload(response));
    }

    @Then("^the server should respond with http status \"([^\"]*)\" \"([^\"]*)\" and error message \"([^\"]*)\"$")
    public void mercuryShouldRespondWithHttpStatusAndErrorMessage(String httpStatus, String type, String errorMessage) {
        runner.http(action -> action.client(httpClient)
                .receive()
                .response()
                .payload("{" + "\"status\":" + httpStatus + "," + "\"type\":\"" + type + "\"," + "\"message\":\""
                                 + errorMessage + "\"" + "}"));
    }

    @Then("{string} gets a {int}")
    public void getsA(final String accountName, final int expectedStatusCode) {

        // perform a ping call so we gain access to the test context
        runner.http(builder -> builder.client(restClient) //
                .send() //
                .get("/ping"));

        runner.http(action -> action.client(restClient)
                .receive()
                .response()
                .validationCallback((message, context) -> {
                    final int actualStatusCode = Integer.parseInt(context.getVariable(nameFrom(accountName, "statusCode")));
                    assertEquals(expectedStatusCode, actualStatusCode);
                }));
    }

    /**
     * This test perform an ignored get call on the /r/ping endpoint for the sole purpose of gaining access to the
     * citrus TestContext which is required to access context variables and perform assertions. The real get request
     * is performed using an HttpURLConnection. The reason behind this workaround is that when the http apache client
     * used by citrus is configured to follow the redirect when a 303 status code is returned. Unfortunately citrus
     * does not expose a way to change the http client configurations via its own client interface.
     *
     * @param accountName the account performing the get request
     * @param url the uri to get (contains variable names that needs to be access via test context)
     * @param headers headers to attached to the request (currently supports Forwarded only)
     */
    @When("{string} get redirect url {string} with headers")
    public void getRedirectUrlWithHeaders(final String accountName, final String url, final Map<String, String> headers) {

        final String[] parts = url.split("/");

        if (parts.length < 4) {
            CitrusAssert.fail("invalid url");
        }

        final String productIdVar = parts[3];

        // perform a ping call so we gain access to the test context
        runner.http(builder -> builder.client(restClient) //
                .send() //
                .get("/ping"));

        runner.http(action -> action.client(restClient)
                .receive()
                .response()
                .validationCallback((message, context) -> {
                    String productId;
                    try {
                        productId = context.getVariable(productIdVar);
                    } catch (CitrusRuntimeException e) {
                        // when the variable does not exist then it is not a variable it's the value
                        productId = productIdVar;
                    }

                    final String relativeUrl = String.format("/to/%s/%s", parts[2], productId);

                    final String requestUrl = String.format("%s%s", httpClient.getEndpointConfiguration().getRequestUrl(), relativeUrl);

                    try {
                        // perform the get request via url connection
                        final HttpURLConnection con = (HttpURLConnection)(new URL(requestUrl).openConnection());
                        con.setRequestProperty(HttpHeaders.FORWARDED, headers.get(HttpHeaders.FORWARDED));
                        con.setInstanceFollowRedirects( false );
                        con.connect();

                        // read the response headers
                        final Map<String, List<String>> responseHeaders = con.getHeaderFields();
                        final List<String> locations = responseHeaders.get(HttpHeaders.LOCATION);

                        // if there is a location then the request was successful
                        if (locations != null && !locations.isEmpty()) {
                            context.setVariable(nameFrom(accountName, "location"), locations.get(0));
                        }
                        // always save the status code
                        context.setVariable(nameFrom(accountName, "statusCode"), con.getResponseCode());
                        // done
                    } catch (IOException e) {
                        CitrusAssert.fail("failed to perform get request");
                    }
                }));
    }

    @Then("{string} is redirected to {string}")
    public void isRedirectedTo(final String accountName, final String expectedUrl) throws URISyntaxException {
        final String[] parts = expectedUrl.split("/");

        if (parts.length < 5) {
            CitrusAssert.fail("invalid url");
        }

        final String cohortVar = parts[3];
        final String deploymentVar = parts[4];

        final URI expectedUri = new URI(String.format("%s//%s", parts[0], parts[2]));
        // perform a ping call so we gain access to the test context
        runner.http(builder -> builder.client(restClient) //
                .send() //
                .get("/ping"));

        runner.http(action -> action.client(restClient)
                .receive()
                .response()
                .validationCallback((message, context) -> {
                    // first check that we got a 303
                    int statusCode = Integer.parseInt(context.getVariable(nameFrom(accountName, "statusCode")));
                    assertEquals(HttpStatus.SEE_OTHER.value(), statusCode);
                    // access the variables
                    final String cohortId = context.getVariable(cohortVar);
                    final String deploymentId = context.getVariable(deploymentVar);
                    final String actualLocation = context.getVariable(nameFrom(accountName, "location"));
                    final String expectedLocation = String.format("%s://%s/%s/%s", expectedUri.getScheme(), expectedUri.getHost(), cohortId, deploymentId);

                    assertEquals(expectedLocation, actualLocation);
                }));
    }
}
