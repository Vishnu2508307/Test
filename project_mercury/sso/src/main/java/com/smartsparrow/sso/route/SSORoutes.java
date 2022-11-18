package com.smartsparrow.sso.route;

import static com.smartsparrow.data.Headers.MYCLOUD_AZURE_AUTHORIZATION_HEADER;
import static com.smartsparrow.data.Headers.PI_AUTHORIZATION_HEADER;
import static com.smartsparrow.dataevent.RouteUri.IES_BATCH_PROFILE_GET;
import static com.smartsparrow.dataevent.RouteUri.IES_PROFILE_GET;
import static com.smartsparrow.dataevent.RouteUri.IES_TOKEN_VALIDATE;
import static com.smartsparrow.dataevent.RouteUri.MYCLOUD_PROFILE_GET;
import static com.smartsparrow.dataevent.RouteUri.MYCLOUD_TOKEN_VALIDATE;
import static com.smartsparrow.dataevent.RouteUri.REGISTRAR_SECTION_ROLE_GET;
import static com.smartsparrow.dataevent.RouteUri.RS;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.inject.Inject;

import org.apache.camel.Exchange;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.http.HttpMethods;
import org.apache.camel.http.common.HttpOperationFailedException;
import org.apache.http.entity.ContentType;
import org.json.JSONArray;
import org.json.JSONObject;

import com.smartsparrow.dataevent.data.HttpOperationFailedProcessor;
import com.smartsparrow.iam.wiring.IesSystemToSystemIdentityProvider;
import com.smartsparrow.sso.event.IESBatchProfileGetEventMessage;
import com.smartsparrow.sso.event.IESBatchProfileGetParams;
import com.smartsparrow.sso.event.IESProfileGetEventMessage;
import com.smartsparrow.sso.event.IESTokenValidationEventMessage;
import com.smartsparrow.sso.event.MyCloudProfileGetEventMessage;
import com.smartsparrow.sso.event.MyCloudTokenValidationEventMessage;
import com.smartsparrow.sso.event.RegistrarSectionRoleGetEventMessage;
import com.smartsparrow.sso.service.IdentityProfile;
import com.smartsparrow.sso.service.SectionRole;
import com.smartsparrow.sso.wiring.IESConfig;
import com.smartsparrow.sso.wiring.MyCloudConfig;
import com.smartsparrow.sso.wiring.RegistrarConfig;
import com.smartsparrow.util.Json;
import com.smartsparrow.util.log.MercuryLogger;
import com.smartsparrow.util.log.MercuryLoggerFactory;

public class SSORoutes extends RouteBuilder {
    private static final MercuryLogger log = MercuryLoggerFactory.getLogger(SSORoutes.class);

    public static final String IES_TOKEN_VALIDATE_EVENT_MESSAGE = "ies.token.validate.event.message";
    public static final String IES_PROFILE_GET_EVENT_MESSAGE = "ies.profile.get.event.message";
    public static final String IES_BATCH_PROFILE_GET_EVENT_MESSAGE = "ies.batch.profile.get.event.message";
    public static final String MYCLOUD_TOKEN_VALIDATE_EVENT_MESSAGE = "mycloud.token.validate.event.message";
    public static final String MYCLOUD_PROFILE_GET_EVENT_MESSAGE = "mycloud.profile.get.event.message";
    public static final String REGISTRAR_SECTION_ROLE_GET_EVENT_MESSAGE = "registrar.section.role.get.event.message";

    private final IESConfig iesConfig;
    private final MyCloudConfig myCloudConfig;
    private final RegistrarConfig registrarConfig;
    private final IesSystemToSystemIdentityProvider identityProvider;

    @Inject
    public SSORoutes(final IESConfig iesConfig,
                     final MyCloudConfig myCloudConfig,
                     final RegistrarConfig registrarConfig,
                     final IesSystemToSystemIdentityProvider identityProvider) {
        this.iesConfig = iesConfig;
        this.myCloudConfig = myCloudConfig;
        this.registrarConfig = registrarConfig;
        this.identityProvider = identityProvider;
    }

