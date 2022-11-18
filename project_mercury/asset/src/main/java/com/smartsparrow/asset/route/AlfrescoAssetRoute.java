package com.smartsparrow.asset.route;

import static com.smartsparrow.data.Headers.ALFRESCO_AZURE_AUTHORIZATION_HEADER;
import static com.smartsparrow.dataevent.RouteUri.ALFRESCO_NODE_CHILDREN;
import static com.smartsparrow.dataevent.RouteUri.ALFRESCO_NODE_CONTENT;
import static com.smartsparrow.dataevent.RouteUri.ALFRESCO_NODE_INFO;
import static com.smartsparrow.dataevent.RouteUri.ALFRESCO_NODE_PULL;
import static com.smartsparrow.dataevent.RouteUri.RS;

import java.io.InputStream;
import java.time.LocalDateTime;
import java.time.ZoneOffset;

import com.smartsparrow.asset.eventmessage.AlfrescoNodeChildrenEventMessage;
import com.smartsparrow.asset.eventmessage.AlfrescoNodeContentEventMessage;
import com.smartsparrow.asset.eventmessage.AlfrescoNodeInfoEventMessage;
import com.smartsparrow.asset.service.AlfrescoNodeChildren;
import com.smartsparrow.asset.service.AlfrescoNodeInfo;
import com.smartsparrow.config.AssetConfig;

import org.apache.camel.Exchange;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.http.HttpMethods;
import org.apache.camel.http.common.HttpOperationFailedException;

import com.smartsparrow.asset.eventmessage.AlfrescoAssetPullEventMessage;
import com.smartsparrow.dataevent.data.HttpOperationFailedProcessor;
import com.smartsparrow.util.log.MercuryLogger;
import com.smartsparrow.util.log.MercuryLoggerFactory;

import org.json.JSONArray;
import org.json.JSONObject;

import javax.inject.Inject;

public class AlfrescoAssetRoute extends RouteBuilder {

    private static final MercuryLogger log = MercuryLoggerFactory.getLogger(AlfrescoAssetRoute.class);

    public static final String ALFRESCO_NODE_CHILDREN_EVENT_MESSAGE = "alfresco.node.children.event.message";
    public static final String ALFRESCO_NODE_CONTENT_EVENT_MESSAGE = "alfresco.node.content.event.message";
    public static final String ALFRESCO_NODE_INFO_EVENT_MESSAGE = "alfresco.node.info.event.message";
    public static final String ALFRESCO_NODE_PULL_EVENT_MESSAGE = "alfresco.node.pull.event.message";

    @Inject
    private AssetConfig assetConfig;

