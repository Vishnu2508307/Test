package com.smartsparrow.user_content.route;

import javax.inject.Inject;

import org.apache.camel.builder.RouteBuilder;

import com.smartsparrow.user_content.wiring.UserContentConfig;

public class UserContentRoute extends RouteBuilder {

    public static final String USER_CONTENT_REQUEST = "user-content-request";
    @Inject
    UserContentConfig userContentConfig;

    public void configure() throws Exception {
        // process events sent to the "Submit" topic.
                from("reactive-streams:" + USER_CONTENT_REQUEST)
                        //Need to find should we handle error
                        .toD("aws-sns://" + userContentConfig.getCacheNameOrArn());
    }
}
