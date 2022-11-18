package com.smartsparrow.rtm.message.handler.asset;

import static com.smartsparrow.util.Warrants.affirmArgument;

import java.util.HashMap;
import java.util.List;
import java.util.UUID;

import javax.inject.Inject;

import com.smartsparrow.exception.NotAuthorizedException;
import com.smartsparrow.iam.lang.UnauthorizedFault;
import com.smartsparrow.sso.lang.MyCloudServiceFault;
import com.smartsparrow.sso.service.MyCloudService;
import com.smartsparrow.workspace.data.AlfrescoAssetSyncStatus;
import com.smartsparrow.workspace.data.AlfrescoAssetSyncSummary;
import com.smartsparrow.workspace.service.AlfrescoAssetSyncType;
import com.smartsparrow.workspace.service.AlfrescoAssetTrackService;
import com.smartsparrow.asset.service.AlfrescoAssetService;
import org.apache.commons.collections4.map.HashedMap;
import org.apache.commons.httpclient.HttpStatus;
import org.eclipse.jetty.websocket.api.Session;

import com.smartsparrow.courseware.data.CoursewareElement;
import com.smartsparrow.courseware.data.CoursewareElementType;
import com.smartsparrow.courseware.service.CoursewareService;
import com.smartsparrow.iam.wiring.AuthenticationContextProvider;
import com.smartsparrow.rtm.lang.RTMValidationException;
import com.smartsparrow.rtm.lang.WriteResponseException;
import com.smartsparrow.rtm.message.MessageHandler;
import com.smartsparrow.rtm.message.recv.asset.AlfrescoAssetsPushMessage;
import com.smartsparrow.rtm.message.send.BasicResponseMessage;
import com.smartsparrow.rtm.util.Responses;
import com.smartsparrow.util.UUIDs;
import com.smartsparrow.util.log.MercuryLogger;
import com.smartsparrow.util.log.MercuryLoggerFactory;
import com.smartsparrow.util.log.ReactiveMdc;
import com.smartsparrow.workspace.service.AlfrescoAssetPushService;

public class AlfrescoAssetsPushMessageHandler implements MessageHandler<AlfrescoAssetsPushMessage> {
    private static final MercuryLogger log = MercuryLoggerFactory.getLogger(AlfrescoAssetsPushMessageHandler.class);

    public static final String AUTHOR_ALFRESCO_ASSETS_PUSH = "author.alfresco.assets.push";
    private static final String AUTHOR_ALFRESCO_ASSETS_PUSH_OK = "author.alfresco.assets.push.ok";
    private static final String AUTHOR_ALFRESCO_ASSETS_PUSH_ERROR = "author.alfresco.assets.push.error";

    private final AlfrescoAssetService alfrescoAssetService;
    private final AlfrescoAssetPushService alfrescoAssetPushService;
    private final CoursewareService coursewareService;
    private final AuthenticationContextProvider authenticationContextProvider;
    private final AlfrescoAssetTrackService alfrescoAssetTrackService;
    private final MyCloudService myCloudService;

    @Inject
    public AlfrescoAssetsPushMessageHandler(final AlfrescoAssetService alfrescoAssetService,
                                            final AlfrescoAssetPushService alfrescoAssetPushService,
                                            final CoursewareService coursewareService,
                                            final AuthenticationContextProvider authenticationContextProvider,
                                            final AlfrescoAssetTrackService alfrescoAssetTrackService,
                                            final MyCloudService myCloudService) {
        this.alfrescoAssetService = alfrescoAssetService;
        this.alfrescoAssetPushService = alfrescoAssetPushService;
        this.coursewareService = coursewareService;
        this.authenticationContextProvider = authenticationContextProvider;
        this.alfrescoAssetTrackService = alfrescoAssetTrackService;
        this.myCloudService = myCloudService;
    }

    @Override
    public void validate(AlfrescoAssetsPushMessage message) throws RTMValidationException {
        affirmArgument(message.getAlfrescoNodeId() != null, "Alfresco node id is required");
        affirmArgument(message.getActivityId() != null, "course id is required");
        List<CoursewareElement> path = coursewareService.getPath(message.getActivityId(), CoursewareElementType.ACTIVITY)
                .block();

        affirmArgument(path != null, "invalid root activity id");
        affirmArgument(path.size() > 0, "invalid root activity id");
        affirmArgument(path.get(0).getElementId().equals(message.getActivityId()), "invalid root activity id");
    }