    @Override
    public void configure() {

        // IES token validation route
        from(RS + IES_TOKEN_VALIDATE)
                // set the route id
                .routeId(IES_TOKEN_VALIDATE)
                // set the event message property
                .setProperty(IES_TOKEN_VALIDATE_EVENT_MESSAGE, body())
                // add the token from the body to the header
                .process(exchange -> {
                    IESTokenValidationEventMessage message = exchange.getProperty(IES_TOKEN_VALIDATE_EVENT_MESSAGE, IESTokenValidationEventMessage.class);
                    exchange.getOut().setHeader("token", message.getToken());
                })
                // set the token to the authorization header
                .setHeader(PI_AUTHORIZATION_HEADER, simple("${in.header.token}"))
                // set the request url
                .setHeader(Exchange.HTTP_URI, constant(iesConfig.getBaseUrl()))
                .setHeader(Exchange.HTTP_PATH, constant("/tokens/validatetoken"))
                // handle the http failure by routing to the http failed processor
                .onException(HttpOperationFailedException.class)
                    .process(new HttpOperationFailedProcessor(exchange -> exchange.getProperty(IES_TOKEN_VALIDATE_EVENT_MESSAGE, IESTokenValidationEventMessage.class)))
                    .handled(true)
                    .stop()
                .end()
                // perform the http request
                .to("https:iesValidation")
                 // mark the event message as valid if the response is successful
                .process(exchange -> {
                    IESTokenValidationEventMessage message = exchange.getProperty(IES_TOKEN_VALIDATE_EVENT_MESSAGE, IESTokenValidationEventMessage.class);
                    JSONObject json = new JSONObject(exchange.getIn().getBody(String.class));
                    if (json.has("status") && "success".equals(json.getString("status"))) {
                        if (json.has("data") && json.getBoolean("data")) {
                            // mark the message as valid
                            message.markValid();
                            exchange.getOut().setBody(message);
                        }
                    }
                    // simply return the message
                    exchange.getOut().setBody(message);
                });

        // IES identity profile get route
        from(RS + IES_PROFILE_GET)
                // set the route id
                .routeId(IES_PROFILE_GET)
                // set the event message property
                .setProperty(IES_PROFILE_GET_EVENT_MESSAGE, body())
                // add id and token from the body to the header
                .process(exchange -> {
                    IESProfileGetEventMessage message = exchange.getProperty(IES_PROFILE_GET_EVENT_MESSAGE, IESProfileGetEventMessage.class);
                    exchange.getOut().setHeader("token", message.getAccessToken());
                    exchange.getOut().setHeader("pearsonUid", message.getPearsonUid());
                })
                // set the token to the authorization header
                .setHeader(PI_AUTHORIZATION_HEADER, simple("${in.header.token}"))
                // set the request url and content type
                .setHeader(Exchange.HTTP_URI, constant(iesConfig.getBaseUrl()))
                .setHeader(Exchange.HTTP_PATH, simple("/identityprofiles/${in.header.pearsonUid}"))
                .setHeader(Exchange.CONTENT_TYPE, constant(ContentType.APPLICATION_JSON.toString()))
                // handle the http failure by routing to the http failed processor
                .onException(HttpOperationFailedException.class)
                    .process(new HttpOperationFailedProcessor(exchange -> exchange.getProperty(IES_PROFILE_GET_EVENT_MESSAGE, IESProfileGetEventMessage.class)))
                    .handled(true)
                    .stop()
                .end()
                // perform the http request
                .to("https:iesIdentityProfileGet")
                .process(exchange -> {
                    IESProfileGetEventMessage message = exchange.getProperty(IES_PROFILE_GET_EVENT_MESSAGE, IESProfileGetEventMessage.class);
                    JSONObject json = new JSONObject(exchange.getIn().getBody(String.class));
                    if (json.has("status") && "success".equals(json.getString("status"))) {
                        // get the identity
                        JSONObject data = json.getJSONObject("data");
                        JSONObject identity = data.getJSONObject("identity");
                        JSONArray emails = data.getJSONArray("emails");
                        message.setIdentityProfile(new IdentityProfile()
                                .setId(identity.getString("id"))
                                .setGivenName(data.getString("givenName"))
                                .setFamilyName(data.getString("familyName"))
                                // TODO email object has a isPrimary field use that to get the primary email
                                .setPrimaryEmail(emails.getJSONObject(0).getString("emailAddress")));
                        exchange.getOut().setBody(message);
                    }
                    // simply return the message
                    exchange.getOut().setBody(message);
                });

        // IES get batch of profile
        from(RS + IES_BATCH_PROFILE_GET)
                // set the route id
                .routeId(IES_BATCH_PROFILE_GET)
                // set the event message property
                .setProperty(IES_BATCH_PROFILE_GET_EVENT_MESSAGE, body())
                // add id and token from the body to the header
                .process(exchange -> {
                    IESBatchProfileGetEventMessage message = exchange.getProperty(IES_BATCH_PROFILE_GET_EVENT_MESSAGE,
                                                                                  IESBatchProfileGetEventMessage.class);
                    exchange.getOut().setHeader("token", identityProvider.getPiToken());
                    exchange.getOut().setHeader("piIds", message.getParams());
                })
                // set the token to the authorization header
                .setHeader(PI_AUTHORIZATION_HEADER, simple("${in.header.token}"))
                // set the request url and content type
                .setHeader(Exchange.HTTP_METHOD, constant("POST"))
                .setHeader(Exchange.HTTP_URI, constant(iesConfig.getBaseUrl()))
                .setHeader(Exchange.HTTP_PATH, simple("/identityprofiles/batch"))
                .setHeader(Exchange.CONTENT_TYPE, constant(ContentType.APPLICATION_JSON.toString()))
                // set the body
                .setBody(exchange -> {
                    IESBatchProfileGetParams params = exchange.getIn().getHeader("piIds",
                                                                                 IESBatchProfileGetParams.class);
                    return Json.stringify(params);
                })
                // handle the http failure by routing to the http failed processor
                .onException(HttpOperationFailedException.class)
                .process(new HttpOperationFailedProcessor(exchange -> exchange.getProperty(
                        IES_BATCH_PROFILE_GET_EVENT_MESSAGE,
                        IESBatchProfileGetEventMessage.class)))
                .handled(true)
                .stop()
                .end()
                // perform the http request
                .to("https:iesBatchIdentityProfileGet")
                .process(exchange -> {
                    IESBatchProfileGetEventMessage message = exchange.getProperty(IES_BATCH_PROFILE_GET_EVENT_MESSAGE,
                                                                                  IESBatchProfileGetEventMessage.class);
                    JSONObject json = new JSONObject(exchange.getIn().getBody(String.class));
                    if (json.has("status") && "success".equals(json.getString("status"))) {
                        // map the response body
                        JSONObject data = json.getJSONObject("data");
                        JSONArray users = data.getJSONArray("users");
                        JSONArray notFound = data.getJSONArray("notFound");

                        final List<IdentityProfile> profiles = new ArrayList<>();

                        // start mapping the users profiles
                        for (int i = 0; i < users.length(); i++) {
                            JSONObject current = users.getJSONObject(i);

                            // find id, givenName and familyName
                            final String id = current.getString("piId");
                            final JSONObject profileData = current.getJSONObject("profileData");
                            final String familyName = profileData.getString("familyName");
                            final String givenName = profileData.getString("givenName");

                            JSONArray emails = profileData.getJSONArray("emails");
                            String primaryEmail = "";
                            // of all the emails we are only interested in the primaryEmail
                            for (int j = 0; j < emails.length(); j++) {
                                JSONObject currentEmail = emails.getJSONObject(j);
                                if (Boolean.parseBoolean(currentEmail.getString("isPrimary"))) {
                                    // incredibly the service returns this boolean value as a string
                                    primaryEmail = currentEmail.getString("emailAddress");
                                    break;
                                }
                            }

                            // add the identity profile info to the list
                            profiles.add(new IdentityProfile()
                                                 .setFamilyName(familyName)
                                                 .setGivenName(givenName)
                                                 .setId(id)
                                                 .setPrimaryEmail(primaryEmail));
                        }

                        // map all the profiles that were not found
                        final Iterator<Object> notFoundIterator = notFound.iterator();
                        final List<String> missingProfiles = new ArrayList<>();

                        while (notFoundIterator.hasNext()) {
                            missingProfiles.add(String.valueOf(notFoundIterator.next()));
                        }

                        // set the found profiles to the message
                        message.setIdentityProfile(profiles);
                        // set the missing ids to the message
                        message.setNotFound(missingProfiles);

                    }
                    // return the message
                    exchange.getOut().setBody(message);
                });

        // myCloud token validation route
        from(RS + MYCLOUD_TOKEN_VALIDATE)
                // set the route id
                .routeId(MYCLOUD_TOKEN_VALIDATE)
                // set the event message property
                .setProperty(MYCLOUD_TOKEN_VALIDATE_EVENT_MESSAGE, body())
                // add the token from the body to the header
                .process(exchange -> {
                    MyCloudTokenValidationEventMessage message = exchange.getProperty(MYCLOUD_TOKEN_VALIDATE_EVENT_MESSAGE, MyCloudTokenValidationEventMessage.class);
                    exchange.getOut().setHeader("token", message.getToken());
                })
                // set the request url
                .setHeader(Exchange.HTTP_METHOD, constant(HttpMethods.POST))
                .setHeader(Exchange.HTTP_URI, constant(myCloudConfig.getBaseUrl()))
                .setHeader(Exchange.HTTP_PATH, simple("/auth/json/pearson/sessions/${in.header.token}?_action=validate"))
                .removeHeader("token")
                // handle the http failure by routing to the http failed processor
                .onException(HttpOperationFailedException.class)
                    .process(new HttpOperationFailedProcessor(exchange -> exchange.getProperty(MYCLOUD_TOKEN_VALIDATE_EVENT_MESSAGE, MyCloudTokenValidationEventMessage.class)))
                    .handled(true)
                    .stop()
                .end()
                // perform the http request
                .to("https:myCloudValidation?connectionClose=true&httpClient.cookiePolicy=ignoreCookies")
                // mark the event message as valid if the response is successful
                .process(exchange -> {
                    MyCloudTokenValidationEventMessage message = exchange.getProperty(MYCLOUD_TOKEN_VALIDATE_EVENT_MESSAGE, MyCloudTokenValidationEventMessage.class);
                    Integer httpCode = (Integer)exchange.getIn().getHeader(Exchange.HTTP_RESPONSE_CODE);
                    if (httpCode == 200) {
                        try {
                            JSONObject json = new JSONObject(exchange.getIn().getBody(String.class));
                            if (json.has("valid") && json.getBoolean("valid") && json.has("uid")) {
                                message.setPearsonUid(json.getString("uid"));
                                message.markValid();
                                exchange.getOut().setBody(message);
                            }
                        }
                        catch (Exception ex) {
                            log.error("myCloud service returned unexpected data object");
                            message.markError();
                        }
                    } else {
                        log.error("myCloud service returned unexpected status code: " + httpCode);
                        message.markError();
                    }
                    // simply return the message
                    exchange.getOut().setBody(message);
                });

        // myCloud identity profile get route
        from(RS + MYCLOUD_PROFILE_GET)
                // set the route id
                .routeId(MYCLOUD_PROFILE_GET)
                // set the event message property
                .setProperty(MYCLOUD_PROFILE_GET_EVENT_MESSAGE, body())
                // add id and token from the body to the header
                .process(exchange -> {
                    MyCloudProfileGetEventMessage message = exchange.getProperty(MYCLOUD_PROFILE_GET_EVENT_MESSAGE, MyCloudProfileGetEventMessage.class);
                    exchange.getOut().setHeader("token", message.getAccessToken());
                    exchange.getOut().setHeader("pearsonUid", message.getPearsonUid()); // is this the best way?
                })
                // set the token to the authorization header
                .setHeader(MYCLOUD_AZURE_AUTHORIZATION_HEADER, simple("${in.header.token}"))
                // set the request url and content type
                .setHeader(Exchange.HTTP_METHOD, constant(HttpMethods.GET))
                .setHeader(Exchange.HTTP_URI, constant(myCloudConfig.getBaseUrl()))
                .setHeader(Exchange.HTTP_PATH, simple("/auth/json/pearson/users/${in.header.pearsonUid}"))
                .removeHeader("token")
                .removeHeader("pearsonUid")
                // handle the http failure by routing to the http failed processor
                .onException(HttpOperationFailedException.class)
                    .process(new HttpOperationFailedProcessor(exchange -> exchange.getProperty(MYCLOUD_PROFILE_GET_EVENT_MESSAGE, MyCloudProfileGetEventMessage.class)))
                    .handled(true)
                    .stop()
                .end()
                // perform the http request
                .to("https:myCloudIdentityProfileGet?connectionClose=true&httpClient.cookiePolicy=ignoreCookies")
                .process(exchange -> {
                    MyCloudProfileGetEventMessage message = exchange.getProperty(MYCLOUD_PROFILE_GET_EVENT_MESSAGE, MyCloudProfileGetEventMessage.class);
                    JSONObject json = new JSONObject(exchange.getIn().getBody(String.class));
                    if (json.has("givenName")) {
                        // get the identity
                        JSONArray givenNames = json.getJSONArray("givenName");
                        JSONArray surnames = json.getJSONArray("sn");
                        JSONArray emails = json.getJSONArray("mail");
                        message.setIdentityProfile(new IdentityProfile()
                                .setId(json.getString("_id"))
                                .setGivenName(givenNames.getString(0))
                                .setFamilyName(surnames.getString(0))
                                .setPrimaryEmail(emails.getString(0)));
                        exchange.getOut().setBody(message);
                    }
                    // simply return the message
                    exchange.getOut().setBody(message);
                });

        // registrar section role get route
        from(RS + REGISTRAR_SECTION_ROLE_GET)
                // set the route id
                .routeId(REGISTRAR_SECTION_ROLE_GET)
                // set the event message property
                .setProperty(REGISTRAR_SECTION_ROLE_GET_EVENT_MESSAGE, body())
                // add id and token from the body to the header
                .process(exchange -> {
                    RegistrarSectionRoleGetEventMessage message = exchange.getProperty(REGISTRAR_SECTION_ROLE_GET_EVENT_MESSAGE, RegistrarSectionRoleGetEventMessage.class);
                    exchange.getOut().setHeader("token", message.getAccessToken());
                    exchange.getOut().setHeader("pearsonUid", message.getPearsonUid());
                    exchange.getOut().setHeader("pearsonSectionId", message.getPearsonSectionId());
                })
                // set the token to the authorization header
                .setHeader(PI_AUTHORIZATION_HEADER, simple("${in.header.token}"))
                // set the request url and content type
                .setHeader(Exchange.HTTP_URI, constant(registrarConfig.getBaseUrl()))
                .setHeader(Exchange.HTTP_PATH, simple("/userassociations/${in.header.pearsonUid}/sections/${in.header.pearsonSectionId}"))
                .setHeader(Exchange.CONTENT_TYPE, constant(ContentType.APPLICATION_JSON.toString()))
                // handle the http failure by routing to the http failed processor
                .onException(HttpOperationFailedException.class)
                    .process(new HttpOperationFailedProcessor(exchange -> exchange.getProperty(REGISTRAR_SECTION_ROLE_GET_EVENT_MESSAGE, RegistrarSectionRoleGetEventMessage.class)))
                    .handled(true)
                    .stop()
                .end()
                // perform the http request
                .to("https:registrarSectionRoleGet")
                .process(exchange -> {
                    RegistrarSectionRoleGetEventMessage message = exchange.getProperty(REGISTRAR_SECTION_ROLE_GET_EVENT_MESSAGE, RegistrarSectionRoleGetEventMessage.class);
                    JSONObject json = new JSONObject(exchange.getIn().getBody(String.class));
                    if (json.has("status") && "active".equals(json.getString("status"))) {
                        // get the role
                        JSONObject section = json.getJSONObject("section");
                        message.setSectionRole(new SectionRole()
                                .setId(section.getString("sectionId"))
                                .setRole(json.getString("authgrouptype")));
                        exchange.getOut().setBody(message);
                    }
                    // simply return the message
                    exchange.getOut().setBody(message);
                });

    }
}
