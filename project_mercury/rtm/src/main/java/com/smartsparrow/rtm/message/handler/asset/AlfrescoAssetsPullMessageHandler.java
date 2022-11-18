package com.smartsparrow.rtm.message.handler.asset;

import static com.smartsparrow.util.Warrants.affirmArgument;
import static com.smartsparrow.util.Warrants.affirmArgumentNotNullOrEmpty;

import java.util.HashMap;
import java.util.List;
import java.util.UUID;

import javax.inject.Inject;

import com.smartsparrow.exception.NotAuthorizedException;
import com.smartsparrow.iam.lang.UnauthorizedFault;
import com.smartsparrow.iam.wiring.AuthenticationContextProvider;
import com.smartsparrow.sso.lang.MyCloudServiceFault;
import com.smartsparrow.sso.service.MyCloudService;
import com.smartsparrow.workspace.data.AlfrescoAssetSyncStatus;
import com.smartsparrow.workspace.data.AlfrescoAssetSyncSummary;
import com.smartsparrow.workspace.data.AlfrescoAssetTrackGateway;
import com.smartsparrow.workspace.service.AlfrescoAssetSyncType;
import com.smartsparrow.workspace.service.AlfrescoAssetTrackService;
import org.apache.commons.collections4.map.HashedMap;
import org.apache.commons.httpclient.HttpStatus;
import org.eclipse.jetty.websocket.api.Session;

import com.smartsparrow.courseware.data.CoursewareElement;
import com.smartsparrow.courseware.data.CoursewareElementType;
import com.smartsparrow.courseware.service.CoursewareService;
import com.smartsparrow.rtm.lang.RTMValidationException;
import com.smartsparrow.rtm.lang.WriteResponseException;
import com.smartsparrow.rtm.message.MessageHandler;
import com.smartsparrow.rtm.message.recv.asset.AlfrescoAssetsPullMessage;
import com.smartsparrow.rtm.message.send.BasicResponseMessage;
import com.smartsparrow.rtm.util.Responses;
import com.smartsparrow.util.UUIDs;
import com.smartsparrow.util.log.MercuryLogger;
import com.smartsparrow.util.log.MercuryLoggerFactory;
import com.smartsparrow.util.log.ReactiveMdc;
import com.smartsparrow.workspace.service.AlfrescoAssetPullService;

public class AlfrescoAssetsPullMessageHandler implements MessageHandler<AlfrescoAssetsPullMessage> {

    private static final MercuryLogger log = MercuryLoggerFactory.getLogger(AlfrescoAssetsPullMessageHandler.class);

    public static final String AUTHOR_ALFRESCO_ASSETS_SYNC = "author.alfresco.assets.sync";
    private static final String AUTHOR_ALFRESCO_ASSETS_SYNC_OK = "author.alfresco.assets.sync.ok";
    private static final String AUTHOR_ALFRESCO_ASSETS_SYNC_ERROR = "author.alfresco.assets.sync.error";

    private final AlfrescoAssetPullService alfrescoAssetPullService;
    private final CoursewareService coursewareService;
    private final AuthenticationContextProvider authenticationContextProvider;
    private final AlfrescoAssetTrackService alfrescoAssetTrackService;
    private final MyCloudService myCloudService;

    @Inject
    public AlfrescoAssetsPullMessageHandler(final AlfrescoAssetPullService alfrescoAssetPullService,
                                            final CoursewareService coursewareService,
                                            final AuthenticationContextProvider authenticationContextProvider,
                                            final AlfrescoAssetTrackService alfrescoAssetTrackService,
                                            final MyCloudService myCloudService) {
        this.alfrescoAssetPullService = alfrescoAssetPullService;
        this.coursewareService = coursewareService;
        this.authenticationContextProvider = authenticationContextProvider;
        this.alfrescoAssetTrackService = alfrescoAssetTrackService;
        this.myCloudService = myCloudService;
    }

    @Override
    public void validate(AlfrescoAssetsPullMessage message) throws RTMValidationException {
        affirmArgument(message.getActivityId() != null, "activity id is required");

        // ensure that the activity is a root level activity, inner activities are not supported
        List<CoursewareElement> path = coursewareService.getPath(message.getActivityId(), CoursewareElementType.ACTIVITY)
                .block();

        affirmArgument(path != null, "invalid root activity id");
        affirmArgument(path.size() > 0, "invalid root activity id");
        affirmArgument(path.get(0).getElementId().equals(message.getActivityId()), "invalid root activity id");
    }

