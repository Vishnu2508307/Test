package com.smartsparrow.rtm.wiring;

import java.util.HashMap;

import com.google.inject.AbstractModule;
import com.google.inject.TypeLiteral;
import com.google.inject.assistedinject.FactoryModuleBuilder;
import com.google.inject.multibindings.MapBinder;
import com.smartsparrow.cache.diffsync.AbstractDiffSyncSubscription;
import com.smartsparrow.cache.diffsync.DiffSyncSubscription;
import com.smartsparrow.dataevent.BroadcastMessage;
import com.smartsparrow.pubsub.data.EventConsumable;
import com.smartsparrow.rtm.subscription.cohort.CohortRTMSubscription;
import com.smartsparrow.rtm.subscription.cohort.archived.CohortArchivedRTMConsumer;
import com.smartsparrow.rtm.subscription.cohort.changed.CohortChangedRTMConsumer;
import com.smartsparrow.rtm.subscription.cohort.disenrolled.CohortDisEnrolledRTMConsumer;
import com.smartsparrow.rtm.subscription.cohort.enrolled.CohortEnrolledRTMConsumer;
import com.smartsparrow.rtm.subscription.cohort.granted.CohortGrantedRTMConsumer;
import com.smartsparrow.rtm.subscription.cohort.revoked.CohortRevokedRTMConsumer;
import com.smartsparrow.rtm.subscription.cohort.unarchived.CohortUnArchivedRTMConsumer;
import com.smartsparrow.rtm.subscription.competency.CompetencyDocumentEventRTMSubscription;
import com.smartsparrow.rtm.subscription.competency.association.created.CompetencyItemAssociationCreatedRTMConsumer;
import com.smartsparrow.rtm.subscription.competency.association.deleted.CompetencyItemAssociationDeletedRTMConsumer;
import com.smartsparrow.rtm.subscription.competency.created.DocumentItemCreatedRTMConsumer;
import com.smartsparrow.rtm.subscription.competency.deleted.DocumentDeletedRTMConsumer;
import com.smartsparrow.rtm.subscription.competency.deleted.DocumentItemDeletedRTMConsumer;
import com.smartsparrow.rtm.subscription.competency.updated.DocumentItemUpdatedRTMConsumer;
import com.smartsparrow.rtm.subscription.competency.updated.DocumentUpdatedRTMConsumer;
import com.smartsparrow.rtm.subscription.courseware.ActivityRTMSubscription;
import com.smartsparrow.rtm.subscription.courseware.alfrescoassetsupdate.ActivityAlfrescoAssetsUpdateRTMConsumer;
import com.smartsparrow.rtm.subscription.courseware.annotationcreated.AnnotationCreatedRTMConsumer;
import com.smartsparrow.rtm.subscription.courseware.annotationdeleted.AnnotationDeletedRTMConsumer;
import com.smartsparrow.rtm.subscription.courseware.annotationupdated.AnnotationUpdatedRTMConsumer;
import com.smartsparrow.rtm.subscription.courseware.assetadded.AssetAddedRTMConsumer;
import com.smartsparrow.rtm.subscription.courseware.assetoptimized.AssetOptimizedRTMConsumer;
import com.smartsparrow.rtm.subscription.courseware.assetremoved.AssetRemovedRTMConsumer;
import com.smartsparrow.rtm.subscription.courseware.assetsremoved.AssetsRemovedRTMConsumer;
import com.smartsparrow.rtm.subscription.courseware.configchange.ActivityConfigChangeRTMConsumer;
import com.smartsparrow.rtm.subscription.courseware.configchange.ComponentConfigChangeRTMConsumer;
import com.smartsparrow.rtm.subscription.courseware.configchange.FeedbackConfigChangeRTMConsumer;
import com.smartsparrow.rtm.subscription.courseware.configchange.InteractiveConfigChangeRTMConsumer;
import com.smartsparrow.rtm.subscription.courseware.configchange.PathwayConfigChangeRTMConsumer;
import com.smartsparrow.rtm.subscription.courseware.created.ActivityCreatedRTMConsumer;
import com.smartsparrow.rtm.subscription.courseware.created.ComponentCreatedRTMConsumer;
import com.smartsparrow.rtm.subscription.courseware.created.FeedbackCreatedRTMConsumer;
import com.smartsparrow.rtm.subscription.courseware.created.InteractiveCreatedRTMConsumer;
import com.smartsparrow.rtm.subscription.courseware.created.PathwayCreatedRTMConsumer;
import com.smartsparrow.rtm.subscription.courseware.created.ScenarioCreatedRTMConsumer;
import com.smartsparrow.rtm.subscription.courseware.deleted.ActivityDeletedRTMConsumer;
import com.smartsparrow.rtm.subscription.courseware.deleted.ComponentDeletedRTMConsumer;
import com.smartsparrow.rtm.subscription.courseware.deleted.FeedbackDeletedRTMConsumer;
import com.smartsparrow.rtm.subscription.courseware.deleted.InteractiveDeletedRTMConsumer;
import com.smartsparrow.rtm.subscription.courseware.deleted.PathwayDeletedRTMConsumer;
import com.smartsparrow.rtm.subscription.courseware.descriptivechange.DescriptiveChangeRTMConsumer;
import com.smartsparrow.rtm.subscription.courseware.duplicated.ActivityDuplicatedRTMConsumer;
import com.smartsparrow.rtm.subscription.courseware.duplicated.InteractiveDuplicatedRTMConsumer;
import com.smartsparrow.rtm.subscription.courseware.elementthemecreate.ElementThemeCreateRTMConsumer;
import com.smartsparrow.rtm.subscription.courseware.elementthemedelete.ElementThemeDeleteRTMConsumer;
import com.smartsparrow.rtm.subscription.courseware.evaluableset.EvaluableSetRTMConsumer;
import com.smartsparrow.rtm.subscription.courseware.manualgrading.ComponentConfigurationCreatedRTMConsumer;
import com.smartsparrow.rtm.subscription.courseware.manualgrading.ComponentManualGradingConfigDeletedRTMConsumer;
import com.smartsparrow.rtm.subscription.courseware.moved.ActivityMovedRTMConsumer;
import com.smartsparrow.rtm.subscription.courseware.moved.ComponentMovedRTMConsumer;
import com.smartsparrow.rtm.subscription.courseware.moved.InteractiveMovedRTMConsumer;
import com.smartsparrow.rtm.subscription.courseware.pathwayreordered.PathwayReOrderedRTMConsumer;
import com.smartsparrow.rtm.subscription.courseware.publication.PublicationJobRTMConsumer;
import com.smartsparrow.rtm.subscription.courseware.publication.PublicationJobRTMSubscription;
import com.smartsparrow.rtm.subscription.courseware.scenarioreordered.ScenarioReOrderedRTMConsumer;
import com.smartsparrow.rtm.subscription.courseware.themechange.ActivityThemeChangeRTMConsumer;
import com.smartsparrow.rtm.subscription.courseware.updated.ScenarioUpdatedRTMConsumer;
import com.smartsparrow.rtm.subscription.data.RTMConsumer;
import com.smartsparrow.rtm.subscription.data.RTMSubscription;
import com.smartsparrow.rtm.subscription.export.ExportEventRTMSubscription;
import com.smartsparrow.rtm.subscription.export.ExportRTMConsumer;
import com.smartsparrow.rtm.subscription.iam.IamAccountProvisionRTMConsumer;
import com.smartsparrow.rtm.subscription.iam.IamAccountProvisionRTMSubscription;
import com.smartsparrow.rtm.subscription.ingestion.IngestionRTMConsumer;
import com.smartsparrow.rtm.subscription.ingestion.IngestionRTMSubscription;
import com.smartsparrow.rtm.subscription.ingestion.project.ActivityIngestionRTMConsumer;
import com.smartsparrow.rtm.subscription.learner.StudentWalkablePrefetchRTMConsumer;
import com.smartsparrow.rtm.subscription.learner.StudentWalkablePrefetchRTMSubscription;
import com.smartsparrow.rtm.subscription.learner.studentprogress.StudentProgressRTMConsumer;
import com.smartsparrow.rtm.subscription.learner.studentprogress.StudentProgressRTMSubscription;
import com.smartsparrow.rtm.subscription.learner.studentscope.StudentScopeRTMConsumer;
import com.smartsparrow.rtm.subscription.learner.studentscope.StudentScopeRTMSubscription;
import com.smartsparrow.rtm.subscription.plugin.PluginPermissionRTMSubscription;
import com.smartsparrow.rtm.subscription.plugin.granted.PluginPermissionGrantedRTMConsumer;
import com.smartsparrow.rtm.subscription.plugin.revoked.PluginPermissionRevokedRTMConsumer;
import com.smartsparrow.rtm.subscription.project.ProjectEventRTMConsumer;
import com.smartsparrow.rtm.subscription.project.ProjectEventRTMSubscription;
import com.smartsparrow.rtm.subscription.workspace.ProjectCreatedRTMConsumer;
import com.smartsparrow.rtm.subscription.workspace.WorkspaceRTMSubscription;
import com.smartsparrow.util.log.MercuryLogger;
import com.smartsparrow.util.log.MercuryLoggerFactory;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