    @Override
    public void handle(Session session, AlfrescoAssetsPushMessage message) throws WriteResponseException {

        final String myCloudToken = authenticationContextProvider.get().getPearsonToken();
        // check if the myCloud token is still valid before continuing
        try {
            myCloudService.validateToken(myCloudToken).block();
        }
        catch (UnauthorizedFault | MyCloudServiceFault ex) {
            BasicResponseMessage response = new BasicResponseMessage(AUTHOR_ALFRESCO_ASSETS_PUSH_ERROR, ex.getResponseStatusCode(), message.getId());
            response.addField("reason", ex.getMessage());
            Responses.write(session, response);

            return;
        }

        final UUID referenceId = UUIDs.timeBased();
        final UUID activityId = message.getActivityId();

        UUID trackReferenceId = alfrescoAssetTrackService.setReferenceId(activityId, referenceId, AlfrescoAssetSyncType.PUSH).block();
        log.jsonInfo("push Bronte activity assets to Alfresco", new HashedMap<String, Object>() {
            {
                put("activityId", activityId);
                put("referenceId", referenceId);
                put("trackReferenceId", trackReferenceId);
            }
        });

        if (!referenceId.equals(trackReferenceId)) {
            log.jsonInfo("Alfresco assets push already in progress for activity, returning existing reference id", new HashedMap<String, Object>() {
                {
                    put("activityId", activityId);
                    put("referenceId", referenceId);
                    put("trackReferenceId", trackReferenceId);
                }
            });

            Responses.writeReactive(session,
                    new BasicResponseMessage(AUTHOR_ALFRESCO_ASSETS_PUSH_OK, message.getId())
                            .addField("activityId", activityId)
                            .addField("referenceId", trackReferenceId));
            return;
        }
        AlfrescoAssetSyncSummary alfrescoAssetSyncSummary = new AlfrescoAssetSyncSummary()
                .setReferenceId(referenceId)
                .setCourseId(activityId)
                .setSyncType(AlfrescoAssetSyncType.PUSH)
                .setStatus(AlfrescoAssetSyncStatus.IN_PROGRESS);
        alfrescoAssetTrackService.saveAlfrescoAssetSyncSummary(alfrescoAssetSyncSummary).blockFirst(); // todo reactify code

        alfrescoAssetService.getNodeChildren(message.getAlfrescoNodeId().toString(), myCloudToken)
                .then(alfrescoAssetPushService.pushCourseAssets(referenceId, activityId, myCloudToken, message.getAlfrescoNodeId()))
                .doOnEach(log.reactiveErrorThrowable("error pushing assets to Alfresco", throwable -> new HashedMap<String, Object>() {
                    {
                        put("activityId", activityId);
                        put("referenceId", referenceId);
                    }
                }))
                .subscriberContext(ReactiveMdc.with(ReactiveMdc.Property.REQUEST_CONTEXT))
                .subscribe(requestNotifications -> {
                    log.jsonInfo("started new Alfresco assets push for activity", new HashedMap<String, Object>() {
                        {
                            put("activityId", activityId);
                            put("referenceId", referenceId);
                            put("trackReferenceId", trackReferenceId);
                        }
                    });

                    Responses.writeReactive(session,
                            new BasicResponseMessage(AUTHOR_ALFRESCO_ASSETS_PUSH_OK, message.getId())
                                    .addField("activityId", activityId)
                                    .addField("referenceId", referenceId));
                }, ex -> {

                    log.jsonError("Alfresco assets push request failed", new HashedMap<String, Object>() {
                        {
                            put("activityId", activityId);
                            put("referenceId", referenceId);
                            put("trackReferenceId", trackReferenceId);
                        }
                    }, ex);

                    Responses.errorReactive(session, message.getId(),
                            AUTHOR_ALFRESCO_ASSETS_PUSH_ERROR,
                            HttpStatus.SC_INTERNAL_SERVER_ERROR, String.format("error pushing assets to Alfresco for referenceid: %s", referenceId));
                });
    }
}