    @Override
    public void configure() {

        from(RS + ALFRESCO_NODE_PULL)
                // set the route id
                .routeId(ALFRESCO_NODE_PULL)
                // set the event message property
                .setProperty(ALFRESCO_NODE_PULL_EVENT_MESSAGE, body())
                // add the token from the body to the header
                .process(exchange -> {
                    AlfrescoAssetPullEventMessage message = exchange.getProperty(ALFRESCO_NODE_PULL_EVENT_MESSAGE, AlfrescoAssetPullEventMessage.class);
                    exchange.getOut().setHeader(ALFRESCO_AZURE_AUTHORIZATION_HEADER, message.getMyCloudToken());
                    exchange.getOut().setHeader(Exchange.HTTP_URI, message.getAlfrescoUrl());
                })
                .setHeader(Exchange.HTTP_METHOD, constant(HttpMethods.GET))
                .log("Alfresco pull node: set header completed")
                // handle any failure by setting the fault
                .onException(HttpOperationFailedException.class)
                .process(new HttpOperationFailedProcessor(exchange -> {
                    log.error(String.format("Alfresco pull node exception : %s", exchange.getException().getMessage()));
                    return exchange.getProperty(ALFRESCO_NODE_PULL_EVENT_MESSAGE, AlfrescoAssetPullEventMessage.class);
                }))
                .handled(true)
                .stop()
                .end()
                .log("Alfresco pull node: start Alfresco request")
                // perform the http request
                .to("https:alfrescoNodePull?connectionClose=true&httpClient.cookiePolicy=ignoreCookies")
                .log("Alfresco pull node: call request completed")
                // process the response
                .process(exchange -> {
                    AlfrescoAssetPullEventMessage message = exchange.getProperty(ALFRESCO_NODE_PULL_EVENT_MESSAGE, AlfrescoAssetPullEventMessage.class);
                    message.setInputStream((InputStream) exchange.getIn().getBody());
                    exchange.getOut().setBody(message);
                })
                .log("Alfresco pull node: get asset inputStream completed");

        from(RS + ALFRESCO_NODE_CHILDREN)
                // set the route id
                .routeId(ALFRESCO_NODE_CHILDREN)
                // set the event message property
                .setProperty(ALFRESCO_NODE_CHILDREN_EVENT_MESSAGE, body())
                // add the token from the body to the header
                .process(exchange -> {
                    AlfrescoNodeChildrenEventMessage message = exchange.getProperty(ALFRESCO_NODE_CHILDREN_EVENT_MESSAGE, AlfrescoNodeChildrenEventMessage.class);
                    exchange.getOut().setHeader("alfrescoNodeId", message.getNodeId());
                    exchange.getOut().setHeader(ALFRESCO_AZURE_AUTHORIZATION_HEADER, message.getMyCloudToken());
                })
                .setHeader(Exchange.HTTP_METHOD, constant(HttpMethods.GET))
                .setHeader(Exchange.HTTP_URI, constant(assetConfig.getAlfrescoUrl()))
                .setHeader(Exchange.HTTP_PATH, simple("/alfresco/api/-default-/public/alfresco/versions/1/nodes/${in.header.alfrescoNodeId}/children"))
                .removeHeader("alfrescoNodeId")
                // handle any failure by setting the fault
                .onException(HttpOperationFailedException.class)
                .process(new HttpOperationFailedProcessor(exchange -> {
                    AlfrescoNodeChildrenEventMessage event = exchange.getProperty(ALFRESCO_NODE_CHILDREN_EVENT_MESSAGE, AlfrescoNodeChildrenEventMessage.class);

                    HttpOperationFailedException ex = exchange.getProperty(Exchange.EXCEPTION_CAUGHT, HttpOperationFailedException.class);
                    String errorMessage = ex.getStatusCode() + " " + ex.getStatusText();
                    event.setErrorMessage(String.format("%s", errorMessage));
                    log.error(String.format("Alfresco get node children endpoint error: %s", errorMessage));

                    return event;
                }))
                .handled(true)
                .stop()
                .end()
                // perform the http request
                .to("https:alfrescoNodeChildren?connectionClose=true&httpClient.cookiePolicy=ignoreCookies")
                // mark the event message as valid if the response is successful
                .process(exchange -> {
                    AlfrescoNodeChildrenEventMessage message = exchange.getProperty(ALFRESCO_NODE_CHILDREN_EVENT_MESSAGE, AlfrescoNodeChildrenEventMessage.class);
                    JSONObject data = new JSONObject(exchange.getIn().getBody(String.class));
                    if (data.has("list")) {
                        AlfrescoNodeChildren alfrescoNodeChildren = new AlfrescoNodeChildren().setId(message.getNodeId());

                        JSONObject list = data.getJSONObject("list");
                        if (list.has("entries")) {
                            JSONArray entries = list.getJSONArray("entries");
                            for (int i = 0; i < entries.length(); i++) {
                                JSONObject object = entries.getJSONObject(i);
                                if (object.has("entry")) {
                                    JSONObject entry = object.getJSONObject("entry");
                                    if (entry.has("id")) {
                                        alfrescoNodeChildren.addChild(entry.getString("id"));
                                    }
                                }
                            }
                        }

                        message.setAlfrescoNodeChildren(alfrescoNodeChildren);
                    } else {
                        message.setErrorMessage("422 Unprocessable entity");
                    }
                    // simply return the message
                    exchange.getOut().setBody(message);
                });

        from(RS + ALFRESCO_NODE_INFO)
                // set the route id
                .routeId(ALFRESCO_NODE_INFO)
                // set the event message property
                .setProperty(ALFRESCO_NODE_INFO_EVENT_MESSAGE, body())
                // add the token from the body to the header
                .process(exchange -> {
                    AlfrescoNodeInfoEventMessage message = exchange.getProperty(ALFRESCO_NODE_INFO_EVENT_MESSAGE, AlfrescoNodeInfoEventMessage.class);
                    exchange.getOut().setHeader("alfrescoNodeId", message.getNodeId());
                    exchange.getOut().setHeader(ALFRESCO_AZURE_AUTHORIZATION_HEADER, message.getMyCloudToken());
                })
                .setHeader(Exchange.HTTP_METHOD, constant(HttpMethods.GET))
                .setHeader(Exchange.HTTP_URI, constant(assetConfig.getAlfrescoUrl()))
                .setHeader(Exchange.HTTP_PATH, simple("/alfresco/api/-default-/public/alfresco/versions/1/nodes/${in.header.alfrescoNodeId}?include=path"))
                .removeHeader("alfrescoNodeId")
                // perform the http request
                .to("https:alfrescoNodeInfo?connectionClose=true&httpClient.cookiePolicy=ignoreCookies")
                // mark the event message as valid if the response is successful
                .process(exchange -> {
                    AlfrescoNodeInfoEventMessage message = exchange.getProperty(ALFRESCO_NODE_INFO_EVENT_MESSAGE, AlfrescoNodeInfoEventMessage.class);
                    JSONObject data = new JSONObject(exchange.getIn().getBody(String.class));
                    if (data.has("entry")) {
                        JSONObject entry = data.getJSONObject("entry");
                        JSONObject content = entry.getJSONObject("content");
                        JSONObject props = entry.getJSONObject("properties");

                        // convert modifiedAt epoch string to long
                        String modifiedAt = entry.getString("modifiedAt");
                        modifiedAt = modifiedAt.substring(0, modifiedAt.indexOf('.')); // '2021-02-03T02:05:47.372+0000' -> '2021-02-03T02:05:47'
                        long modifiedAtEpoch = LocalDateTime.parse(modifiedAt).toEpochSecond(ZoneOffset.UTC);

                        String pathName = entry.getJSONObject("path").getString("name");

                        message.setAlfrescoNodeInfo(
                                new AlfrescoNodeInfo()
                                        .setId(message.getNodeId())
                                        .setName(entry.getString("name"))
                                        .setModifiedAt(modifiedAtEpoch)
                                        .setVersion(props.getString("cm:versionLabel"))
                                        .setMimeType(content.getString("mimeType"))
                                        .setWidth(props.getDouble("exif:pixelXDimension"))
                                        .setHeight(props.getDouble("exif:pixelYDimension"))
                                        .setAltText((props.has("cplg:altText")) ? props.getString("cplg:altText") : "")
                                        .setLongDesc((props.has("cplg:longDescription")) ? props.getString("cplg:longDescription") : "")
                                        .setPathName(pathName)
                                        .setWorkURN((props.has("cp:workURN")) ? props.getString("cp:workURN") : "")
                        );
                    }
                    // simply return the message
                    exchange.getOut().setBody(message);
                });

        from(RS + ALFRESCO_NODE_CONTENT)
                // set the route id
                .routeId(ALFRESCO_NODE_CONTENT)
                // set the event message property
                .setProperty(ALFRESCO_NODE_CONTENT_EVENT_MESSAGE, body())
                // add the token from the body to the header
                .process(exchange -> {
                    AlfrescoNodeContentEventMessage message = exchange.getProperty(ALFRESCO_NODE_CONTENT_EVENT_MESSAGE, AlfrescoNodeContentEventMessage.class);
                    exchange.getOut().setHeader("alfrescoNodeId", message.getNodeId());
                    exchange.getOut().setHeader(ALFRESCO_AZURE_AUTHORIZATION_HEADER, message.getMyCloudToken());
                })
                .setHeader(Exchange.HTTP_METHOD, constant(HttpMethods.GET))
                .setHeader(Exchange.HTTP_URI, constant(assetConfig.getAlfrescoUrl()))
                .setHeader(Exchange.HTTP_PATH, simple("/alfresco/api/-default-/public/alfresco/versions/1/nodes/${in.header.alfrescoNodeId}/content"))
                .removeHeader("alfrescoNodeId")
                // perform the http request
                .to("https:alfrescoNodeContent?connectionClose=true&httpClient.cookiePolicy=ignoreCookies")
                // mark the event message as valid if the response is successful
                .process(exchange -> {
                    AlfrescoNodeContentEventMessage message = exchange.getProperty(ALFRESCO_NODE_CONTENT_EVENT_MESSAGE, AlfrescoNodeContentEventMessage.class);
                    message.setContentStream((InputStream) exchange.getIn().getBody());
                    exchange.getOut().setBody(message);
                });
    }
}