class RTMSubscriptionModule extends AbstractModule {

    private static final MercuryLogger log = MercuryLoggerFactory.getLogger(RTMSubscriptionModule.class);

    private MapBinder<Class<? extends RTMSubscription>, RTMConsumer<? extends EventConsumable<? extends BroadcastMessage>>> subscriptionConsumers;

    public RTMSubscriptionModule() {
    }

    @SuppressFBWarnings(value = "DM_EXIT", justification = "Shut down the application if the subscription consumers binding fails")
    protected void configure() {
        subscriptionConsumers = MapBinder.newMapBinder(binder(),
                new TypeLiteral<Class<? extends RTMSubscription>>() {
                },
                new TypeLiteral<RTMConsumer<? extends EventConsumable<? extends BroadcastMessage>>>() {
                })
                .permitDuplicates();

        install(new FactoryModuleBuilder()
                        .implement(AbstractDiffSyncSubscription.class, DiffSyncSubscription.class)
                        .build(DiffSyncSubscription.DiffSyncSubscriptionFactory.class));

        install(new FactoryModuleBuilder()
                        .implement(RTMSubscription.class,ProjectEventRTMSubscription.class)
                        .build(ProjectEventRTMSubscription.ProjectEventRTMSubscriptionFactory.class));
        install(new FactoryModuleBuilder()
                        .implement(RTMSubscription.class,WorkspaceRTMSubscription.class)
                        .build(WorkspaceRTMSubscription.WorkspaceRTMSubscriptionFactory.class));
        install(new FactoryModuleBuilder()
                        .implement(RTMSubscription.class,ExportEventRTMSubscription.class)
                        .build(ExportEventRTMSubscription.ExportEventRTMSubscriptionFactory.class));
        install(new FactoryModuleBuilder()
                        .implement(RTMSubscription.class,CompetencyDocumentEventRTMSubscription.class)
                        .build(CompetencyDocumentEventRTMSubscription.CompetencyDocumentEventRTMSubscriptionFactory.class));
        install(new FactoryModuleBuilder()
                        .implement(RTMSubscription.class,CohortRTMSubscription.class)
                        .build(CohortRTMSubscription.CohortRTMSubscriptionFactory.class));
        install(new FactoryModuleBuilder()
                        .implement(RTMSubscription.class,IamAccountProvisionRTMSubscription.class)
                        .build(IamAccountProvisionRTMSubscription.IamAccountProvisionRTMSubscriptionFactory.class));
        install(new FactoryModuleBuilder()
                        .implement(RTMSubscription.class,ActivityRTMSubscription.class)
                        .build(ActivityRTMSubscription.ActivityRTMSubscriptionFactory.class));
        install(new FactoryModuleBuilder()
                        .implement(RTMSubscription.class,IngestionRTMSubscription.class)
                        .build(IngestionRTMSubscription.IngestionRTMSubscriptionFactory.class));
        install(new FactoryModuleBuilder()
                        .implement(RTMSubscription.class,PluginPermissionRTMSubscription.class)
                        .build(PluginPermissionRTMSubscription.PluginPermissionRTMSubscriptionFactory.class));
        install(new FactoryModuleBuilder()
                        .implement(RTMSubscription.class,StudentWalkablePrefetchRTMSubscription.class)
                        .build(StudentWalkablePrefetchRTMSubscription.StudentWalkablePrefetchRTMSubscriptionFactory.class));
        install(new FactoryModuleBuilder()
                        .implement(RTMSubscription.class,StudentScopeRTMSubscription.class)
                        .build(StudentScopeRTMSubscription.StudentScopeRTMSubscriptionFactory.class));
        install(new FactoryModuleBuilder()
                        .implement(RTMSubscription.class, PublicationJobRTMSubscription.class)
                        .build(PublicationJobRTMSubscription.PublicationJobRTMSubscriptionFactory.class));
        install(new FactoryModuleBuilder()
                        .implement(RTMSubscription.class, StudentProgressRTMSubscription.class)
                        .build(StudentProgressRTMSubscription.StudentProgressRTMSubscriptionFactory.class));




        // if the binding fails exit
        try {
            bindSubscriptionConsumers();
        } catch (Exception e) {
            log.jsonError(e.getMessage(), new HashMap<>(), e);
            System.exit(1);
        }
    }

