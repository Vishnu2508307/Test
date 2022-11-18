package com.smartsparrow.rtm.route;

import static com.smartsparrow.dataevent.RouteUri.COMPETENCY_DOCUMENT_UPDATE;
import static com.smartsparrow.dataevent.RouteUri.PUBLICATION_JOB_EVENT;
import static com.smartsparrow.dataevent.RouteUri.RS;
import static com.smartsparrow.dataevent.RouteUri.PUBLICATION_OCULUS_STATUS_EVENT;

import javax.inject.Inject;

import org.apache.camel.Exchange;
import org.apache.camel.LoggingLevel;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.http.common.HttpOperationFailedException;
import org.apache.http.entity.ContentType;
import org.json.JSONObject;

import com.smartsparrow.courseware.eventmessage.PublicationOculusEventMessage;
import com.smartsparrow.dataevent.data.HttpOperationFailedProcessor;
import com.smartsparrow.publication.wiring.PublicationConfig;
import com.smartsparrow.rtm.message.handler.competency.CompetencyDocumentEventHandler;
import com.smartsparrow.rtm.message.handler.courseware.publication.PublicationJobHandler;


public class RTMRoutes extends RouteBuilder {

    public static final String COMPETENCY_DOCUMENT_EVENT_MESSAGE = "competencyDocumentEventMessage";
    public static final String PUBLICATION_JOB_EVENT_MESSAGE = "publicationJobEventMessage";
    public static final String PUBLICATION_OCULUS_STATUS_EVENT_MESSAGE = "publicationOculusStatusEventMessage";

    @Inject
    private CompetencyDocumentEventHandler competencyDocumentEventHandler;

    @Inject
    private PublicationJobHandler publicationJobHandler;

    @Inject
    private PublicationConfig publicationConfig;

    @Override
    public void configure() {
        // This route will have handler which calls RTM producer based on competency document action set in the event message
        from(RS + COMPETENCY_DOCUMENT_UPDATE)
                .log(LoggingLevel.DEBUG, "Received competency document update event ${in.body}")
                // set competency document event message
                .setProperty(COMPETENCY_DOCUMENT_EVENT_MESSAGE, body())
                .id(COMPETENCY_DOCUMENT_UPDATE)
                .bean(competencyDocumentEventHandler);


        from(RS + PUBLICATION_JOB_EVENT)
                .id(PUBLICATION_JOB_EVENT)
                .setProperty(PUBLICATION_JOB_EVENT_MESSAGE, body())
                .log(LoggingLevel.DEBUG, "Received publication job event ${in.body}")
                .bean(publicationJobHandler);

        from(RS + PUBLICATION_OCULUS_STATUS_EVENT)
                // set the route id
                .routeId(PUBLICATION_OCULUS_STATUS_EVENT)
                // set the event message property
                .setProperty(PUBLICATION_OCULUS_STATUS_EVENT_MESSAGE, body())
                // add id and token from the body to the header
                .process(exchange -> {
                    PublicationOculusEventMessage message = exchange.getProperty(PUBLICATION_OCULUS_STATUS_EVENT_MESSAGE, PublicationOculusEventMessage.class);
                    exchange.getOut().setHeader("bookId", message.getBookId());
                })
                // set the request url and content type
                .setHeader(Exchange.HTTP_URI, constant(publicationConfig.getBaseUrl()))
                .setHeader(Exchange.HTTP_PATH, simple("/nextext-api/api/nextext/books/${in.header.bookId}"))
                .setHeader(Exchange.CONTENT_TYPE, constant(ContentType.APPLICATION_JSON.toString()))
                // handle the http failure by routing to the http failed processor
                .onException(HttpOperationFailedException.class)
                .process(new HttpOperationFailedProcessor(exchange -> exchange.getProperty(PUBLICATION_OCULUS_STATUS_EVENT_MESSAGE, PublicationOculusEventMessage.class)))
                .handled(true)
                .stop()
                .end()
                // perform the http request
                .to("https:getPublicationOculusStatus")
                .process(exchange -> {
                    PublicationOculusEventMessage message = exchange.getProperty(PUBLICATION_OCULUS_STATUS_EVENT_MESSAGE, PublicationOculusEventMessage.class);
                    JSONObject json = new JSONObject(exchange.getIn().getBody(String.class));
                    if (json.has("bookId")) {
                        // get the identity
                        JSONObject data = json.getJSONObject("metadata");
                        message.setOculusStatus(data.getString("status"));
                        message.setOculusVersion(data.getString("edition"));
                        exchange.getOut().setBody(message);
                    }
                    // simply return the message
                    exchange.getOut().setBody(message);
                });

    }
}
