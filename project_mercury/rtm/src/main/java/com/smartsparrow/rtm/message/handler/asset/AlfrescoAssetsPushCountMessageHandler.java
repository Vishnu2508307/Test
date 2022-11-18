package com.smartsparrow.rtm.message.handler.asset;

import static com.smartsparrow.util.Warrants.affirmArgument;

import java.util.HashMap;
import java.util.List;

import javax.inject.Inject;

import org.apache.http.HttpStatus;
import org.eclipse.jetty.websocket.api.Session;

import com.smartsparrow.courseware.data.CoursewareElement;
import com.smartsparrow.courseware.data.CoursewareElementType;
import com.smartsparrow.courseware.service.CoursewareService;
import com.smartsparrow.rtm.lang.RTMValidationException;
import com.smartsparrow.rtm.lang.WriteResponseException;
import com.smartsparrow.rtm.message.MessageHandler;
import com.smartsparrow.rtm.message.recv.courseware.activity.ActivityGenericMessage;
import com.smartsparrow.rtm.message.send.BasicResponseMessage;
import com.smartsparrow.rtm.util.Responses;
import com.smartsparrow.util.log.MercuryLogger;
import com.smartsparrow.util.log.MercuryLoggerFactory;
import com.smartsparrow.workspace.service.AlfrescoAssetPushService;

public class AlfrescoAssetsPushCountMessageHandler implements MessageHandler<ActivityGenericMessage> {

    private static final MercuryLogger log = MercuryLoggerFactory.getLogger(AlfrescoAssetsPushCountMessageHandler.class);

    public static final String AUTHOR_ALFRESCO_ASSETS_PUSH_COUNT = "author.alfresco.assets.push.count";
    private static final String AUTHOR_ALFRESCO_ASSETS_PUSH_COUNT_OK = "author.alfresco.assets.push.count.ok";
    private static final String AUTHOR_ALFRESCO_ASSETS_PUSH_COUNT_ERROR = "author.alfresco.assets.push.count.error";

    private final AlfrescoAssetPushService alfrescoAssetPushService;
    private final CoursewareService coursewareService;

    @Inject
    public AlfrescoAssetsPushCountMessageHandler(final AlfrescoAssetPushService alfrescoAssetPushService,
                                                 final CoursewareService coursewareService) {
        this.alfrescoAssetPushService = alfrescoAssetPushService;
        this.coursewareService = coursewareService;
    }

    @Override
    public void validate(ActivityGenericMessage message) throws RTMValidationException {

        affirmArgument(message.getActivityId() != null, "course id is required");
        List<CoursewareElement> path = coursewareService.getPath(message.getActivityId(), CoursewareElementType.ACTIVITY)
                .block();

        affirmArgument(path != null, "invalid root activity id");
        affirmArgument(path.size() > 0, "invalid root activity id");
        affirmArgument(path.get(0).getElementId().equals(message.getActivityId()), "invalid root activity id");
    }

    @Override
    public void handle(Session session, ActivityGenericMessage message) throws WriteResponseException {

        alfrescoAssetPushService.getAeroImageAssetCount(message.getActivityId())
                .subscribe(aeroImageAssetCount -> {

                    Responses.writeReactive(session,
                            new BasicResponseMessage(AUTHOR_ALFRESCO_ASSETS_PUSH_COUNT_OK, message.getId())
                                    .addField("pushAssetCount", aeroImageAssetCount)
                    );
                }, ex -> {

                    log.jsonError("Alfresco get aero asset count failed", new HashMap<String, Object>() {
                        {
                            put("courseId", message.getActivityId());
                        }
                    }, ex);

                    Responses.errorReactive(session, message.getId(), AUTHOR_ALFRESCO_ASSETS_PUSH_COUNT_ERROR,
                            HttpStatus.SC_INTERNAL_SERVER_ERROR,
                            String.format("error getting push asset count for the course: %s", message.getActivityId()));

                });
    }
}