    private void bindSubscriptionConsumers() {

        //
        // Workspace RTM Subscription
        //
        new BinderBuilder(WorkspaceRTMSubscription.class)
                .addConsumer(ProjectCreatedRTMConsumer.class);

      // Cohort RTM Subscription
        new BinderBuilder(CohortRTMSubscription.class)
                .addConsumer(CohortChangedRTMConsumer.class);
        new BinderBuilder(CohortRTMSubscription.class)
                .addConsumer(CohortEnrolledRTMConsumer.class);
        new BinderBuilder(CohortRTMSubscription.class)
                .addConsumer(CohortDisEnrolledRTMConsumer.class);
        new BinderBuilder(CohortRTMSubscription.class)
                .addConsumer(CohortGrantedRTMConsumer.class);
        new BinderBuilder(CohortRTMSubscription.class)
                .addConsumer(CohortRevokedRTMConsumer.class);
        new BinderBuilder(CohortRTMSubscription.class)
                .addConsumer(CohortUnArchivedRTMConsumer.class);
        new BinderBuilder(CohortRTMSubscription.class)
                .addConsumer(CohortArchivedRTMConsumer.class);

        //Plugin Permission RTM Subscription
        new BinderBuilder(PluginPermissionRTMSubscription.class)
                .addConsumer(PluginPermissionGrantedRTMConsumer.class);
        new BinderBuilder(PluginPermissionRTMSubscription.class)
                .addConsumer(PluginPermissionRevokedRTMConsumer.class);

        // Iam account provision RTM Subscription
        new BinderBuilder(IamAccountProvisionRTMSubscription.class)
                .addConsumer(IamAccountProvisionRTMConsumer.class);

        //Competency Document RTM Subscription
        new BinderBuilder(CompetencyDocumentEventRTMSubscription.class)
                .addConsumer(DocumentItemCreatedRTMConsumer.class);
        new BinderBuilder(CompetencyDocumentEventRTMSubscription.class)
                .addConsumer(DocumentItemUpdatedRTMConsumer.class);
        new BinderBuilder(CompetencyDocumentEventRTMSubscription.class)
                .addConsumer(DocumentItemDeletedRTMConsumer.class);
        new BinderBuilder(CompetencyDocumentEventRTMSubscription.class)
                .addConsumer(CompetencyItemAssociationCreatedRTMConsumer.class);
        new BinderBuilder(CompetencyDocumentEventRTMSubscription.class)
                .addConsumer(CompetencyItemAssociationDeletedRTMConsumer.class);
        new BinderBuilder(CompetencyDocumentEventRTMSubscription.class)
                .addConsumer(DocumentUpdatedRTMConsumer.class);
        new BinderBuilder(CompetencyDocumentEventRTMSubscription.class)
                .addConsumer(DocumentDeletedRTMConsumer.class);

        // Publication job status Subscription
        new BinderBuilder(PublicationJobRTMSubscription.class)
                .addConsumer(PublicationJobRTMConsumer.class);

        // Learner Walkable Prefetch RTM Subscription
        new BinderBuilder(StudentWalkablePrefetchRTMSubscription.class)
                .addConsumer(StudentWalkablePrefetchRTMConsumer.class);

        // student progress RTM Subscription
        new BinderBuilder(StudentProgressRTMSubscription.class)
                .addConsumer(StudentProgressRTMConsumer.class);

        // Student scope RTM subscription
        new BinderBuilder(StudentScopeRTMSubscription.class)
                .addConsumer(StudentScopeRTMConsumer.class);

        // Export RTM subscription
        new BinderBuilder(ExportEventRTMSubscription.class)
                .addConsumer(ExportRTMConsumer.class);

        // Ingestion RTM subscription
        new BinderBuilder(ProjectEventRTMSubscription.class)
                .addConsumer(ProjectEventRTMConsumer.class);
        new BinderBuilder(IngestionRTMSubscription.class)
                .addConsumer(IngestionRTMConsumer.class);
        // Project Ingestion RTM subscription
        new BinderBuilder(ActivityRTMSubscription.class)
                .addConsumer(ActivityIngestionRTMConsumer.class);

        // Activity RTM subscription

        // CREATED
        new BinderBuilder(ActivityRTMSubscription.class)
                .addConsumer(ActivityCreatedRTMConsumer.class);
        new BinderBuilder(ActivityRTMSubscription.class)
                .addConsumer(ScenarioCreatedRTMConsumer.class);
        new BinderBuilder(ActivityRTMSubscription.class)
                .addConsumer(InteractiveCreatedRTMConsumer.class);
        new BinderBuilder(ActivityRTMSubscription.class)
                .addConsumer(PathwayCreatedRTMConsumer.class);
        new BinderBuilder(ActivityRTMSubscription.class)
                .addConsumer(FeedbackCreatedRTMConsumer.class);
        new BinderBuilder(ActivityRTMSubscription.class)
                .addConsumer(ComponentCreatedRTMConsumer.class);
        // DELETED
        new BinderBuilder(ActivityRTMSubscription.class)
                .addConsumer(ActivityDeletedRTMConsumer.class);
        new BinderBuilder(ActivityRTMSubscription.class)
                .addConsumer(InteractiveDeletedRTMConsumer.class);
        new BinderBuilder(ActivityRTMSubscription.class)
                .addConsumer(PathwayDeletedRTMConsumer.class);
        new BinderBuilder(ActivityRTMSubscription.class)
                .addConsumer(FeedbackDeletedRTMConsumer.class);
        new BinderBuilder(ActivityRTMSubscription.class)
                .addConsumer(ComponentDeletedRTMConsumer.class);
        // UPDATED
        new BinderBuilder(ActivityRTMSubscription.class)
                .addConsumer(ScenarioUpdatedRTMConsumer.class);
        // DUPLICATED
        new BinderBuilder(ActivityRTMSubscription.class)
                .addConsumer(ActivityDuplicatedRTMConsumer.class);
        new BinderBuilder(ActivityRTMSubscription.class)
                .addConsumer(InteractiveDuplicatedRTMConsumer.class);
        // SCENARIO_REORDERED
        new BinderBuilder(ActivityRTMSubscription.class)
                .addConsumer(ScenarioReOrderedRTMConsumer.class);
        // CONFIG_CHANGE
        new BinderBuilder(ActivityRTMSubscription.class)
                .addConsumer(ActivityConfigChangeRTMConsumer.class);
        new BinderBuilder(ActivityRTMSubscription.class)
                .addConsumer(ComponentConfigChangeRTMConsumer.class);
        new BinderBuilder(ActivityRTMSubscription.class)
                .addConsumer(InteractiveConfigChangeRTMConsumer.class);
        new BinderBuilder(ActivityRTMSubscription.class)
                .addConsumer(PathwayConfigChangeRTMConsumer.class);
        new BinderBuilder(ActivityRTMSubscription.class)
                .addConsumer(FeedbackConfigChangeRTMConsumer.class);
        // THEME_CHANGE
        new BinderBuilder(ActivityRTMSubscription.class)
                .addConsumer(ActivityThemeChangeRTMConsumer.class);
        // PATHWAY_REORDERED
        new BinderBuilder(ActivityRTMSubscription.class)
                .addConsumer(PathwayReOrderedRTMConsumer.class);
        // ASSET_ADDED
        new BinderBuilder(ActivityRTMSubscription.class)
                .addConsumer(AssetAddedRTMConsumer.class);
        // ASSET_REMOVED
        new BinderBuilder(ActivityRTMSubscription.class)
                .addConsumer(AssetRemovedRTMConsumer.class);
        // MANUAL_GRADING_CONFIGURATION_CREATED
        new BinderBuilder(ActivityRTMSubscription.class)
                .addConsumer(ComponentConfigurationCreatedRTMConsumer.class);
        // MANUAL_GRADING_CONFIGURATION_DELETED
        new BinderBuilder(ActivityRTMSubscription.class)
                .addConsumer(ComponentManualGradingConfigDeletedRTMConsumer.class);
        // DESCRIPTIVE_CHANGE
        new BinderBuilder(ActivityRTMSubscription.class)
                .addConsumer(DescriptiveChangeRTMConsumer.class);
        // ACTIVITY_MOVED
        new BinderBuilder(ActivityRTMSubscription.class)
                .addConsumer(ActivityMovedRTMConsumer.class);
        // INTERACTIVE_MOVED
        new BinderBuilder(ActivityRTMSubscription.class)
                .addConsumer(InteractiveMovedRTMConsumer.class);
        // ANNOTATION_CREATED
        new BinderBuilder(ActivityRTMSubscription.class)
                .addConsumer(AnnotationCreatedRTMConsumer.class);
        // ANNOTATION_UPDATED
        new BinderBuilder(ActivityRTMSubscription.class)
                .addConsumer(AnnotationUpdatedRTMConsumer.class);
        // ANNOTATION_DELETED
        new BinderBuilder(ActivityRTMSubscription.class)
                .addConsumer(AnnotationDeletedRTMConsumer.class);
        // EVALUABLE_SET
        new BinderBuilder(ActivityRTMSubscription.class)
                .addConsumer(EvaluableSetRTMConsumer.class);
        // ASSETS_REMOVED
        new BinderBuilder(ActivityRTMSubscription.class)
                .addConsumer(AssetsRemovedRTMConsumer.class);
        // ELEMENT_THEME_CREATE
        new BinderBuilder(ActivityRTMSubscription.class)
                .addConsumer(ElementThemeCreateRTMConsumer.class);
        // ELEMENT_THEME_DELETE
        new BinderBuilder(ActivityRTMSubscription.class)
                .addConsumer(ElementThemeDeleteRTMConsumer.class);
        //ALFRESCO_ASSETS_UPDATE
        new BinderBuilder(ActivityRTMSubscription.class)
                .addConsumer(ActivityAlfrescoAssetsUpdateRTMConsumer.class);
        //ASSET_OPTIMIZED
        new BinderBuilder(ActivityRTMSubscription.class)
                .addConsumer(AssetOptimizedRTMConsumer.class);
        // COMPONENT_MOVED
        new BinderBuilder(ActivityRTMSubscription.class)
                .addConsumer(ComponentMovedRTMConsumer.class);
    }

    private class BinderBuilder {
        private final Class<? extends RTMSubscription> type;

        private BinderBuilder(Class<? extends RTMSubscription> type) {
            this.type = type;
        }

        final BinderBuilder addConsumer(final Class<? extends RTMConsumer<? extends EventConsumable<? extends BroadcastMessage>>> consumer) {
            subscriptionConsumers.addBinding(type).to(consumer);
            return this;
        }
    }


}
