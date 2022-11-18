package com.smartsparrow.wiring.rtm;

import static com.smartsparrow.rtm.message.handler.GraphQLQueryMessageHandler.GRAPHQL_QUERY;
import static com.smartsparrow.rtm.message.handler.asset.GetLearnerAssetMessageHandler.LEARNER_ASSET_GET;
import static com.smartsparrow.rtm.message.handler.courseware.LearnerEvaluateMessageHandler.LEARNER_EVALUATE;
import static com.smartsparrow.rtm.message.handler.diffsync.DiffSyncAckMessageHandler.DIFF_SYNC_ACK;
import static com.smartsparrow.rtm.message.handler.diffsync.DiffSyncEndMessageHandler.DIFF_SYNC_END;
import static com.smartsparrow.rtm.message.handler.diffsync.DiffSyncPatchMessageHandler.DIFF_SYNC_PATCH;
import static com.smartsparrow.rtm.message.handler.diffsync.DiffSyncStartMessageHandler.DIFF_SYNC_START;
import static com.smartsparrow.rtm.message.handler.iam.IESAuthorizeMessageHandler.IES_AUTHORIZE;
import static com.smartsparrow.rtm.message.handler.learner.ProgressSubscribeMessageHandler.LEARNER_PROGRESS_SUBSCRIBE;
import static com.smartsparrow.rtm.message.handler.learner.ProgressUnsubscribeMessageHandler.LEARNER_PROGRESS_UNSUBSCRIBE;
import static com.smartsparrow.rtm.message.handler.learner.RestartActivityMessageHandler.LEARNER_ACTIVITY_RESTART;
import static com.smartsparrow.rtm.message.handler.learner.SetStudentScopeMessageHandler.LEARNER_STUDENT_SCOPE_SET;
import static com.smartsparrow.rtm.message.handler.learner.StudentPrefetchSubscribeMessageHandler.LEARNER_STUDENT_PREFETCH_SUBSCRIBE;
import static com.smartsparrow.rtm.message.handler.learner.StudentPrefetchUnsubscribeMessageHandler.LEARNER_STUDENT_PREFETCH_UNSUBSCRIBE;
import static com.smartsparrow.rtm.message.handler.learner.StudentScopeSubscribeMessageHandler.LEARNER_STUDENT_SCOPE_SUBSCRIBE;
import static com.smartsparrow.rtm.message.handler.learner.StudentScopeUnsubscribeMessageHandler.LEARNER_STUDENT_SCOPE_UNSUBSCRIBE;
import static com.smartsparrow.rtm.message.handler.learner.annotation.CreateLearnerAnnotationMessageHandler.LEARNER_ANNOTATION_CREATE;
import static com.smartsparrow.rtm.message.handler.learner.annotation.DeleteLearnerAnnotationMessageHandler.LEARNER_ANNOTATION_DELETE;
import static com.smartsparrow.rtm.message.handler.learner.annotation.ListDeploymentAnnotationMessageHandler.LEARNER_ANNOTATION_LIST;
import static com.smartsparrow.rtm.message.handler.learner.annotation.UpdateLearnerAnnotationMessageHandler.LEARNER_ANNOTATION_UPDATE;
import static com.smartsparrow.rtm.message.handler.learner.theme.GetLearnerThemeVariantMessageHandler.LEARNER_THEME_VARIANT_GET;
import static com.smartsparrow.rtm.message.handler.learner.theme.GetSelectedThemeMessageHandler.LEARNER_SELECTED_THEME_GET;
import static com.smartsparrow.rtm.message.handler.math.LearnerMathAssetGetMessageHandler.LEARNER_MATH_ASSET_GET;
import static com.smartsparrow.rtm.message.handler.plugin.LearnspacePluginLogMessageHandler.LEARNSPACE_PLUGIN_LOG;

