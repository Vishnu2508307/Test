package com.smartsparrow.rtm.message.handler.asset;

import com.smartsparrow.courseware.data.CoursewareElement;
import com.smartsparrow.courseware.data.CoursewareElementType;
import com.smartsparrow.courseware.service.CoursewareService;
import com.smartsparrow.rtm.lang.RTMValidationException;
import com.smartsparrow.rtm.lang.WriteResponseException;
import com.smartsparrow.rtm.message.MessageHandler;
import com.smartsparrow.rtm.message.recv.asset.AlfrescoAssetsProgressMessage;
import com.smartsparrow.rtm.message.send.BasicResponseMessage;
import com.smartsparrow.rtm.util.Responses;
import com.smartsparrow.util.log.MercuryLogger;
import com.smartsparrow.util.log.MercuryLoggerFactory;
import com.smartsparrow.workspace.service.AlfrescoAssetTrackService;
import org.apache.http.HttpStatus;
import org.eclipse.jetty.websocket.api.Session;

import javax.inject.Inject;
import java.util.HashMap;
import java.util.List;

import static com.smartsparrow.util.Warrants.affirmArgument;

public class AlfrescoAssetsProgressMessageHandler implements MessageHandler<AlfrescoAssetsProgressMessage> {

    private static final MercuryLogger log = MercuryLoggerFactory.getLogger(AlfrescoAssetsProgressMessageHandler.class);

    public static final String AUTHOR_ALFRESCO_ASSETS_PROGRESS = "author.alfresco.assets.progress";
    public static final String AUTHOR_ALFRESCO_ASSETS_PROGRESS_OK = "author.alfresco.assets.progress.ok";
    public static final String AUTHOR_ALFRESCO_ASSETS_PROGRESS_ERROR = "author.alfresco.assets.progress.error";

    private final AlfrescoAssetTrackService alfrescoAssetTrackService;
    private final CoursewareService coursewareService;

    @Inject
    public AlfrescoAssetsProgressMessageHandler(final AlfrescoAssetTrackService alfrescoAssetTrackService,
                                                final CoursewareService coursewareService) {
        this.alfrescoAssetTrackService = alfrescoAssetTrackService;
        this.coursewareService = coursewareService;
    }

    @Override
    public void validate(AlfrescoAssetsProgressMessage message) throws RTMValidationException {

        affirmArgument(message.getSyncType() != null, "sync type is required");

        affirmArgument(message.getActivityId() != null, "course id is required");
        List<CoursewareElement> path = coursewareService.getPath(message.getActivityId(), CoursewareElementType.ACTIVITY)
                .block();

        affirmArgument(path != null, "invalid root activity id");
        affirmArgument(path.size() > 0, "invalid root activity id");
        affirmArgument(path.get(0).getElementId().equals(message.getActivityId()), "invalid root activity id");
    }

    @Override
    public void handle(Session session, AlfrescoAssetsProgressMessage message) throws WriteResponseException {

        alfrescoAssetTrackService.isSyncInProgress(message.getActivityId(), message.getSyncType())
                .subscribe(inProgress -> {
                            Responses.writeReactive(session, new BasicResponseMessage(AUTHOR_ALFRESCO_ASSETS_PROGRESS_OK, message.getId())
                                    .addField("courseId", message.getActivityId())
                                    .addField("syncType", message.getSyncType())
                                    .addField("inProgress", inProgress));
                        },
                        ex -> {
                            log.debug("Unable to check if an alfresco sync is in progress", new HashMap<String, Object>() {
                                {
                                    put("message", message.toString());
                                    put("error", ex.getStackTrace());
                                }
                            });
                            Responses.errorReactive(session, message.getId(), AUTHOR_ALFRESCO_ASSETS_PROGRESS_ERROR, HttpStatus.SC_UNPROCESSABLE_ENTITY,
                                    "Unable to check if an alfresco sync is in progress");
                        }
                );
    }
}