    @Override
    public void handle(Session session, AlfrescoAssetsPullMessage message) throws WriteResponseException {

        final String myCloudToken = authenticationContextProvider.get().getPearsonToken();
        // check if the myCloud token is still valid before continuing
        try {
            myCloudService.validateToken(myCloudToken).block();
        }
        catch (UnauthorizedFault | MyCloudServiceFault ex) {
            BasicResponseMessage response = new BasicResponseMessage(AUTHOR_ALFRESCO_ASSETS_SYNC_ERROR, ex.getResponseStatusCode(), message.getId());
            response.addField("reason", ex.getMessage());
            Responses.write(session, response);

            return;
        }

        // generate a referenceId for this request
        final UUID referenceId = UUIDs.timeBased();
        final UUID activityId = message.getActivityId();

        UUID trackReferenceId = alfrescoAssetTrackService.setReferenceId(activityId, referenceId, AlfrescoAssetSyncType.PULL).block();
        log.jsonInfo("pull Alfresco assets for activity to Bronte", new HashedMap<String, Object>() {
            {
                put("activityId", activityId);
                put("referenceId", referenceId);
                put("trackReferenceId", trackReferenceId);
            }
        });

        if (!referenceId.equals(trackReferenceId)) {
            log.jsonInfo("Alfresco assets pull already in progress for activity, returning existing reference id", new HashedMap<String, Object>() {
                {
                    put("activityId", activityId);
                    put("referenceId", referenceId);
                    put("trackReferenceId", trackReferenceId);
                }
            });

            Responses.writeReactive(session,
                    new BasicResponseMessage(AUTHOR_ALFRESCO_ASSETS_SYNC_OK, message.getId())
                            .addField("activityId", activityId)
                            .addField("referenceId", trackReferenceId));
            return;
        }
        AlfrescoAssetSyncSummary alfrescoAssetSyncSummary = new AlfrescoAssetSyncSummary()
                .setReferenceId(referenceId)
                .setCourseId(activityId)
                .setSyncType(AlfrescoAssetSyncType.PULL)
                .setStatus(AlfrescoAssetSyncStatus.IN_PROGRESS);
        alfrescoAssetTrackService.saveAlfrescoAssetSyncSummary(alfrescoAssetSyncSummary).blockFirst(); // todo reactify code

        // sync and pull all the assets from alfresco
        alfrescoAssetPullService.pullAssets(referenceId, activityId, myCloudToken, message.isForceAssetSync())
                .doOnEach(log.reactiveErrorThrowable("error pulling assets from alfresco", throwable -> new HashedMap<String, Object>() {
                    {
                        put("activityId", activityId);
                        put("referenceId", referenceId);
                    }
                }))
                .subscriberContext(ReactiveMdc.with(ReactiveMdc.Property.REQUEST_CONTEXT))
                .collectList()
                .subscribe(notifications -> {
                    log.jsonInfo("started new Alfresco assets pull for activity", new HashedMap<String, Object>() {
                        {
                            put("activityId", activityId);
                            put("referenceId", referenceId);
                            put("trackReferenceId", trackReferenceId);
                        }
                    });

                    Responses.writeReactive(session,
                            new BasicResponseMessage(AUTHOR_ALFRESCO_ASSETS_SYNC_OK, message.getId())
                                    .addField("activityId", activityId)
                                    .addField("referenceId", referenceId));
                }, ex -> {
                    log.jsonError("Alfresco assets pull request failed", new HashMap<String, Object>() {
                        {
                            put("activityId", activityId);
                            put("referenceId", referenceId);
                            put("trackReferenceId", trackReferenceId);
                        }
                    }, ex);
                    Responses.errorReactive(session, message.getId(),
                            AUTHOR_ALFRESCO_ASSETS_SYNC_ERROR,
                            HttpStatus.SC_INTERNAL_SERVER_ERROR, String.format("error syncing assets to Alfresco for referenceid: %s", referenceId));
                });


    }
}