import com.smartsparrow.rtm.message.authorization.AllowAuthenticated;
import com.smartsparrow.rtm.message.authorization.AllowCohortInstructorOrEnrolledStudentAuthorizer;
import com.smartsparrow.rtm.message.authorization.AllowDeleteLearnerAnnotationAuthorizer;
import com.smartsparrow.rtm.message.authorization.AllowListDeploymentAnnotationAuthorizer;
import com.smartsparrow.rtm.message.authorization.AllowUpdateDeploymentAnnotationAuthorizer;
import com.smartsparrow.rtm.message.authorization.AllowWorkspaceRoles;
import com.smartsparrow.rtm.message.authorization.Everyone;
import com.smartsparrow.rtm.message.handler.GraphQLQueryMessageHandler;
import com.smartsparrow.rtm.message.handler.asset.GetLearnerAssetMessageHandler;
import com.smartsparrow.rtm.message.handler.courseware.LearnerEvaluateMessageHandler;
import com.smartsparrow.rtm.message.handler.diffsync.DiffSyncAckMessageHandler;
import com.smartsparrow.rtm.message.handler.diffsync.DiffSyncEndMessageHandler;
import com.smartsparrow.rtm.message.handler.diffsync.DiffSyncPatchMessageHandler;
import com.smartsparrow.rtm.message.handler.diffsync.DiffSyncStartMessageHandler;
import com.smartsparrow.rtm.message.handler.iam.IESAuthorizeMessageHandler;
import com.smartsparrow.rtm.message.handler.learner.ProgressSubscribeMessageHandler;
import com.smartsparrow.rtm.message.handler.learner.ProgressUnsubscribeMessageHandler;
import com.smartsparrow.rtm.message.handler.learner.RestartActivityMessageHandler;
import com.smartsparrow.rtm.message.handler.learner.SetStudentScopeMessageHandler;
import com.smartsparrow.rtm.message.handler.learner.StudentPrefetchSubscribeMessageHandler;
import com.smartsparrow.rtm.message.handler.learner.StudentPrefetchUnsubscribeMessageHandler;
import com.smartsparrow.rtm.message.handler.learner.StudentScopeSubscribeMessageHandler;
import com.smartsparrow.rtm.message.handler.learner.StudentScopeUnsubscribeMessageHandler;
import com.smartsparrow.rtm.message.handler.learner.annotation.CreateLearnerAnnotationMessageHandler;
import com.smartsparrow.rtm.message.handler.learner.annotation.DeleteLearnerAnnotationMessageHandler;
import com.smartsparrow.rtm.message.handler.learner.annotation.ListDeploymentAnnotationMessageHandler;
import com.smartsparrow.rtm.message.handler.learner.annotation.UpdateLearnerAnnotationMessageHandler;
import com.smartsparrow.rtm.message.handler.learner.theme.GetLearnerThemeVariantMessageHandler;
import com.smartsparrow.rtm.message.handler.learner.theme.GetSelectedThemeMessageHandler;
import com.smartsparrow.rtm.message.handler.math.LearnerMathAssetGetMessageHandler;
import com.smartsparrow.rtm.message.handler.plugin.LearnspacePluginLogMessageHandler;
import com.smartsparrow.rtm.message.recv.GetAssetMessage;
import com.smartsparrow.rtm.message.recv.GraphQLQueryMessage;
import com.smartsparrow.rtm.message.recv.courseware.LearnerEvaluateMessage;
import com.smartsparrow.rtm.message.recv.diffsync.DiffSyncAckMessage;
import com.smartsparrow.rtm.message.recv.diffsync.DiffSyncEndMessage;
import com.smartsparrow.rtm.message.recv.diffsync.DiffSyncMessage;
import com.smartsparrow.rtm.message.recv.diffsync.DiffSyncPatchMessage;
import com.smartsparrow.rtm.message.recv.diffsync.DiffSyncStartMessage;
import com.smartsparrow.rtm.message.recv.iam.IESAuthorizeMessage;
import com.smartsparrow.rtm.message.recv.learner.ListLearnerAnnotationMessage;
import com.smartsparrow.rtm.message.recv.learner.ProgressSubscribeMessage;
import com.smartsparrow.rtm.message.recv.learner.RestartActivityMessage;
import com.smartsparrow.rtm.message.recv.learner.SetStudentScopeMessage;
import com.smartsparrow.rtm.message.recv.learner.StudentGenericMessage;
import com.smartsparrow.rtm.message.recv.learner.StudentScopeSubscribeMessage;
import com.smartsparrow.rtm.message.recv.learner.annotation.CreateLearnerAnnotationMessage;
import com.smartsparrow.rtm.message.recv.learner.annotation.DeleteLearnerAnnotationMessage;
import com.smartsparrow.rtm.message.recv.learner.annotation.UpdateLearnerAnnotationMessage;
import com.smartsparrow.rtm.message.recv.learner.theme.GetLearnerThemeVariantMessage;
import com.smartsparrow.rtm.message.recv.learner.theme.GetSelectedThemeMessage;
import com.smartsparrow.rtm.message.recv.math.MathAssetGetMessage;
import com.smartsparrow.rtm.message.recv.plugin.LearnspacePluginLogMessage;
import com.smartsparrow.rtm.wiring.RTMMessageBindingException;
import com.smartsparrow.rtm.wiring.RTMMessageOperations;

