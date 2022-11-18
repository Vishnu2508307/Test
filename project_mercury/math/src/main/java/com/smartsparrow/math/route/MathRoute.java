package com.smartsparrow.math.route;

import static com.smartsparrow.dataevent.RouteUri.MATH_ASSET_GET;
import static com.smartsparrow.dataevent.RouteUri.RS;

import javax.inject.Inject;

import org.apache.camel.Exchange;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.http.HttpMethods;
import org.apache.camel.http.common.HttpOperationFailedException;
import org.json.JSONObject;

import com.smartsparrow.dataevent.data.HttpOperationFailedProcessor;
import com.smartsparrow.math.config.MathConfig;
import com.smartsparrow.math.event.MathAssetEventMessage;
import com.smartsparrow.util.log.MercuryLogger;
import com.smartsparrow.util.log.MercuryLoggerFactory;

public class MathRoute extends RouteBuilder {

    private static final MercuryLogger log = MercuryLoggerFactory.getLogger(MathRoute.class);

    public static final String MATH_ASSET_GET_EVENT_MESSAGE = "math.asset.get.event.message";
    public static final String SUBMIT_MATH_RESOLVER_REQUEST = "direct:submit_math_resolver_request";
    public static final String ASSET_MATH_RESOLVER_QUEUE = "direct:submit_math_resolver_retry_queue";

    @Inject
    private MathConfig mathMLConfig;

    @Override
    public void configure() {

        from(RS + MATH_ASSET_GET)
                // set the route id
                .routeId(MATH_ASSET_GET)
                // set the event message property
                .setProperty(MATH_ASSET_GET_EVENT_MESSAGE, body())
                // set the headers
                .setHeader(Exchange.HTTP_METHOD, constant(HttpMethods.POST))
                .setHeader(Exchange.HTTP_URI, constant(mathMLConfig.getMathMLUri()))
                .setHeader(Exchange.HTTP_PATH, simple(mathMLConfig.getMathMLPath()))
                .setHeader(Exchange.CONTENT_TYPE, constant("application/x-www-form-urlencoded; charset=UTF-8"))
                // set the body
                .setBody(exchange -> {
                    MathAssetEventMessage message = exchange.getProperty(MATH_ASSET_GET_EVENT_MESSAGE,
                                                                         MathAssetEventMessage.class);
                    return simple("mml=" + message.getMathML() +
                                          "&metrics=" + mathMLConfig.getMetrics() +
                                          "&centerbaseline=" + mathMLConfig.getCenterbaseline()).getText();
                })
                // handle the http failure by routing to the http failed processor
                .onException(HttpOperationFailedException.class)
                .process(new HttpOperationFailedProcessor(exchange -> exchange.getProperty(
                        MATH_ASSET_GET_EVENT_MESSAGE,
                        MathAssetEventMessage.class)))
                .handled(true)
                .stop()
                .end()
                // perform the http request
                .to("https:showimage?connectionClose=true&httpClient.cookiePolicy=ignoreCookies")
                // mark the event message as valid if the response is successful
                .process(exchange -> {
                    MathAssetEventMessage message = exchange.getProperty(MATH_ASSET_GET_EVENT_MESSAGE,
                                                                         MathAssetEventMessage.class);
                    Integer httpCode = (Integer) exchange.getIn().getHeader(Exchange.HTTP_RESPONSE_CODE);
                    if (httpCode == 200) {
                        try {
                            JSONObject data = new JSONObject(exchange.getIn().getBody(String.class));
                            if (data.has("status") && data.getString("status").equals("ok")) {
                                JSONObject result = data.getJSONObject("result");
                                message.setHeight(result.getInt("height"));
                                message.setWidth(result.getInt("width"));
                                message.setContent(result.getString("content"));
                                message.setBaseline(result.getInt("baseline"));
                                message.setFormat(result.getString("format"));
                                message.setAlt(result.has("alt") ? result.getString("alt") : "");
                                message.setRole(result.getString("role"));
                            }
                            exchange.getMessage().setBody(message);
                        } catch (Exception ex) {
                            log.error("MATH_ASSET_GET route returned unexpected data object");
                        }
                    } else {
                        log.error("MATH_ASSET_GET route returned unexpected status code: " + httpCode);
                    }
                });

        // process events sent to the "Submit" topic.
        from(SUBMIT_MATH_RESOLVER_REQUEST)
                .toD("aws-sns://" + mathMLConfig.getSubmitTopicNameOrArn());

        // process messages to be sent to the retry/delay queue
        from(ASSET_MATH_RESOLVER_QUEUE)
                .toD("aws-sqs://" + mathMLConfig.getDelayQueueNameOrArn());
    }

}