/**
 * Binds RTM message apis that are relevant to the learnspace
 */
public class LearnspaceRTMOperationsBinding {

    private final RTMMessageOperations binder;

    public LearnspaceRTMOperationsBinding(RTMMessageOperations rtmMessageOperations) {
        this.binder = rtmMessageOperations;
    }

    public void bind() throws RTMMessageBindingException {
        binder.bind(IES_AUTHORIZE)
                .toMessageType(IESAuthorizeMessage.class)
                .withAuthorizers(Everyone.class)
                .withMessageHandlers(IESAuthorizeMessageHandler.class);

        binder.bind(LEARNSPACE_PLUGIN_LOG)
                .toMessageType(LearnspacePluginLogMessage.class)
                .withAuthorizers(Everyone.class)
                .withMessageHandlers(LearnspacePluginLogMessageHandler.class);

        binder.bind(LEARNER_EVALUATE) //
                .toMessageType(LearnerEvaluateMessage.class)
                .withAuthorizers(AllowAuthenticated.class)
                .withMessageHandlers(LearnerEvaluateMessageHandler.class);

        binder.bind(LEARNER_STUDENT_SCOPE_SET) //
                .toMessageType(SetStudentScopeMessage.class)
                .withAuthorizers(AllowAuthenticated.class)
                .withMessageHandlers(SetStudentScopeMessageHandler.class);

        binder.bind(LEARNER_PROGRESS_SUBSCRIBE) //
                .toMessageType(ProgressSubscribeMessage.class)
                .withAuthorizers(AllowAuthenticated.class)
                .withMessageHandlers(ProgressSubscribeMessageHandler.class);

        binder.bind(LEARNER_PROGRESS_UNSUBSCRIBE) //
                .toMessageType(ProgressSubscribeMessage.class)
                .withAuthorizers(AllowAuthenticated.class)
                .withMessageHandlers(ProgressUnsubscribeMessageHandler.class);

        binder.bind(LEARNER_STUDENT_SCOPE_SUBSCRIBE)
                .toMessageType(StudentScopeSubscribeMessage.class)
                .withAuthorizers(AllowAuthenticated.class) // TODO Add allow enrolled authorizer (to deployment and or cohort)
                .withMessageHandlers(StudentScopeSubscribeMessageHandler.class);

        binder.bind(LEARNER_STUDENT_SCOPE_UNSUBSCRIBE)
                .toMessageType(StudentScopeSubscribeMessage.class)
                .withAuthorizers(AllowAuthenticated.class)
                .withMessageHandlers(StudentScopeUnsubscribeMessageHandler.class);

        binder.bind(LEARNER_ACTIVITY_RESTART)
                .toMessageType(RestartActivityMessage.class)
                .withAuthorizers(AllowAuthenticated.class)
                .withMessageHandlers(RestartActivityMessageHandler.class);

        /* Learner annotation Message  */
        binder.bind(LEARNER_ANNOTATION_CREATE)
                .toMessageType(CreateLearnerAnnotationMessage.class)
                .withAuthorizers(AllowAuthenticated.class, AllowCohortInstructorOrEnrolledStudentAuthorizer.class)
                .withMessageHandlers(CreateLearnerAnnotationMessageHandler.class);

        binder.bind(LEARNER_ANNOTATION_UPDATE)
                .toMessageType(UpdateLearnerAnnotationMessage.class)
                .withAuthorizers(AllowAuthenticated.class, AllowWorkspaceRoles.class, AllowUpdateDeploymentAnnotationAuthorizer.class)
                .withMessageHandlers(UpdateLearnerAnnotationMessageHandler.class);

        binder.bind(LEARNER_ANNOTATION_LIST)
                .toMessageType(ListLearnerAnnotationMessage.class)
                .withAuthorizers(AllowAuthenticated.class, AllowWorkspaceRoles.class, AllowListDeploymentAnnotationAuthorizer.class)
                .withMessageHandlers(ListDeploymentAnnotationMessageHandler.class);

        binder.bind(LEARNER_ANNOTATION_DELETE)
                .toMessageType(DeleteLearnerAnnotationMessage.class)
                .withAuthorizers(AllowAuthenticated.class, AllowDeleteLearnerAnnotationAuthorizer.class)
                .withMessageHandlers(DeleteLearnerAnnotationMessageHandler.class);

        binder.bind(LEARNER_ASSET_GET)
                .toMessageType(GetAssetMessage.class)
                .withAuthorizers(AllowAuthenticated.class)
                .withMessageHandlers(GetLearnerAssetMessageHandler.class);

        binder.bind(LEARNER_STUDENT_PREFETCH_SUBSCRIBE)
                .toMessageType(StudentGenericMessage.class)
                .withAuthorizers(AllowAuthenticated.class)
                .withMessageHandlers(StudentPrefetchSubscribeMessageHandler.class);

        binder.bind(LEARNER_STUDENT_PREFETCH_UNSUBSCRIBE)
                .toMessageType(StudentGenericMessage.class)
                .withAuthorizers(AllowAuthenticated.class)
                .withMessageHandlers(StudentPrefetchUnsubscribeMessageHandler.class);

        /* Math */
        binder.bind(LEARNER_MATH_ASSET_GET)
                .toMessageType(MathAssetGetMessage.class)
                .withMessageHandlers(LearnerMathAssetGetMessageHandler.class)
                .withAuthorizers(AllowAuthenticated.class, AllowWorkspaceRoles.class);

        /* GraphQL Messages */
        binder.bind(GRAPHQL_QUERY) //
                .toMessageType(GraphQLQueryMessage.class) //
                .withAuthorizers(AllowAuthenticated.class) //
                .withMessageHandlers(GraphQLQueryMessageHandler.class);

        // diff sync patch and ack
        binder.bind(DIFF_SYNC_PATCH)
                .toMessageType(DiffSyncPatchMessage.class)
                .withAuthorizers(AllowAuthenticated.class, AllowWorkspaceRoles.class)
                .withMessageHandlers(DiffSyncPatchMessageHandler.class);

        binder.bind(DIFF_SYNC_ACK)
                .toMessageType(DiffSyncAckMessage.class)
                .withAuthorizers(AllowAuthenticated.class, AllowWorkspaceRoles.class)
                .withMessageHandlers(DiffSyncAckMessageHandler.class);

        // diff sync start and end
        binder.bind(DIFF_SYNC_START)
                .toMessageType(DiffSyncStartMessage.class)
                .withAuthorizers(AllowAuthenticated.class, AllowWorkspaceRoles.class)
                .withMessageHandlers(DiffSyncStartMessageHandler.class);

        binder.bind(DIFF_SYNC_END)
                .toMessageType(DiffSyncEndMessage.class)
                .withAuthorizers(AllowAuthenticated.class, AllowWorkspaceRoles.class)
                .withMessageHandlers(DiffSyncEndMessageHandler.class);

        binder.bind(LEARNER_SELECTED_THEME_GET)
                .toMessageType(GetSelectedThemeMessage.class)
                .withAuthorizers(AllowAuthenticated.class)
                .withMessageHandlers(GetSelectedThemeMessageHandler.class);

        binder.bind(LEARNER_THEME_VARIANT_GET)
                .toMessageType(GetLearnerThemeVariantMessage.class)
                .withAuthorizers(AllowAuthenticated.class)
                .withMessageHandlers(GetLearnerThemeVariantMessageHandler.class);
    }
}
